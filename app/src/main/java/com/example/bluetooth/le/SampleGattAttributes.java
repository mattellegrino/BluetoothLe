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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    public static final String BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb";
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHARACTERISTIC_DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String STEPS = "00000007-0000-3512-2118-0009af100700";
    public static String BATTERY_INFO = "00000006-0000-3512-2118-0009af100700";
    public static String UUID_CHARACTERISTIC_AUTH = "00000009-0000-3512-2118-0009af100700";
    public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String STEPS_SERVICE = "0000fee0-0000-1000-8000-00805f9b34fb";
    public static String UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0000fee0-0000-1000-8000-00805f9b34fb","Device General Features");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(UUID_CHARACTERISTIC_AUTH, "Authentication");
        attributes.put(UUID_CHARACTERISTIC_DEVICE_NAME,"Device Name");
        attributes.put(STEPS, "Realtime Steps");
        attributes.put(BATTERY_INFO, "Battery Info");
        attributes.put(UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION,"Descriptor Client Characteristic Configuration");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
