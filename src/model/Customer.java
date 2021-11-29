package model;

public class Customer {
    Integer customerId;
    String name;
    String address;
    String unit;
    FirstLevelDivision customerFirstLevelDivision;
    String firstLevelDivisionName;
    Country customerCountry;
    String countryName;
    String zip;
    String country;
    String phoneNumber;
    Integer firstLevelDivisionId;

    public void setCustomerFirstLevelDivision(FirstLevelDivision customerFirstLevelDivision) {
        this.customerFirstLevelDivision = customerFirstLevelDivision;
    }

    public String getFirstLevelDivisionName() {
        return firstLevelDivisionName;
    }

    public void setFirstLevelDivisionName(String firstLevelDivisionName) {
        this.firstLevelDivisionName = firstLevelDivisionName;
    }

    public Integer getFirstLevelDivisionId() { return firstLevelDivisionId; };

    public void setFirstLevelDivisionId(Integer firstLevelDivisionId) {
        this.firstLevelDivisionId = firstLevelDivisionId;
    }

    public Country getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(Country customerCountry) {
        this.customerCountry = customerCountry;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Customer(Integer customerId, String name, String address, FirstLevelDivision customerFirstLevelDivision, Country customerCountry, String zip, String country, String phoneNumber) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.customerFirstLevelDivision = customerFirstLevelDivision;
        this.customerCountry = customerCountry;
        this.zip = zip;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }

    public Customer(Integer customerId, String name, String address, String firstLevelDivisionName, Integer firstLevelDivisionId, String zip, String countryName, String phoneNumber) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.firstLevelDivisionName = firstLevelDivisionName;
        this.firstLevelDivisionId = firstLevelDivisionId;
        this.zip = zip;
        this.countryName = countryName;
        this.phoneNumber = phoneNumber;
    }

    // Short for simpler needs
    public Customer(Integer customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }


    // Getter & Setter methods
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
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

    public FirstLevelDivision getCustomerFirstLevelDivision() {


        return customerFirstLevelDivision;
    }

    public void setFirstLevelDivision(FirstLevelDivision firstLevelDivision) {
        this.customerFirstLevelDivision = customerFirstLevelDivision;
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

    @Override public String toString() {
        return (Integer.toString(customerId) + " - " + name);
    }
}
