package com.example.bluetooth.le;

/*  Copyright (C) 2015-2021 Andreas Shimokawa, Carsten Pfeiffer, Daniel
    Dakhno, Daniele Gobbetti

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.example.bluetooth.le.ble.BtLEAction;
import com.example.bluetooth.le.ble.BtLEQueue;
import com.example.bluetooth.le.ble.NotifyAction;
import com.example.bluetooth.le.ble.ReadAction;
import com.example.bluetooth.le.ble.RequestMtuAction;
import com.example.bluetooth.le.ble.WaitAction;
import com.example.bluetooth.le.ble.WriteAction;


public class TransactionBuilder {

    private final Transaction mTransaction;
    private boolean mQueued;
    public TransactionBuilder(String taskName) {
        mTransaction = new Transaction(taskName);
    }

    public TransactionBuilder read(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            Log.w("Transaction Builder","Unable to read characteristic: null");
            return this;
        }
        ReadAction action = new ReadAction(characteristic);
        return add(action);
    }

    public TransactionBuilder write(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (characteristic == null) {
            Log.w("Transaction Builder","Unable to write characteristic: null");
            return this;
        }
        WriteAction action = new WriteAction(characteristic, data);
        return add(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TransactionBuilder requestMtu(int mtu){
        return add(
                new RequestMtuAction(mtu)
        );
    }

    public TransactionBuilder notify(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (characteristic == null) {
            Log.w("Transaction Builder","Unable to notify characteristic: null");
            return this;
        }
        NotifyAction action = createNotifyAction(characteristic, enable);
        return add(action);
    }

    protected NotifyAction createNotifyAction(BluetoothGattCharacteristic characteristic, boolean enable) {
        return new NotifyAction(characteristic, enable);
    }

    /**
     * Causes the queue to sleep for the specified time.
     * Note that this is usually a bad idea, since it will not be able to process messages
     * during that time. It is also likely to cause race conditions.
     * @param millis the number of milliseconds to sleep
     */
    public TransactionBuilder wait(int millis) {
        WaitAction action = new WaitAction(millis);
        return add(action);
    }

    public TransactionBuilder add(BtLEAction action) {
        mTransaction.add(action);
        return this;
    }

    /**
     * Sets a GattCallback instance that will be called when the transaction is executed,
     * resulting in GattCallback events.
     *
     * @param callback the callback to set, may be null
     */
    public void setGattCallback(@Nullable GattCallback callback) {
        mTransaction.setGattCallback(callback);
    }

    public
    @Nullable
    GattCallback getGattCallback() {
        return mTransaction.getGattCallback();
    }

    /**
     * To be used as the final step to execute the transaction by the given queue.
     *
     * @param queue
     */
    public void queue(BtLEQueue queue) {
        if (mQueued) {
            throw new IllegalStateException("This builder had already been queued. You must not reuse it.");
        }
        mQueued = true;
        queue.add(mTransaction);
    }

    public Transaction getTransaction() {
        return mTransaction;
    }

    public String getTaskName() {
        return mTransaction.getTaskName();
    }
}
