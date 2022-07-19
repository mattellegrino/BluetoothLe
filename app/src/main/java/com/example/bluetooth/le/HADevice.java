package com.example.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

public class HADevice implements Parcelable {
    public static final String ACTION_DEVICE_CHANGED = "action.device_changed";
    public static final String EXTRA_DEVICE = "device";
    private String DeviceName;
    private String DeviceAddress;
    private State DeviceState = State.NOT_CONNECTED;

    public HADevice(String deviceName, String deviceAddress) {
        DeviceName = deviceName;
        DeviceAddress = deviceAddress;
    }

    protected HADevice(Parcel in) {
        DeviceName = in.readString();
        DeviceAddress = in.readString();
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
        return DeviceName;
    }

    public String getDeviceAddress() {
        return DeviceAddress;
    }

    public State getDeviceState() {
        return DeviceState;
    }

    public void setState(State newState) {
        this.DeviceState = newState;
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(DeviceName);
        dest.writeString(DeviceAddress);
        dest.writeInt(DeviceState.ordinal());
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
        return DeviceState.ordinal() >= State.CONNECTED.ordinal();
    }
    public boolean isInitializing() {
        return DeviceState == State.INITIALIZING;
    }

    public boolean isInitialized() {
        return DeviceState.ordinal() >= State.INITIALIZED.ordinal();
    }

    public boolean isConnecting() {
        return DeviceState == State.CONNECTING;
    }

}
