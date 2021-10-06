package model;

public class Customer {
    String customerId;
    String name;
    String address;
    String unit;
    String city;
    String zip;
    String country;
    String phoneNumber;

    public Customer() {}

    // Long with generic strings
    public Customer(String customerId, String name, String address, String unit, String city, String zip, String country, String phoneNumber) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.unit = unit;
        this.city = city;
        this.zip = zip;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }

    // Short for simpler needs
    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }


    // Getter & Setter methods
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
