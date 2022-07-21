package com.example.bluetooth.le;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class HealthApplication extends Application {
    private User user;

    /**
     * Main Application class that initializes and provides access to certain things like
     * logging and DB access.
     */
    // Since this class must not log to slf4j, we use plain android.util.Log
    private static final String TAG = "HealthApplication";
    public static final String DATABASE_NAME = "HealthApp";

    private static HealthApplication context;
    private static final Lock dbLock = new ReentrantLock();
    //private static DeviceService deviceService;
    private static SharedPreferences sharedPrefs;
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String PREFS_VERSION = "shared_preferences_version";
    //if preferences have to be migrated, increment the following and add the migration logic in migratePrefs below; see http://stackoverflow.com/questions/16397848/how-can-i-migrate-android-preferences-with-a-new-version
    private static final int CURRENT_PREFS_VERSION = 10;

    private static HealthApplication app;
    public static final String ACTION_QUIT
            = "com.example.bluetooth.le.action.quit";
    public static final String ACTION_LANGUAGE_CHANGE = "com.example.bluetooth.le.action.language_change";
    public static final String ACTION_NEW_DATA = "com.example.bluetooth.le.action.new_data";


    public static void quit() {
        Intent quitIntent = new Intent(HealthApplication.ACTION_QUIT);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(quitIntent);
        //HealthApplication.deviceService().quit();
        System.exit(0);
    }

    private static Locale language;
    public HealthApplication() {
        context = this;

        // don't do anything here, add it to onCreate instead
    }

    public static SharedPreferences getDeviceSpecificSharedPrefs(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isEmpty()) {
            return null;
        }
        return context.getSharedPreferences("devicesettings_" + deviceIdentifier, Context.MODE_PRIVATE);
    }

    public static void deleteDeviceSpecificSharedPrefs(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isEmpty()) {
            return;
        }
        context.getSharedPreferences("devicesettings_" + deviceIdentifier, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static HealthApplication app() {
        return app;
    }

    public static void setSharedPrefs(SharedPreferences sharedPrefs) {
        HealthApplication.sharedPrefs = sharedPrefs;
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

    public static SharedPreferences getSharedPrefs() {
        return sharedPrefs;
    }

    public static HealthApplication getApp() {
        return app;
    }

    public static Locale getLanguage() {
        return language;
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
}
