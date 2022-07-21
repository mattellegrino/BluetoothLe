package com.example.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MiBandPairingActivity  extends AppCompatActivity implements BondingInterface{

    private final BroadcastReceiver bondingReceiver = Bonding.getBondingReceiver(this);
    private final BroadcastReceiver pairingReceiver = Bonding.getPairingReceiver(this);
    public static final String EXTRAS_DEVICE = "Device_Info";
    private TextView message;
    private HADevice HAdevice;
    private boolean isPairing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_band_pairing);
        message = findViewById(R.id.miband_pair_message);
        HAdevice = getIntent().getParcelableExtra(DeviceControlActivity.EXTRAS_DEVICE);
        System.out.println(HAdevice.getDeviceAddress());
        startPairing();
    }


    private void startPairing() {
        isPairing = true;
        Bonding.tryBondThenComplete(this, HAdevice,getContext());

    }

    @Override
    public void onBondingComplete(boolean success) {
        Log.d("MiBandPairingActivity","pairingFinished: " + success);
        if (!isPairing) {
            // already gone?
            return;
        } else {
            isPairing = false;
        }

        if (success) {
            // remember the device since we do not necessarily pair... temporary -- we probably need
            // to query the db for available devices in ControlCenter. But only remember un-bonded
            // devices, as bonded devices are displayed anyway.
            String macAddress = HAdevice.getDeviceAddress();
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
            if (device != null && HAdevice.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_NONE) {
                SharedPreferences prefs = HealthApplication.getSharedPrefs();
            }

            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    }

    @Override
    public HADevice getCurrentTarget() {
        return HAdevice;
    }



    public void unregisterBroadcastReceivers() {
        safeUnregisterBroadcastReceiver(this,pairingReceiver);
        safeUnregisterBroadcastReceiver(this,bondingReceiver);
    }

    public static boolean safeUnregisterBroadcastReceiver(Context context, BroadcastReceiver receiver) {
        try {
            context.unregisterReceiver(receiver);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void registerBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(pairingReceiver, new IntentFilter(HADevice.ACTION_DEVICE_CHANGED));
        registerReceiver(bondingReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    public Context getContext() {
        return this;
    }


    @Override
    protected void onResume() {
        registerBroadcastReceivers();
        super.onResume();
    }

    @Override
    protected void onStart() {
        registerBroadcastReceivers();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceivers();
        if (isPairing) {
            stopPairing();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        unregisterBroadcastReceivers();
        if (isPairing) {
            stopPairing();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        // WARN: Do not stop pairing or unregister receivers pause!
        // Bonding process can pause the activity and you might miss broadcasts
        super.onPause();
    }


    private void stopPairing() {
        isPairing = false;
        //Bonding.stopBluetoothBonding(HAdevice.getBluetoothDevice());
    }
}
