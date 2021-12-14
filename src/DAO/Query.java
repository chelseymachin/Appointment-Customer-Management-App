package DAO;

import controller.AppointmentScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import model.Appointment;
import model.Country;
import model.FirstLevelDivision;
import model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static DAO.DatabaseConnection.connection;

public class Query {

    /** Takes login form input and returns true if it matches an existing login; returns false if not */
    public static boolean loginAttempt(String username, String password) {
        try{
            DatabaseConnection.openConnection();
            PreparedStatement pst = connection.prepareStatement("SELECT User_Name, Password FROM users WHERE User_Name=? AND Password=?");
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

    /** checks current user's ID and uses that to search through appointments for that User ID to see if any of them are starting within 15 minutes of user's local time then produces alert for that user */
    @FXML public static void checkForUpcomingAppts() {
            try {
                ResultSet apptResults = connection.createStatement().executeQuery(String.format("SELECT Customer_Name, Location, Start FROM customers c INNER JOIN appointments a ON c.Customer_ID=a.Customer_ID INNER JOIN users u ON a.User_ID=u.User_ID WHERE a.User_ID='%s' AND a.Start BETWEEN '%s' AND '%s'", User.getUserId(), LocalDateTime.now(ZoneId.of("UTC")), LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15)));

                while (apptResults.next()) {
                    String  location = apptResults.getString("Location");
                    String name = apptResults.getString("Customer_Name");
                    String apptTimeUTCString = apptResults.getString("Start");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime usersLocalApptTimeLDT = LocalDateTime.parse(apptTimeUTCString, formatter);
                    usersLocalApptTimeLDT = usersLocalApptTimeLDT.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setContentText("You have an appointment with " + name + " starting shortly at " + usersLocalApptTimeLDT.toString().substring(11, 16) + "! Better make your way to " + location + " soon!");
                    a.showAndWait();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
    }

    /** receives LDT (in UDT is expectation) start, end, and customer ID and checks it against the database for any overlaps */
    @FXML public static boolean doesItOverlapAnyExistingApptButItself(LocalDateTime start, LocalDateTime end, Integer customerId, Integer apptId) throws SQLException {
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        String sql = "SELECT Customer_ID, TIME(Start), TIME(End), DATE(Start), Appointment_ID FROM appointments WHERE (? >= TIME(Start) AND ? <= TIME(End)) OR (? <= TIME(Start) AND ? >= TIME(End)) OR (? <= TIME(Start) AND ? >= TIME(Start)) OR (? <= TIME(End) AND ? >= TIME(End));";

        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setString(1, startTime.toString());
        prepared.setString(2, startTime.toString());
        prepared.setString(3, endTime.toString());
        prepared.setString(4, endTime.toString());
        prepared.setString(5, startTime.toString());
        prepared.setString(6, endTime.toString());
        prepared.setString(7, startTime.toString());
        prepared.setString(8, endTime.toString());

        try {
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                if ((results.getInt("Customer_ID") == customerId) && (results.getInt("Appointment_ID") == apptId)) {
                    return false;
                } else if ((results.getInt("Customer_ID") != customerId) && (results.getInt("Appointment_ID") == apptId)) {
                    return false;
                } else if ((results.getInt("Customer_ID") != customerId) && (results.getInt("Appointment_ID") != apptId)) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /** should receive times to check against UTC times in DB to make sure a proposed appointment for a customer doesn't overlap an appointment time on the same date that they already have */
    @FXML public static boolean doesItOverlapCustomersOtherAppointments(LocalDateTime start, LocalDateTime end, Integer customerId) throws SQLException {
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        Boolean itOverlaps = false;

        String sql = "SELECT Customer_ID, TIME(Start), TIME(End), DATE(Start) FROM appointments WHERE (? >= TIME(Start) AND ? <= TIME(End)) OR (? <= TIME(Start) AND ? >= TIME(End)) OR (? <= TIME(Start) AND ? >= TIME(Start)) OR (? <= TIME(End) AND ? >= TIME(End));";
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setString(1, startTime.toString());
        prepared.setString(2, startTime.toString());
        prepared.setString(3, endTime.toString());
        prepared.setString(4, endTime.toString());
        prepared.setString(5, startTime.toString());
        prepared.setString(6, endTime.toString());
        prepared.setString(7, startTime.toString());
        prepared.setString(8, endTime.toString());
        try {
            prepared.execute();
            ResultSet results = prepared.getResultSet();
            while (results.next()) {
                if ((results.getInt("Customer_ID") == customerId) & (results.getString("DATE(Start)") == start.toLocalDate().toString())) {
                    itOverlaps = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return itOverlaps;
    }

    /** Takes input and updates database record of matching customer ID */
    @FXML public static void updateCustomer(String customerId, String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) {
        try {
            connection.createStatement().executeUpdate(String.format("UPDATE customers"
                            + " SET Customer_Name='%s', Address='%s', Postal_Code='%s', Phone='%s', Last_Update=NOW(), Last_Updated_By='%s', Division_ID='%s'"
                            + " WHERE Customer_ID='%s'",
                    name, address, zip, phone, userId, firstLevelDivisionId, Integer.parseInt(customerId)));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /** Takes customer creation parameters and creates next increment customer id and record for customers table in database */
    @FXML public static void addCustomer(String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) throws SQLException {
        connection.createStatement().executeUpdate(String.format("INSERT INTO customers "
                        + "(Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) " +
                        "VALUES ('%s', '%s', '%s', '%s', NOW(), '%s', NOW(), '%s', '%s')",
                name, address, zip, phone, userId, userId, firstLevelDivisionId));
    }

    /** Takes input and updates database record of matching appointment ID */
    @FXML public static void updateAppointment(String appointmentId, String title, String type, String location, String description, Integer contactId, Integer customerId, LocalDateTime apptStart, LocalDateTime apptEnd, Integer userId) {
        try {
            connection.createStatement().executeUpdate(String.format("Update appointments"
                + " SET Title='%s', Description='%s', Location='%s', Type='%s', Start='%s', End='%s', Last_Update=NOW(), Last_Updated_By='%s', Customer_ID='%s', Contact_ID='%s'"
                + " WHERE Appointment_ID='%s'",
                    title, description, location, type, apptStart, apptEnd, userId, customerId, contactId, appointmentId));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /** Takes appointment creation parameters and creates next increment appointment id and record for id on appointments table in database*/
    @FXML public static void addAppointment(String title, String type, String location, String description, Integer contactId, Integer customerId, LocalDateTime apptStart, LocalDateTime apptEnd, Integer userId) throws SQLException {
            connection.createStatement().executeUpdate(String.format("INSERT INTO appointments (Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', NOW(), '%s', NOW(), '%s', '%s', '%s', '%s')", title, description, location, type, apptStart, apptEnd, userId, userId, customerId, userId, contactId));
    }

    /** Returns a list of first level division names that exist in first_level_divisions table for combo box population */
    @FXML public static ObservableList<String> getFirstLevelDivisionsList() {
        ObservableList<String> firstLevelDivisionsList = FXCollections.observableArrayList();

        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Division FROM first_level_divisions;");
            while(results.next()) {
                firstLevelDivisionsList.add(results.getString("Division"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return firstLevelDivisionsList;
    }

    /** takes the name of a first level division/state/province and returns the first level division ID from the first_level_divisions table in the database as an integer */
    @FXML public static Integer getFirstLevelDivisionId(String firstLevelDivisionName) throws SQLException {
        String sql = "SELECT Division_ID, Country_ID, Division FROM first_level_divisions WHERE Division=?";
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

    /** takes the name of a country and returns the country ID from the countries table in database as an integer */
    @FXML public static Integer getCountryId(String countryName) throws SQLException {
        String sql = "SELECT Country_ID, Country FROM countries WHERE Country=?";
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


    /** Takes a country name and selects the corresponding first level divisions list that it's tied to from the database */
    @FXML public static ObservableList<FirstLevelDivision> getFirstLevelDivisionsByCountry(String countryName) throws SQLException {
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

    /** Gets all appointments from the database appointments table and converts them to the user's local timezone before returning them as an observable list of Appointments */
    public static ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
        Connection connection;
        try {
            connection = DatabaseConnection.openConnection();
            ResultSet results = connection.createStatement().executeQuery("SELECT * FROM appointments ORDER BY Start;");
            while (results.next()) {
                // converts all appts to user time so it can be displayed in their timezone
                LocalDateTime apptStartConvertedToUserTime = AppointmentScreen.utcToUsersLDT(results.getTimestamp("Start").toLocalDateTime());
                LocalDateTime apptEndConvertedToUserTime = AppointmentScreen.utcToUsersLDT(results.getTimestamp("End").toLocalDateTime());

                appointmentsList.add(new Appointment(
                        results.getString("Appointment_ID"),
                        results.getString("Customer_ID"),
                        results.getString("Title"),
                        results.getString("Description"),
                        results.getString("Location"),
                        results.getString("Type"),
                        apptStartConvertedToUserTime.toString(),
                        apptStartConvertedToUserTime.toString(),
                        apptEndConvertedToUserTime.toString(),
                        results.getString("User_ID"),
                        results.getString("Contact_ID")));
            }
            return appointmentsList;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }
    
    
    /** Gets all customers from the customers table in database and returns them as an observable list of Strings */
    public static ObservableList<String> getCustomersList() {
        ObservableList<String> customersList = FXCollections.observableArrayList();

        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Customer_ID, Customer_Name from customers;");
            while(results.next()) {
                customersList.add(results.getString("Customer_ID"));
            }
        } catch (Exception exception) {
            System.out.println("Error in getting all customers list");
        }
        return customersList;
    }

    /** Gets all countries from the countries table in database and returns them as an observable list of Strings */
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

    /** Generates a list of all appointment times available and returns it as an obserable list of Strings */
    public static ObservableList<String> getApptTimes() {
        ObservableList<String> apptTimes = FXCollections.observableArrayList();

        for (int i = 1; i < 24; i++ ) {
            if (i < 10) {
                String newAppt1 = LocalTime.parse("0" + i + ":00").toString();
                apptTimes.add(newAppt1);
                String newAppt2 = LocalTime.parse("0" + i + ":15").toString();
                apptTimes.add(newAppt2);
                String newAppt3 = LocalTime.parse("0" + i + ":30").toString();
                apptTimes.add(newAppt3);
                String newAppt4 = LocalTime.parse("0" + i + ":45").toString();
                apptTimes.add(newAppt4);
            } else {
                String newAppt1 = LocalTime.parse(i + ":00").toString();
                apptTimes.add(newAppt1);
                String newAppt2 = LocalTime.parse(i + ":15").toString();
                apptTimes.add(newAppt2);
                String newAppt3 = LocalTime.parse(i + ":30").toString();
                apptTimes.add(newAppt3);
                String newAppt4 = LocalTime.parse(i + ":45").toString();
                apptTimes.add(newAppt4);
            }
        }
            return apptTimes;
    }


    /** Deletes appointment from appointments table in database that has same appt ID as parameter */
    public static void deleteAppt(String apptId) {
        try {
            connection.createStatement().executeUpdate(String.format("DELETE FROM appointments WHERE Appointment_ID='%s'", apptId));
        } catch (Exception e) {
            System.out.println("Error deleting appointment: " + e.getMessage());
        }
    }

    /** uses checkForCustomerAppointments to validate customer selection; if open appointments exist, returns error; if no open appointments, deletes customer from customers table in database */
    public static void deleteCustomer(String customerId) {
        if (checkForCustomerAppointments(customerId)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("You cannot delete a customer that has open appointments!  Please delete this customer's appointments first before trying to delete the customer again.");
            a.showAndWait();
        } else {
            try {
                connection.createStatement().executeUpdate(String.format("DELETE FROM customers WHERE Customer_ID='%s'", customerId));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /** checks appointments table in database for any appointments that are currently open for a customer; returns true if they exist, false otherwise */
    public static Boolean checkForCustomerAppointments(String customerId) {
        Boolean hasAppointments = false;

        try {
            ResultSet results = connection.createStatement().executeQuery(String.format("SELECT * FROM appointments WHERE Customer_ID='%s'", customerId));
            while(results.next()) {
                hasAppointments = true;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return hasAppointments;
    }

    /** Gets all contacts from contacts table in database and returns their ID as an observable list of strings */
    public static ObservableList<String> getContacts() {
        ObservableList<String> contactsList = FXCollections.observableArrayList();
        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Contact_ID from contacts;");
            while(results.next()) {
                contactsList.add(results.getString("Contact_ID"));
            }
        } catch (Exception exception) {
            System.out.println("Error in getting all contacts list");
        }
        return contactsList;
    }
}
