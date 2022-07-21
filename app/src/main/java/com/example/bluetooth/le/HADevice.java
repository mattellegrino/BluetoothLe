package com.example.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class HADevice implements Parcelable {
    public static final String ACTION_DEVICE_CHANGED = "action.device_changed";
    public static final String EXTRA_DEVICE = "device";
    private BluetoothDevice bluetoothDevice;
    private String deviceName;
    private String deviceAddress;
    private State deviceState = State.NOT_CONNECTED;

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setDeviceState(State deviceState) {
        this.deviceState = deviceState;
    }

    public HADevice(String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }


    public HADevice(String deviceName, String deviceAddress, BluetoothDevice device) {
        this.bluetoothDevice = device;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    protected HADevice(Parcel in) {
        bluetoothDevice = in.readParcelable(getClass().getClassLoader());
        deviceName = in.readString();
        deviceAddress = in.readString();
        deviceState = State.values()[in.readInt()];
    }

    public static final Creator<HADevice> CREATOR = new Creator<HADevice>() {
        @Override
        public HADevice createFromParcel(Parcel in) {
            return new HADevice(in);
        }

        @Override
        public HADevice[] newArray(int size) {
            return new HADevice[size];
        }
    };

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public State getDeviceState() {
        return deviceState;
    }

    public void setState(State newState) {
        this.deviceState = newState;
    }

    public void sendDeviceUpdateIntent(Context context) {
        Intent deviceUpdateIntent = new Intent(ACTION_DEVICE_CHANGED);
        deviceUpdateIntent.putExtra(EXTRA_DEVICE, (Parcelable) this);
        LocalBroadcastManager.getInstance(context).sendBroadcast(deviceUpdateIntent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(bluetoothDevice, 0);
        parcel.writeString(deviceName);
        parcel.writeString(deviceAddress);
        parcel.writeInt(deviceState.ordinal());

    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public enum State {
        // Note: the order is important!
        NOT_CONNECTED,
        WAITING_FOR_RECONNECT,
        CONNECTING,
        CONNECTED,
        INITIALIZING,
        AUTHENTICATION_REQUIRED, // some kind of pairing is required by the device
        AUTHENTICATING, // some kind of pairing is requested by the device
        /**
         * Means that the device is connected AND all the necessary initialization steps
         * have been performed. At the very least, this means that basic information like
         * device name, firmware version, hardware revision (as applicable) is available
         * in the GBDevice.
         */
        INITIALIZED,
    }

    public boolean isConnected() {
        return deviceState.ordinal() >= State.CONNECTED.ordinal();
    }
    public boolean isInitializing() {
        return deviceState == State.INITIALIZING;
    }

    public boolean isInitialized() {
        return deviceState.ordinal() >= State.INITIALIZED.ordinal();
    }

    public boolean isConnecting() {
        return deviceState == State.CONNECTING;
    }

}
