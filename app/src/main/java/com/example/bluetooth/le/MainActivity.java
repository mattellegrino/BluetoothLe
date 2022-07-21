package com.example.bluetooth.le;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("firstTime", 0); // 0 - for private mode
        String value_shared = pref.getString("isBonded", null);
        if(value_shared != null ){
            setContentView(R.layout.activity_bonded);
        }
         else
            setContentView(R.layout.activity_main);



        ImageButton blbutton;
        /* database reference */
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