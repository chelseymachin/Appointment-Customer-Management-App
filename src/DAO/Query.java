package DAO;

import controller.AppointmentScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.Appointment;
import model.FirstLevelDivision;
import model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static DAO.DatabaseConnection.connection;

public class Query {
    // these are the "initialization" queries, or queries I expect to run at the very start of the program (checking the login attempt and checking to see if whoever is logging in has upcoming appts)

    /**
     * Takes username and password and checks them against the database for a matching record
     *
     * @param username username for attempted login
     * @param password password for attempted login
     * @return true if matching record exists in database; false if not
     */
    public static boolean loginAttempt(String username, String password) {
        try{
            // open connection to DB
            DatabaseConnection.openConnection();

            // prep SQL statement; then insert variables from function input
            String sql = "SELECT User_Name, Password FROM users WHERE User_Name=? AND Password=?";
            PreparedStatement preparedSQL = connection.prepareStatement(sql);
            preparedSQL.setString(1, username);
            preparedSQL.setString(2, password);
            preparedSQL.execute();

            // create result set from query attempt with username and password as input
            ResultSet results = preparedSQL.getResultSet();

            // if result set is positive, returns true/valid login attempt; else returns false
            return results.next();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    /**
     * Checks currently logged in user's ID and uses that to search through appointments in the database for any matching appointments starting within 15 minutes of user's local time, then produces alert to notify
     */
    public static void checkForUpcomingAppts() {
            try {
                // create result set from query attempt with currently logged in user as input
                ResultSet apptResults = connection.createStatement().executeQuery(String.format("SELECT Customer_Name, Location, Start FROM customers c INNER JOIN appointments a ON c.Customer_ID=a.Customer_ID INNER JOIN users u ON a.User_ID=u.User_ID WHERE a.User_ID='%s' AND a.Start BETWEEN '%s' AND '%s'", User.getUserId(), LocalDateTime.now(ZoneId.of("UTC")), LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15)));

                // Loops through appointment results from database and alerts informing user of customer name, local time of appointment, and location
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
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }
    }

    // helper database query functions to validate appointment times against existing appointment records in database

    /**
     * Checks start and end of appointment against others in database with same customer id; includes selected appointment id in order to allow itself to be overwritten if some parameters change (customer, date, etc)
     *
     * @param start start time of appointment as LocalDateTime object
     * @param end end time of appointment as LocalDateTime object
     * @param customerId customer ID as integer
     * @param apptId appointment ID as integer
     * @return true if the appointment data input overlaps an already existing appointment (that's not itself); false if it doesn't overlap any other appointments (but itself)
     */
    public static boolean doesItOverlapAnyExistingApptButItself(LocalDateTime start, LocalDateTime end, Integer customerId, Integer apptId) {
        // converts input LDT/date + time to just a LT/time object
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        try {
            // prep SQL statement; then insert variables from function input
            String sql = "SELECT Customer_ID, TIME(Start), TIME(End), DATE(Start), Appointment_ID FROM appointments WHERE (? >= TIME(Start) AND ? <= TIME(End)) OR (? <= TIME(Start) AND ? >= TIME(End)) OR (? <= TIME(Start) AND ? >= TIME(Start)) OR (? <= TIME(End) AND ? >= TIME(End));";
            PreparedStatement preparedSQL = connection.prepareStatement(sql);
            preparedSQL.setString(1, startTime.toString());
            preparedSQL.setString(2, startTime.toString());
            preparedSQL.setString(3, endTime.toString());
            preparedSQL.setString(4, endTime.toString());
            preparedSQL.setString(5, startTime.toString());
            preparedSQL.setString(6, endTime.toString());
            preparedSQL.setString(7, startTime.toString());
            preparedSQL.setString(8, endTime.toString());
            preparedSQL.execute();
            ResultSet results = preparedSQL.getResultSet();

            // loops through overlapping appointments to check for a few factors
            while (results.next()) {
                // customer ID and Appt ID are the same, then the only appointment overlapping this one is itself, so we can safely save over it
                if ((results.getInt("Customer_ID") == customerId) && (results.getInt("Appointment_ID") == apptId)) {
                    return false;
                }
                // customer id is not the same but the appt id is; that means that the customer has been changed, meaning it no longer overlaps the previous customer's appointment and can safely be saved
                else if ((results.getInt("Customer_ID") != customerId) && (results.getInt("Appointment_ID") == apptId)) {
                    return false;
                }
                // customer id is not the same and appt id is also not; the appt overlaps, but it's for a different customer/appt combo so we can safely save
                else if ((results.getInt("Customer_ID") != customerId) && (results.getInt("Appointment_ID") != apptId)) {
                    return false;
                }
                // any other possibility means an overlap and should return false; an indication that we shouldn't save the appointment details selected
                else {
                    return true;
                }
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return false;
    }

    /**
     * receives start and end of a suggested appointment and checks to see if that overlaps any other appointments for the same customer
     *
     * @param start LocalDateTime object to indicate start date/time of desired appointment
     * @param end LocalDateTime object to indicate end date/time of desired appointment
     * @param customerId customer ID as integer
     * @return true if overlap exists for another appointment with the same customer; false if the appointment start/end don't overlap any other appointment for this customer
     * @throws SQLException throws error if unable to get results from database
     */
    public static boolean doesItOverlapCustomersOtherAppointments(LocalDateTime start, LocalDateTime end, Integer customerId) throws SQLException {
        // converts input LDT/date + time to just a LT/time object
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        // set return variable to false by default
        Boolean itOverlaps = false;

        // prep SQL statement; then insert variables from function input
        try {
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
        prepared.execute();
        ResultSet results = prepared.getResultSet();

        // loops through all appointments that overlap to see if any of them are for the same customer on the same date; if so, returns true
        while (results.next()) {
                if ((results.getInt("Customer_ID") == customerId) & (results.getString("DATE(Start)") == start.toLocalDate().toString())) {
                    itOverlaps = true;
                }
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return itOverlaps;
    }

    /**
     * Checks appointments table in database for any appointments that are currently entered for a customer
     *
     * @param customerId - a string containing the customer's ID #
     * @return true if customer has open appointments; false if no open appointments in database currently
     */
    public static Boolean checkForCustomerAppointments(String customerId) {
        // variable set to false by default
        Boolean hasAppointments = false;

        try {
            // prep SQL statement; then insert variables from function input
            String sql = "SELECT Appointment_ID FROM appointments WHERE Customer_ID=?;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, customerId);
            prepared.execute();
            ResultSet results = prepared.getResultSet();
            while(results.next()) {
                hasAppointments = true;
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return hasAppointments;
    }

    // customer database query functions

    /**
     * uses checkForCustomerAppointments to validate customer selection; if open appointments exist, returns error; if no open appointments, deletes customer from customers table in database
     *
     * @param customerId customer ID as string
     */
    public static void deleteCustomer(String customerId) {
        if (checkForCustomerAppointments(customerId)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("You cannot delete a customer that has open appointments!  Please delete this customer's appointments first before trying to delete the customer again.");
            a.showAndWait();
        } else {
            try {
                // prep SQL statement; then insert variables from function input
                String sql = "DELETE FROM customers WHERE Customer_ID=?;";
                PreparedStatement prepared = connection.prepareStatement(sql);
                prepared.setString(1, customerId);
                prepared.execute();
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    /**
     * Takes input and updates database record of matching customer ID
     *
     * @param customerId customer ID as string
     * @param name customer's name as string
     * @param address customer's street address as string
     * @param zip customer's postal code as string
     * @param phone customer's phone # as string
     * @param firstLevelDivisionId the Integer of the id pertaining to the state/province the customer lives in
     * @param userId the integer of the id of the currently logged in user
     */
    public static void updateCustomer(String customerId, String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) {
        try {
            // prep SQL statement; then insert variables from function input
            String sql = "UPDATE customers SET Customer_Name=?, Address=?, Postal_Code=?, Phone=?, Last_Update=NOW(), Last_Updated_By=?, Division_ID=? WHERE Customer_ID=?;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, name);
            prepared.setString(2, address);
            prepared.setString(3, zip);
            prepared.setString(4, phone);
            prepared.setInt(5, userId);
            prepared.setInt(6, firstLevelDivisionId);
            prepared.setString(7, customerId);
            prepared.execute();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    /**
     * Takes customer creation parameters and creates next increment customer id and record for customers table in database
     *
     * @param name customer's name as string
     * @param address customer's street address as string
     * @param zip customer's postal code as string
     * @param phone customer's phone # as string
     * @param firstLevelDivisionId the Integer of the id pertaining to the state/province the customer lives in
     * @param userId the integer of the id of the currently logged in user
     * @throws SQLException throws error if unable to add record to database
     */
    public static void addCustomer(String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) throws SQLException {
        try {
            // prep SQL statement; then insert variables from function input
            String sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) VALUES (?, ?, ?, ?, NOW(), ?, NOW(), ?, ?);";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, name);
            prepared.setString(2, address);
            prepared.setString(3, zip);
            prepared.setString(4, phone);
            prepared.setInt(5, userId);
            prepared.setInt(6, userId);
            prepared.setInt(7, firstLevelDivisionId);
            prepared.execute();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    // appointment database query functions

    /**
     *  Deletes appointment from appointments table in database that has same appointment ID as parameter
     *
     * @param apptId appointment ID as string
     * @param apptType appointment type as a string; for more info in functionality
     */
    public static void deleteAppt(String apptId, String apptType) {
        try {
            // prep SQL statement; then insert variables from function input
            String sql = "DELETE FROM appointments WHERE Appointment_ID=?;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, apptId);
            prepared.execute();

            // produces alert that confirms the deletion of the appointment given
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Deleted!");
            alert.setContentText("Appt #" + apptId + ", a(n) " + apptType + ", has been deleted from the list.");
            alert.showAndWait();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    /**
     * Takes input and updates database record of matching appointment ID
     *
     * @param appointmentId appointment ID as string
     * @param title appointment title as string
     * @param type appointment type as string
     * @param location appointment location as string
     * @param description description of appointment as string
     * @param contactId contact ID assigned to appointment, as Integer
     * @param customerId customer ID assigned to appointment, as Integer
     * @param apptStart LocalDateTime object of the date/time of appointment start
     * @param apptEnd LocalDateTime object of the date/time of appointment end
     * @param userId the integer of the id of the currently logged in user
     */
    public static void updateAppointment(String appointmentId, String title, String type, String location, String description, Integer contactId, Integer customerId, LocalDateTime apptStart, LocalDateTime apptEnd, Integer userId) {
        try {
            // prep SQL statement; then insert variables from function input
            String sql = "UPDATE appointments SET Title=?, Description=?, Location=?, Type=?, Start=?, End=?, Last_Update=NOW(), Last_Updated_By=?, Customer_ID=?, Contact_ID=? WHERE Appointment_ID=?;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, title);
            prepared.setString(2, description);
            prepared.setString(3, location);
            prepared.setString(4, type);
            prepared.setTimestamp(5, Timestamp.valueOf(apptStart));
            prepared.setTimestamp(6, Timestamp.valueOf(apptEnd));
            prepared.setInt(7, userId);
            prepared.setInt(8, customerId);
            prepared.setInt(9, contactId);
            prepared.setString(10, appointmentId);
            prepared.execute();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    /**
     * Takes appointment creation parameters and creates next increment appointment id and record for id on appointments table in database
     *
     * @param title appointment title as string
     * @param type appointment type as string
     * @param location appointment location as string
     * @param description description of appointment as string
     * @param contactId contact ID assigned to appointment, as Integer
     * @param customerId customer ID assigned to appointment, as Integer
     * @param apptStart LocalDateTime object of the date/time of appointment start
     * @param apptEnd LocalDateTime object of the date/time of appointment end
     * @param userId the integer of the id of the currently logged in user
     * @throws SQLException throws error if unable to add record to the database
     */
    public static void addAppointment(String title, String type, String location, String description, Integer contactId, Integer customerId, LocalDateTime apptStart, LocalDateTime apptEnd, Integer userId) throws SQLException {
        try {
            // prep SQL statement; then insert variables from function input
            String sql = "INSERT INTO appointments (Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, NOW(), ?, ?, ?, ?);";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, title);
            prepared.setString(2, description);
            prepared.setString(3, location);
            prepared.setString(4, type);
            prepared.setTimestamp(5, Timestamp.valueOf(apptStart));
            prepared.setTimestamp(6, Timestamp.valueOf(apptEnd));
            prepared.setInt(7, userId);
            prepared.setInt(8, userId);
            prepared.setInt(9, customerId);
            prepared.setInt(10, userId);
            prepared.setInt(11, contactId);
            prepared.execute();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    // helper query functions that return lists for combo box or table population and organization/filtering of first level divisions and countries

    /**
     * helper function for easy combo box population
     * @return a list of first level division names that exist in the first_level_divisions table of the database
     */
    public static ObservableList<String> getFirstLevelDivisionsList() {
        // empty string list to store the results
        ObservableList<String> firstLevelDivisionsList = FXCollections.observableArrayList();

        try {
            // queries the database to return the name of the division for each record in database list
            ResultSet results = connection.createStatement().executeQuery("SELECT Division FROM first_level_divisions;");

            // loops through resultset and adds a new string to the list for each division name (with the division name)
            while(results.next()) {
                firstLevelDivisionsList.add(results.getString("Division"));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return firstLevelDivisionsList;
    }

    /**
     * helper function for easy combo box population
     * @return a list of country names that exist in the countries table of the database
     */
    public static ObservableList<String> getCountriesList() {
        // empty string list to store the results
        ObservableList<String> countriesList = FXCollections.observableArrayList();

        try {
            // queries the database to return the name of the country for each record in database list
            ResultSet results = connection.createStatement().executeQuery("SELECT Country from countries;");

            // loops through resultset and adds a new string to the list for each country name (with the country name)
            while(results.next()) {
                countriesList.add(results.getString("Country"));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return countriesList;
    }

    /**
     * filters/produces the first level divisions list for a given country name
     * @param countryName name of country you're looking to produce a corresponding first level divisions list for
     * @return a list of first level division objects that match the country ID of the name given in the database
     * @throws SQLException throws error if unable to get results from database
     */
    public static ObservableList<FirstLevelDivision> getFirstLevelDivisionsByCountry(String countryName) throws SQLException {
        // empty list to store results
        ObservableList<FirstLevelDivision> divisions = FXCollections.observableArrayList();

        try {
            // prep SQL statement; then insert variables from function input
            String sql = "SELECT Division_ID, first_level_divisions.Country_ID, Division FROM first_level_divisions INNER JOIN countries ON first_level_divisions.Country_ID = countries.Country_ID WHERE countries.Country=?;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, countryName);
            prepared.execute();

            // store results of statement in ResultSet
            ResultSet results = prepared.getResultSet();

            // loop through results and add a new FirstLevelDivision object for each result in the database
            while (results.next()) {
                FirstLevelDivision newFLD = new FirstLevelDivision(
                        results.getInt("Division_ID"),
                        results.getInt("Country_ID"),
                        results.getString("Division")
                );
                divisions.add(newFLD);
            }
            return divisions;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }

    /**
     * takes the name of a first level division/state/province and returns the first level division ID from the first_level_divisions table in the database as an integer
     * @param firstLevelDivisionName name of first Level division that you want to get the ID for
     * @return the integer ID of the first level division name given
     * @throws SQLException throws error if unable to get results from database
     */
    public static Integer getFirstLevelDivisionId(String firstLevelDivisionName) throws SQLException {
        try {
            // prep SQL statement; then insert variables from function input
            String sql = "SELECT Division_ID, Division FROM first_level_divisions WHERE Division=?";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, firstLevelDivisionName);
            prepared.execute();

            // save results to ResultSet object and return ID
            ResultSet results = prepared.getResultSet();
            return results.getInt("Division_ID");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    /**
     * Gets all customers' IDs from the customers table in database and returns them as an observable list of Strings
     * @return an observable list of customer IDs to populate a combobox easily with
     */
    public static ObservableList<String> getCustomersList() {
        // creates empty observable list to store results in
        ObservableList<String> customersList = FXCollections.observableArrayList();


        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Customer_ID, Customer_Name from customers;");
            // loops through results and adds a new ID to list of customer IDs for each result
            while(results.next()) {
                customersList.add(results.getString("Customer_ID"));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return customersList;
    }

    /**
     * Gets all contacts' IDs from the contacts table in database and returns them as an observable list of Strings
     * @return an observable list of contact IDs to populate a combobox easily with
     */
    public static ObservableList<String> getContacts() {
        // creates empty observable list to store results in
        ObservableList<String> contactsList = FXCollections.observableArrayList();

        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Contact_ID from contacts;");
            // loops through results and adds a new ID to list of contact IDs for each result
            while(results.next()) {
                contactsList.add(results.getString("Contact_ID"));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return contactsList;
    }

    /**
     * Generates a list of all appointment times available and returns it as an obserable list of Strings
     * @return an observable list of appointment times as strings to populate a combobox easily with
     */
    public static ObservableList<String> getApptTimes() {
        // an empty list to store the results in
        ObservableList<String> apptTimes = FXCollections.observableArrayList();

        // loops through 24 hour time period to create appointment times
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

    /**
     * Gets all appointments from the database appointments table and converts them to the user's local timezone before returning them as an observable list of Appointments
     * @return an observable list of appointments from the database that are converted to the user's local timezone
     */
    public static ObservableList<Appointment> getAllAppointments() {
        // an empty list to store the results in
        ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
        try {
            // saves results from query into ResultSet object
            ResultSet results = connection.createStatement().executeQuery("SELECT * FROM appointments ORDER BY Start;");

            // loops through all results to create a new appointment object for each item in records
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
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }
}
