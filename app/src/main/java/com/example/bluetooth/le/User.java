package com.example.bluetooth.le;

public class User {
    private String ID;
    private String name;
    private String surname;
    private String city;
    private String address;
    private String telephone;
    private String email;
    private String mac_address;


    User(String ID, String name, String surname, String city, String address, String telephone, String email, String mac_address){
        this.ID = ID;
        this.name = name;
        this.surname = surname;
        this.city = city;
        this.address = address;
        this.telephone = telephone;
        this.email = email;
        this.mac_address= mac_address;
    }

    User(String ID, String email){
        this.ID = ID;
        this.name = "";
        this.surname = "";
        this.city = "";
        this.address = "";
        this.telephone = "";
        this.email = email;
        this.mac_address="";
    }
    /** GETTER **/
    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getTelephone() {return telephone; }

    public String getEmail() {
        return email;
    }

    public String getMac_address() { return mac_address; }

    /** SETTER **/
    public void setID(String ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMac_address(String mac_address) { this.mac_address= mac_address;}

}
