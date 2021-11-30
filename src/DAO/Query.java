package DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Contact;
import model.Country;
import model.Customer;
import model.FirstLevelDivision;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static DAO.DatabaseConnection.connection;

public class Query {
    private static ResultSet selection;
    private static String input;
    private static Statement statement;
    static ObservableList<String> startTimes = FXCollections.observableArrayList();

    public static boolean loginAttempt(String username, String password) {
        try{
            DatabaseConnection.openConnection();
            PreparedStatement pst = connection.prepareStatement("SELECT * FROM users WHERE User_Name=? AND Password=?");
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static void updateCustomer(String customerId, String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) {
        try {
            connection.createStatement().executeUpdate(String.format("UPDATE customers"
                            + " SET Customer_Name='%s', Address='%s', Postal_Code='%s', Phone='%s', Last_Update=NOW(), Last_Updated_By='%s', Division_ID='%s'"
                            + " WHERE Customer_ID='%s'",
                    name, address, zip, phone, userId, firstLevelDivisionId, Integer.parseInt(customerId)));
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    public static void addCustomer(String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) throws SQLException {
        connection.createStatement().executeUpdate(String.format("INSERT INTO customers "
                        + "(Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) " +
                        "VALUES ('%s', '%s', '%s', '%s', NOW(), '%s', NOW(), '%s', '%s')",
                name, address, zip, phone, userId, userId, firstLevelDivisionId));
    }

    public static ObservableList<String> getFirstLevelDivisionsList() {
        ObservableList<String> firstLevelDivisionsList = FXCollections.observableArrayList();

        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Division_ID, Division, Country_ID FROM first_level_divisions;");
            while(results.next()) {
                firstLevelDivisionsList.add(results.getString("Division"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return firstLevelDivisionsList;
    }

    public static Integer getFirstLevelDivisionId(String firstLevelDivisionName) throws SQLException {
        String sql = "SELECT * FROM first_level_divisions WHERE Division=?";
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setString(1, firstLevelDivisionName);

        try {
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                FirstLevelDivision newFLD = new FirstLevelDivision(
                        results.getInt("Division_ID"),
                        results.getInt("Country_ID"),
                        results.getString("Division")
                );
                return newFLD.firstLevelDivisionId;
            }
        } catch (Exception ex) {
            System.out.println("Error in getting FLD");
        }
        return null;
    }

    public static Integer getCountryId(String countryName) throws SQLException {
        String sql = "SELECT * FROM countries WHERE Country=?";
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setString(1, countryName);

        try {
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                Country newCountry = new Country(
                        results.getInt("Country_ID"),
                        results.getString("Country")
                );
                return newCountry.countryId;
            }
        } catch (Exception ex) {
            System.out.println("Error in getting country ID");
        }
        return null;
    }

    public static ObservableList<FirstLevelDivision> getFirstLevelDivisionsByCountry(String countryName) throws SQLException {
        Country newCountry = new Country(DAO.Query.getCountryId(countryName), countryName);
        ObservableList<FirstLevelDivision> divisions = FXCollections.observableArrayList();

        String sql = "SELECT * FROM first_level_divisions WHERE COUNTRY_ID=?;";
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setInt(1, newCountry.getCountryId());

        try {
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                FirstLevelDivision newFLD = new FirstLevelDivision(
                        results.getInt("Division_ID"),
                        results.getInt("Country_ID"),
                        results.getString("Division")
                );
                divisions.add(newFLD);
            }
            return divisions;
        } catch (Exception ex) {
            System.out.println("Error in retrieving first level divisions by country name");
            return null;
        }


    }

    public static ObservableList<Customer> getCustomersList() {
        ObservableList<Customer> customersList = FXCollections.observableArrayList();

        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Customer_ID, Customer_Name from customers;");
            while(results.next()) {
                Customer newCust = new Customer(
                        results.getInt("Customer_ID"),
                        results.getString("Customer_Name")
                );
                customersList.add(newCust);
            }
        } catch (Exception exception) {
            System.out.println("Error in getting all customers list");
        }
        return customersList;
    }

    public static ObservableList<String> getCountriesList() {
        ObservableList<String> countriesList = FXCollections.observableArrayList();

        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Country_ID, Country from countries;");
            while(results.next()) {
                countriesList.add(results.getString("Country"));
            }
        } catch (SQLException ex) {
            System.out.println("Error with getting all countries");
        }
        return countriesList;
    }

    public static ObservableList<String> getStartTimes() {
        startTimes.add("08:00");
        startTimes.add("08:30");
        startTimes.add("09:00");
        startTimes.add("09:30");
        startTimes.add("10:00");
        startTimes.add("10:30");
        startTimes.add("11:00");
        startTimes.add("11:30");
        startTimes.add("12:00");
        startTimes.add("12:30");
        startTimes.add("13:00");
        startTimes.add("13:30");
        startTimes.add("14:00");
        startTimes.add("14:30");
        startTimes.add("15:00");
        startTimes.add("15:30");
        startTimes.add("16:00");
        startTimes.add("16:30");
        startTimes.add("17:00");
        startTimes.add("17:30");
        startTimes.add("18:00");
        startTimes.add("18:30");
        startTimes.add("19:00");
        startTimes.add("19:30");
        startTimes.add("20:00");
        startTimes.add("20:30");
        startTimes.add("21:00");
        startTimes.add("21:30");
        return startTimes;
    }

    public static void deleteAppt(String apptId) {
        try {
            connection.createStatement().executeUpdate(String.format("DELETE FROM appointments WHERE Appointment_ID='%s'", apptId));
        } catch (Exception e) {
            System.out.println("Error deleting appointment: " + e.getMessage());
        }
    }

    public static ObservableList<Contact> getContacts() {
        ObservableList<Contact> contactsList = FXCollections.observableArrayList();
        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Contact_ID, Contact_Name, Email from contacts;");
            while(results.next()) {
                Contact newContact = new Contact(
                    results.getInt("Contact_ID"),
                    results.getString("Contact_Name"),
                    results.getString("Email")
                );
                contactsList.add(newContact);
            }
        } catch (Exception exception) {
            System.out.println("Error in getting all contacts list");
        }
        return contactsList;
    }



}
