/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    protected HADevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private boolean needsAuth;
    private int mConnectionState = STATE_DISCONNECTED;
    private HashMap<UUID, BluetoothGattCharacteristic> mAvailableCharacteristics = new HashMap<UUID, BluetoothGattCharacteristic>();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_STEPS_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.STEPS);
    public final static UUID UUID_BATTERY_INFO =
            UUID.fromString(SampleGattAttributes.BATTERY_INFO);
    public final static UUID UUID_AUTHENTICATION =
            UUID.fromString(SampleGattAttributes.UUID_CHARACTERISTIC_AUTH);
    public static final UUID UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString((SampleGattAttributes.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION));

    byte[] authKeyBytes = new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45};

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            try{
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                //make auth
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }catch (SecurityException se)
            {
                se.printStackTrace();
                throw se;
            }
    }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            //Controllo se lo stato del device è inizializzato e inizializzo o meno a seconda dello stato

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                //Inserisco characteristics
                gatt.getServices().forEach(s-> s.getCharacteristics().forEach(c->mAvailableCharacteristics.put(c.getUuid(),c)));
                //Inizializzo il dispositivo

                if(getDevice().getDeviceState().compareTo(HADevice.State.INITIALIZING)>=0) {
                    Log.w("LeService","Services discovered, but device state is already" + getDevice().getDeviceState() + "for device" + getDevice().getDeviceName() + "so ignoring");
                    return;
                }
                Log.w("LeService","Services discovered, but device state is already" + getDevice().getDeviceState() + "for device" + getDevice().getDeviceName() + "so ignoring");
                System.out.println("Stato del device" + getDevice().getDeviceState());

                if(needsAuth)
                {

                }
                initializeDevice();


            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }

        }
        public HADevice getDevice() {
            return mDevice;
        }


        public void initializeDevice() {
            new Timer().schedule(new TimerTask() {
                // Inizialize dispositivo
                @Override
                public void run() {
                    BluetoothGattCharacteristic authcharacteristic = getCharacteristic(HuamiService.UUID_CHARACTERISTIC_AUTH);
                    System.out.println("Authentication");
                    //activate notification
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            notifycharacteristic(authcharacteristic);
                        }
                    },500L);

                    if(needsAuth)
                    {
                    getDevice().setDeviceState(HADevice.State.AUTHENTICATING);
                    byte[] sendKey = org.apache.commons.lang3.ArrayUtils.addAll(new byte[]{HuamiService.AUTH_SEND_KEY, 0}, getSecretKey());
                    writeValue(mBluetoothGatt, authcharacteristic, sendKey);}
                    else {
                        getDevice().setDeviceState(HADevice.State.INITIALIZING);
                        assert authcharacteristic != null;
                        writeValue(mBluetoothGatt,authcharacteristic,requestAuthNumber());
                    }
                    // here goes your code to delay
                }
            }, 500L);

        }


        private byte[] requestAuthNumber() {

                return new byte[]{HuamiService.AUTH_REQUEST_RANDOM_AUTH_NUMBER, 0};

        }

        public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
            if(mAvailableCharacteristics.containsKey(uuid))
                return mAvailableCharacteristics.get(uuid);
            return null;
        }

        public void notifycharacteristic(BluetoothGattCharacteristic characteristic) {
            boolean result = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            if (result) {
                BluetoothGattDescriptor notifyDescriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
                if (notifyDescriptor != null) {
                    int properties = characteristic.getProperties();
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        Log.d("Notify","use NOTIFICATION");
                        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        result = mBluetoothGatt.writeDescriptor(notifyDescriptor);
                    } else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                        Log.d(" Notify","use INDICATION");
                        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        result = mBluetoothGatt.writeDescriptor(notifyDescriptor);

                    } else {

                    }
                }
            } else {
                Log.e("Autenthication","Unable to enable notification for " + characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {



                if (UUID_AUTHENTICATION.equals(characteristic.getUuid())) {
                    try {
                        byte[] value = characteristic.getValue();
                        System.out.println(value[0] + " " + value[1] + " " + value[2]);
                        if (value[0] == HuamiService.AUTH_RESPONSE &&
                                value[1] == HuamiService.AUTH_SEND_KEY &&
                                value[2] == HuamiService.AUTH_SUCCESS) {
                            System.out.println("Autentic part 1");
                            //TransactionBuilder builder = createTransactionBuilder("Sending the secret key to the device");
                            writeValue(mBluetoothGatt, characteristic, new byte[]{HuamiService.AUTH_REQUEST_RANDOM_AUTH_NUMBER, 0});

                            //huamiSupport.performImmediately(builder);
                        } else if (value[0] == HuamiService.AUTH_RESPONSE &&
                                (value[1] & 0x0f) == HuamiService.AUTH_REQUEST_RANDOM_AUTH_NUMBER &&
                                value[2] == HuamiService.AUTH_SUCCESS) {

                            byte[] eValue = handleAESAuth(value, getSecretKey());
                            byte[] responseValue = ArrayUtils.addAll(
                                    new byte[]{(byte) (HuamiService.AUTH_SEND_ENCRYPTED_AUTH_NUMBER), 0}, eValue);

                            //ransactionBuilder builder = createTransactionBuilder("Sending the encrypted random key to the device");
                            writeValue(mBluetoothGatt, characteristic, responseValue);
                            System.out.println("Autentic part 2");

                            //huamiSupport.setCurrentTimeWithService(builder);
                            // huamiSupport.performImmediately(builder);
                        } else if (value[0] == HuamiService.AUTH_RESPONSE &&
                                (value[1] & 0x0f) == HuamiService.AUTH_SEND_ENCRYPTED_AUTH_NUMBER &&
                                value[2] == HuamiService.AUTH_SUCCESS) {
                            //TransactionBuilder builder = createTransactionBuilder("Authenticated, now initialize phase 2");
                       /* builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));
                        huamiSupport.enableFurtherNotifications(builder, true);
                        huamiSupport.requestDeviceInfo(builder);
                        huamiSupport.phase2Initialize(builder);
                        huamiSupport.phase3Initialize(builder);
                        huamiSupport.setInitialized(builder);
                        huamiSupport.performImmediately(builder);*/
                            getDevice().setDeviceState(HADevice.State.INITIALIZED);
                            System.out.println("Autenticato");
                        }
                    } catch (Exception e) {

                    }
                }

                final byte[] data = characteristic.getValue();
                int i;
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));

                    if (UUID_STEPS_MEASUREMENT.equals(characteristic.getUuid())) {

                        String finaldata;
                        String value = stringBuilder.toString().replaceAll(" ", "").substring(2, 10);
                        StringBuilder sb = new StringBuilder();
                        for (i = value.length() - 1; i > 0; i -= 2) {
                            sb.append(String.valueOf(value.charAt(i - 1)));
                            sb.append(String.valueOf(value.charAt(i)));
                        }
                        finaldata = sb.toString();
                        System.out.println(Integer.parseInt(finaldata, 16));
                    }
                }

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    protected byte[] getSecretKey() {
        byte[] authKeyBytes = new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45};

        //String authKey = RandomStringUtils.random(16, true, true);
        String authKey = "FQK5uuUZ7TZmOLM1";

            byte[] srcBytes = authKey.trim().getBytes();
            if (authKey.length() == 34 && authKey.substring(0, 2).equals("0x")) {
                srcBytes = hexStringToByteArray(authKey.substring(2));
            }
            System.arraycopy(srcBytes, 0, authKeyBytes, 0, Math.min(srcBytes.length, 16));


        return authKeyBytes;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);


        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            System.out.println("Hear Rate Entrato");
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            int i;
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));

                if(UUID_STEPS_MEASUREMENT.equals(characteristic.getUuid()))
                {
                    String finaldata;
                    String value = stringBuilder.toString().replaceAll(" ","").substring(2,10);
                    StringBuilder sb = new StringBuilder();
                    for (i=value.length()-1; i>0; i-=2)
                    {
                        sb.append(String.valueOf(value.charAt(i-1)));
                        sb.append(String.valueOf(value.charAt(i)));
                    }
                    finaldata = sb.toString();
                    intent.putExtra(EXTRA_DATA, new String(data) + "\n" + Integer.parseInt(finaldata,16));
                }
                else if (UUID_BATTERY_INFO.equals(characteristic.getUuid()))
                {
                   System.out.println("Valore esadecimale Battery Info: " + stringBuilder.toString());

                }
                else
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    protected boolean writeValue(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {

            Log.d("Authentication","writing to characteristic: " + characteristic.getUuid() + ": " + (Arrays.toString(value)));

        if (characteristic.setValue(value)) {
            gatt.writeCharacteristic(characteristic);
        }
        return false;
    }

    private byte[] handleAESAuth(byte[] value, byte[] secretKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        byte[] mValue = Arrays.copyOfRange(value, 3, 19);
        @SuppressLint("GetInstance") Cipher ecipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec newKey = new SecretKeySpec(secretKey, "AES");
        ecipher.init(Cipher.ENCRYPT_MODE, newKey);
        return ecipher.doFinal(mValue);
    }


    @Override
    public IBinder onBind(Intent intent) {

        needsAuth = (boolean) intent.getExtras().get("firstTime");
        this.mDevice = intent.getParcelableExtra(DeviceControlActivity.EXTRAS_DEVICE);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            try {
                if (mBluetoothGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }
            catch(SecurityException se) {
                throw se;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        try {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
        catch (SecurityException se){
            throw se;
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
