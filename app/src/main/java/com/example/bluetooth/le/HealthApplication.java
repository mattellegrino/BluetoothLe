package com.example.bluetooth.le;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class HealthApplication extends Application {
    private static  DatabaseReference d_reference;
    private static FirebaseAuth f_auth;
    private User user;
    /**
     * Main Application class that initializes and provides access to certain things like
     * logging and DB access.
     */

    private static HealthApplication context;

    public static final String ACTION_QUIT
            = "com.example.bluetooth.le.action.quit";
    public static final String ACTION_LANGUAGE_CHANGE = "com.example.bluetooth.le.action.language_change";
    public static final String ACTION_NEW_DATA = "com.example.bluetooth.le.action.new_data";

    public HealthApplication() {
        context = this;
        // don't do anything here, add it to onCreate instead
    }

    public static void quit() {
        Intent quitIntent = new Intent(HealthApplication.ACTION_QUIT);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(quitIntent);
        //HealthApplication.deviceService().quit();
        System.exit(0);
    }

    /* Puo servire nel caso si accoppiano piu' dispositivi, o il dispositivo stesso, recupera le sharedpreferences */
    public static SharedPreferences getDeviceSpecificSharedPrefs(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isEmpty()) {
            return null;
        }
        return context.getSharedPreferences( deviceIdentifier, Context.MODE_PRIVATE);
    }

    /* stesssa cosa puo essere utile in futuro */

    public static void deleteDeviceSpecificSharedPrefs(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isEmpty()) {
            return;
        }
        context.getSharedPreferences("devicesettings_" + deviceIdentifier, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static void commit_database_steps(String finaldata) {

    }


    private void RetrieveUserInfoAndPush(DataSnapshot customersSnapshot, String email){

        // for each customer
        for (DataSnapshot ds : customersSnapshot.getChildren()){

            String mail = (String)ds.child("email").getValue();

            // if the current element has the same email as the one written by the user (or last session)
            if (email.equals(mail)){

                // set the rest id
                user.setEmail(email);
                user.setMac_address((String)ds.child("mac_address").getValue());
                user.setHeart_rate((String)ds.child("heart_rate").getValue());
                user.setCurrent_steps((String)ds.child("current_steps").getValue());
                // tell the application to use this customer id and mail
                setUser(user);

                // Open the reservation list activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static HealthApplication getContext() {
        return context;
    }

    public static boolean isRunningLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isRunningMarshmallowOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isRunningNougatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isRunningOreoOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static void setDatabase(DatabaseReference reference, FirebaseAuth auth) {
        d_reference=reference;
        f_auth=auth;
    }
}
