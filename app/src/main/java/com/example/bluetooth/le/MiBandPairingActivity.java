package com.example.bluetooth.le;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class MiBandPairingActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
