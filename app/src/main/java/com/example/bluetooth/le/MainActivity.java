package com.example.bluetooth.le;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("firstTime", 0); // 0 - for private mode
        String value_shared = pref.getString("isBonded", null);
        if(value_shared != null ){
            String sharedprefaddress = getApplicationContext().getSharedPreferences("Device", 0).getString("MacAddress-MIBAND3","-1");
            String statedevice = getApplicationContext().getSharedPreferences("Device", 0).getString("State-MIBAND3","-1");
            HADevice.State stateEnum = HADevice.State.valueOf(statedevice);
            setContentView(R.layout.activity_bonded);
            TextView devicename = findViewById(R.id.DeviceName);
            devicename.setText("Mi Band 3");
            TextView deviceaddress= findViewById(R.id.DeviceAddress);
            deviceaddress.setText(sharedprefaddress);
            final Intent intent = new Intent(this, DeviceControlActivity.class);
            HADevice haDevice = new HADevice("Mi Band 3",sharedprefaddress);
            haDevice.setState(stateEnum);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME,"Mi Band 3");
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,sharedprefaddress);
            intent.putExtra(DeviceControlActivity.EXTRA_CONNECT_FIRST_TIME,false);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE, haDevice);

            startActivity(intent);
        }
         else
            setContentView(R.layout.activity_main);



        ImageButton blbutton;
        blbutton = findViewById(R.id.buttonbluetooth);
        Intent intent = new Intent(this, DeviceScanActivity.class);
        blbutton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(intent);
            }
        });
    }

}