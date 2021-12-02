package DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import model.Country;
import model.FirstLevelDivision;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

    /** checks current user's ID and uses that to search through appointments for that User ID to see if any of them are starting within 15 minutes of user's local time */
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

    public static void updateCustomer(String customerId, String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) {
        try {
            connection.createStatement().executeUpdate(String.format("UPDATE customers"
                            + " SET Customer_Name='%s', Address='%s', Postal_Code='%s', Phone='%s', Last_Update=NOW(), Last_Updated_By='%s', Division_ID='%s'"
                            + " WHERE Customer_ID='%s'",
                    name, address, zip, phone, userId, firstLevelDivisionId, Integer.parseInt(customerId)));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void addCustomer(String name, String address, String zip, String phone, Integer firstLevelDivisionId, Integer userId) throws SQLException {
        connection.createStatement().executeUpdate(String.format("INSERT INTO customers "
                        + "(Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) " +
                        "VALUES ('%s', '%s', '%s', '%s', NOW(), '%s', NOW(), '%s', '%s')",
                name, address, zip, phone, userId, userId, firstLevelDivisionId));
    }

    public static void updateAppointment(String appointmentId, String title, String type, String location, String description, Integer contactId, Integer customerId, LocalDateTime apptStart, LocalDateTime apptEnd, Integer userId) {
        try {
            connection.createStatement().executeUpdate(String.format("Update appointments"
                + " SET Title='%s', Description='%s', Location='%s', Type='%s', Start='%s', End='%s', Last_Update=NOW(), Last_Updated_By='%s', Customer_ID='%s', Contact_ID='%s'"
                + " WHERE Appointment_ID='%s'",
                    title, description, location, type, apptStart, apptEnd, userId, customerId, contactId, appointmentId));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void addAppointment(String title, String type, String location, String description, Integer contactId, Integer customerId, LocalDateTime apptStart, LocalDateTime apptEnd, Integer userId) throws SQLException {
            connection.createStatement().executeUpdate(String.format("INSERT INTO appointments (Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', NOW(), '%s', NOW(), '%s', '%s', '%s', '%s')", title, description, location, type, apptStart, apptEnd, userId, userId, customerId, userId, contactId));
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



    public static void deleteAppt(String apptId) {
        try {
            connection.createStatement().executeUpdate(String.format("DELETE FROM appointments WHERE Appointment_ID='%s'", apptId));
        } catch (Exception e) {
            System.out.println("Error deleting appointment: " + e.getMessage());
        }
    }

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

    public static ObservableList<String> getContacts() {
        ObservableList<String> contactsList = FXCollections.observableArrayList();
        try {
            ResultSet results = connection.createStatement().executeQuery("SELECT Contact_ID, Contact_Name, Email from contacts;");
            while(results.next()) {
                contactsList.add(results.getString("Contact_ID"));
            }
        } catch (Exception exception) {
            System.out.println("Error in getting all contacts list");
        }
        return contactsList;
    }






}
