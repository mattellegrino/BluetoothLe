package com.example.bluetooth.le.sample;

public class MiBandActivitySample {

    private int timestamp;
    private String deviceId;
    private String userId;
    private int rawIntensity;
    private int steps;
    private int rawKind;
    private int heartRate;

    public MiBandActivitySample() {
    }

    public MiBandActivitySample(int timestamp, String deviceId) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
    }

    public MiBandActivitySample(int timestamp, String deviceId, String userId, int rawIntensity, int steps, int rawKind, int heartRate) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.userId = userId;
        this.rawIntensity = rawIntensity;
        this.steps = steps;
        this.rawKind = rawKind;
        this.heartRate = heartRate;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRawIntensity() {
        return rawIntensity;
    }

    public void setRawIntensity(int rawIntensity) {
        this.rawIntensity = rawIntensity;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getRawKind() {
        return rawKind;
    }

    public void setRawKind(int rawKind) {
        this.rawKind = rawKind;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
