package com.example.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MiBandPairingActivity  extends AppCompatActivity implements BondingInterface{

    private final BroadcastReceiver bondingReceiver = Bonding.getBondingReceiver(this);
    public static final String EXTRAS_DEVICE = "Device_Info";
    private TextView message;
    private HADevice HAdevice;
    private boolean isPairing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_band_pairing);
        message = findViewById(R.id.miband_pair_message);
        HAdevice = getIntent().getParcelableExtra(MiBandPairingActivity.EXTRAS_DEVICE);
        System.out.println(HAdevice.getDeviceAddress());
        startPairing();
    }


    private void startPairing() {
        isPairing = true;
        Bonding.tryBondThenComplete(this, HAdevice);

    }

    @Override
    public void onBondingComplete(boolean success) {

    }

    @Override
    public HADevice getCurrentTarget() {
        return HAdevice;
    }

    @Override
    public void unregisterBroadcastReceivers() {

    }

    @Override
    public void registerBroadcastReceivers() {
        //LocalBroadcastManager.getInstance(this).registerReceiver(pairingReceiver, new IntentFilter(GBDevice.ACTION_DEVICE_CHANGED));
        registerReceiver(bondingReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    public Context getContext() {
        return null;
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
        Bonding.stopBluetoothBonding(HAdevice.getBluetoothDevice());
    }
}
