package com.example.bluetooth.le;

public class User {
    private String ID;
    private String email;
    private String mac_address;
    private String heart_rate;
    private String current_steps;


    User(String ID, String email, String mac_address,String heart_rate,String current_steps){
        this.ID = ID;
        this.email = email;
        this.mac_address= mac_address;
        this.heart_rate=heart_rate;
        this.current_steps=current_steps;
    }

    User(String ID, String email){
        this.ID = ID;
        this.email = email;
        this.mac_address="";
        this.heart_rate="";
        this.current_steps="";
    }
    /** GETTER **/
    public String getID() {
        return ID;
    }

    public String getEmail() {
        return email;
    }

    public String getMac_address() { return mac_address; }

    public String getCurrent_steps() { return current_steps; }

    public String getHeart_rate() { return heart_rate; }

    /** SETTER **/
    public void setID(String ID) {
        this.ID = ID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMac_address(String mac_address) { this.mac_address= mac_address;}

    public void setCurrent_steps(String current_steps) { this.current_steps = current_steps;}

    public void setHeart_rate(String heart_rate) {this.heart_rate = heart_rate;}


}
