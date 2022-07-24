package com.example.bluetooth.le;

import com.example.bluetooth.le.sample.MiBandActivitySample;

public class User {
    private String ID;
    private String email;
    private String mac_address;
    private String current_steps;
    private String current_heart_rate;
    private HADevice haDevice;
    //private MiBandActivitySample miBandActivitySample;


    User(String ID, String email){
        this.ID = ID;
        this.email = email;
        this.mac_address="";
        this.current_steps="";
        this.current_heart_rate="";
        //HADevice viene istanziato in devicecontrolactivity
        //capire il ruolo di MiBandActivitySample, per ora lo menziono solo ma dovremmo utilizzarlo

    }

    /** GETTER **/
    public String getID() {
        return ID;
    }

    public String getEmail() {
        return email;
    }

    public String getMac_address() { return mac_address; }

    public HADevice getHaDevice() {
        return haDevice;
    }

    public String getCurrent_heart_rate() {
        return current_heart_rate;
    }

    public String getCurrent_steps() {
        return current_steps;
    }


    /** SETTER **/
    public void setID(String ID) {
        this.ID = ID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMac_address(String mac_address) { this.mac_address= mac_address;}

    public void setHaDevice(HADevice haDevice) {
        this.haDevice = haDevice;
    }


    /*
    public void setMiBandActivitySample(MiBandActivitySample miBandActivitySample) {
        this.miBandActivitySample = miBandActivitySample;
    }
    public MiBandActivitySample getMiBandActivitySample() {
        return miBandActivitySample;
    }
     */

    public void setCurrent_steps(String current_steps) {
        this.current_steps = current_steps;
    }

    public void setCurrent_heart_rate(String current_heart_rate) {
        this.current_heart_rate = current_heart_rate;
    }
}
