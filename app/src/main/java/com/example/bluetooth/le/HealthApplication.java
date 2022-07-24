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
    private static User user;
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

    public static void commit_database_macaddress() {
        d_reference.child(user.getID()).child("mac_address").setValue(user.getMac_address());
    }

    public static void commit_database_csteps(String steps,String current_time) {
        d_reference.child(user.getID()).child("current_steps").setValue(steps);
        d_reference.child(user.getID()).child("current_time").setValue(current_time);
    }
    public static User getUser() {
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

    public static DatabaseReference getD_reference() {
        return d_reference;
    }
    public static FirebaseAuth getF_auth() {
        return f_auth;
    }
}
