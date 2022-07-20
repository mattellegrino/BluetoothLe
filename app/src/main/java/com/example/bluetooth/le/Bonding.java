package com.example.bluetooth.le;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Locale;

public class Bonding {

    private static final long DELAY_AFTER_BONDING = 1000; // 1s


    public static BroadcastReceiver getBondingReceiver(final BondingInterface bondingInterface) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String bondingMacAddress = bondingInterface.getCurrentTarget().getDeviceAddress();
                    try {
                    Log.i("Bonding","Bond state changed: " + device + ", state: " + device.getBondState() + ", expected address: " + bondingMacAddress);
                    if (bondingMacAddress != null && bondingMacAddress.equals(device.getAddress())) {
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                        switch (bondState) {
                            case BluetoothDevice.BOND_BONDED: {
                                Log.i("Bonding","Bonded with " + device.getAddress());
                                //noinspection StatementWithEmptyBody
                                attemptToFirstConnect(bondingInterface.getCurrentTarget(),device);
                                return;
                            }
                            case BluetoothDevice.BOND_NONE: {
                                Log.i("Bonding","Not bonded with " + device.getAddress() + ", attempting to connect anyway.");
                                attemptToFirstConnect(bondingInterface.getCurrentTarget(),device);
                                return;
                            }
                            case BluetoothDevice.BOND_BONDING: {
                                Log.i("Bonding","Bonding in progress with " + device.getAddress());
                                return;
                            }
                            default: {
                                Log.w("Bonding","Unknown bond state for device " + device.getAddress() + ": " + bondState);
                                bondingInterface.onBondingComplete(false);
                            }
                        }
                    }
                }
                    catch(SecurityException se){
                        se.printStackTrace();
                        throw se;
                    }
                }
            }
        };
    }

    public static void tryBondThenComplete(BondingInterface bondingInterface, HADevice HAdevice) {
        bondingInterface.registerBroadcastReceivers();
        BluetoothDevice device = HAdevice.getBluetoothDevice();

        try {
        int bondState = device.getBondState();
        if (bondState == BluetoothDevice.BOND_BONDED) {
            System.out.println("BOND_BONDED");
            //GB.toast(bondingInterface.getContext().getString(R.string.pairing_already_bonded, device.getName(), device.getAddress()), Toast.LENGTH_SHORT, GB.INFO);
            //noinspection StatementWithEmptyBody
            attemptToFirstConnect(HAdevice,bondingInterface.getCurrentTarget().getBluetoothDevice());
            return;

        } else if (bondState == BluetoothDevice.BOND_BONDING) {
            System.out.println("BOND_BONDING");
            //GB.toast(bondingInterface.getContext(), bondingInterface.getContext().getString(R.string.pairing_in_progress, device.getName(), device.getAddress()), Toast.LENGTH_LONG, GB.INFO);
            return;
        }

        //GB.toast(bondingInterface.getContext(), bondingInterface.getContext().getString(R.string.pairing_creating_bond_with, device.getName(), device.getAddress()), Toast.LENGTH_LONG, GB.INFO);
        //toast(bondingInterface.getContext(), bondingInterface.getContext().getString(R.string.discovery_attempting_to_pair, device.getName()), Toast.LENGTH_SHORT, GB.INFO);

            System.out.println("BLUETOOTH BOND");
            bluetoothBond(bondingInterface, HAdevice);


    }
        catch (SecurityException se){
            se.printStackTrace();
            throw se;
        }
    }

    public static void attemptToFirstConnect(HADevice HADevice, final BluetoothDevice bluetoothDevice) {
        Looper mainLooper = Looper.getMainLooper();
        new Handler(mainLooper).postDelayed(new Runnable() {
            @Override
            public void run() {
                //GBApplication.deviceService().disconnect();
                //GBDevice device = DeviceHelper.getInstance().toSupportedDevice(candidate);
                Intent intent = new Intent(HealthApplication.getContext(),DeviceControlActivity.class);
                intent.putExtra("HADevice",HADevice);
                intent.putExtra(DeviceControlActivity.EXTRA_CONNECT_FIRST_TIME,true);
                startDeviceControlActivity(intent);
            }
        }, DELAY_AFTER_BONDING);
    }



    private static void startDeviceControlActivity(Intent intent) {

     HealthApplication.getContext().startActivity(intent);
    }

    public static void stopBluetoothBonding(BluetoothDevice device) {
        try {
            //noinspection JavaReflectionMemberAccess
            device.getClass().getMethod("cancelBondProcess").invoke(device);
        } catch (Throwable ignore) {
        }
    }

    private static void bluetoothBond(BondingInterface context, HADevice haDevice) {
        BluetoothDevice device = haDevice.getBluetoothDevice();
        try {
        if (device.createBond()) {
            // Async, results will be delivered via a broadcast
            Log.i("Bonding","Bonding in progress...");
        } else {
            Log.e("Bonding",String.format(Locale.getDefault(),
                    "Bonding failed immediately! %1$s (%2$s) %3$d",
                    device.getName(),
                    device.getAddress(),
                    device.getType())
            );

            BluetoothClass bluetoothClass = device.getBluetoothClass();
            if (bluetoothClass != null) {
                Log.e("Bonding",String.format(Locale.getDefault(),
                        "BluetoothClass: %1$s",
                        bluetoothClass.toString()));
            }

            // Theoretically we shouldn't be doing this
            // because this function shouldn't've been called
            // with an already bonded device
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.w("Bonding","For some reason the device is already bonded, but let's try first connect");
                attemptToFirstConnect(context.getCurrentTarget(),device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                Log.w("Bonding","Device is still bonding after an error");
                // TODO: Not sure we can handle this better, it's weird already.
            } else {
                Log.w("Bonding","Bonding failed immediately and no bond was made");
                //toast(context.getContext(), context.getContext().getString(R.string.discovery_bonding_failed_immediately, device.getName()), Toast.LENGTH_SHORT, GB.ERROR);
            }
        }
    }
        catch (SecurityException se){
            se.printStackTrace();
            throw se;
        }
    }
}
