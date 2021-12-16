package controller;

import DAO.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import static DAO.DatabaseConnection.connection;

public class ReportsScreen implements Initializable {
    public TextArea reportTextArea;
    public Button apptsByTypeMonthButton;
    public Button consultantSchedulesButton;
    public Button backButton;
    public Button logoutButton;
    public ComboBox monthsComboBox;
    public ComboBox contactIdsComboBox;
    public ComboBox customerIdsComboBox;

    /**
     * shows a report that displays all the types and totals of appointments for a selected month
     */
    public void reportOneButtonHandler() {
        try {
            if (monthsComboBox.getValue() != null) {
                String selectedMonth = monthsComboBox.getValue().toString();
                reportTextArea.setText(getApptsByTypeMonth(selectedMonth));
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Please select a month to see all appointment types and totals for that month!");
                a.showAndWait();
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    /**
     * shows a report that displays all upcoming appointments for a selected employee/contact
     */
    public void reportTwoButtonHandler() {
        try {
            if (contactIdsComboBox.getValue() != null) {
                String selectedContactId = contactIdsComboBox.getValue().toString();
                reportTextArea.setText(getContactSchedules(selectedContactId));
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Please select a contact ID to see that employee's schedule");
                a.showAndWait();
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    /**
     * shows a report that displays all upcoming appointments for a selected customer
     */
    public void reportThreeButtonHandler() {
        if (customerIdsComboBox.getValue() != null) {
            String selectedCustomerId = customerIdsComboBox.getValue().toString();
            reportTextArea.setText(getApptsByCustomerId(selectedCustomerId));
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a customer ID to see that customer's schedule");
            a.showAndWait();
        }
    }

    /** Function that builds a string based report using StringBuilder object of a selected contact's schedule of appointments in the database
     *
     * @param contactId String input of the selected contact/employee ID
     * @return String based report in the text window
     * @throws SQLException throws error if unable to get results from database
     */
    public String getContactSchedules(String contactId) throws SQLException {
        reportTextArea.clear();
        contactIdsComboBox.getSelectionModel().clearSelection();
        try {
            StringBuilder reportText = new StringBuilder();
            reportText.append("Contact ID: " + contactId + " - Schedule\n...................................................................................................................................................................................................\n" +
                    "Date        |         Start    |    End   |   Appt ID   |    Title     |      Type      |        Description     |     Customer ID\n.......................................................................................................................................................................................................\n");
            String sql = "SELECT * FROM appointments WHERE Contact_ID=? ORDER BY Start;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, contactId);
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                    String date = results.getString("Start").substring(0, 10);
                    String start = results.getString("Start").substring(11, 16);
                    String end = results.getString("End").substring(11, 16);
                    String title = results.getString("Title");
                    String description = results.getString("Description").substring(0, Math.min(results.getString("Description").length(), 15));;
                    String type = results.getString("Type");
                    String customerId = results.getString("Customer_ID");
                    String appointmentId = results.getString("Appointment_ID");

                    reportText.append(date + "   |   " + start + "   |   " + end + "   |   " + appointmentId + "    |  " + title + " | " + type + " | " +  description + " |  " +  customerId + "\n\n");
            }
            return reportText.toString();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return "Oops! Something went wrong!";
        }
    }

    /** Function that builds a string based report using StringBuilder object of a selected customer's schedule of appointments in the database
     *
     * @param customerId String input of the selected customer
     * @return String based report in the text window
     */
    public String getApptsByCustomerId(String customerId) {
        reportTextArea.clear();
        customerIdsComboBox.getSelectionModel().clearSelection();
        try {
            StringBuilder reportText = new StringBuilder();
            reportText.append("Customer ID: " + customerId + " - Schedule\n...................................................................................................................................................................................................\n" +
                    "Date        |         Start    |    End   |   Appt ID   |    Title     |      Type      |        Description     |     Contact ID\n.......................................................................................................................................................................................................\n");

            String sql = "SELECT * FROM appointments WHERE Customer_ID=? ORDER BY Start;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, customerId);
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                    String date = results.getString("Start").substring(0, 10);
                    String start = results.getString("Start").substring(11, 16);
                    String end = results.getString("End").substring(11, 16);
                    String title = results.getString("Title");
                    String description = results.getString("Description").substring(0, Math.min(results.getString("Description").length(), 15));;
                    String type = results.getString("Type");
                    String contactId = results.getString("Contact_ID");
                    String appointmentId = results.getString("Appointment_ID");

                    reportText.append(date + "   |   " + start + "   |   " + end + "   |   " + appointmentId + "    |  " + title + " | " + type + " | " +  description + " |  " +  contactId + "\n\n");
            }
            return reportText.toString();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return "Oops! Something went wrong!";
        }
    }

    /** Function that builds a string based report using StringBuilder object of a selected month's type of appointments and number of those types
     *
     * @param month String input of the selected month
     * @return String based report in the text window
     * @throws SQLException throws error if unable to get results from database
     */
    public String getApptsByTypeMonth(String month) throws SQLException {
        reportTextArea.clear();
        monthsComboBox.valueProperty().set(null);
        monthsComboBox.setPromptText("Month...");

        try {
            StringBuilder reportText = new StringBuilder();
            reportText.append("Number of appointments by type in " + month + "\n" + "\n");
            reportText.append("# of Appts       |        Type \n...........................................................................................................\n");

            String sql = "SELECT Type, COUNT(*) AS NumberOfAppointments FROM appointments WHERE MONTHNAME(Start)=? GROUP BY Type;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setString(1, month);
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                if(results.getInt("NumberOfAppointments") == 0) {
                    reportText.append("We have no appointments in this month of any type!");
                } else {
                    reportText.append(results.getString("NumberOfAppointments") + "    |    " + results.getString("Type") + "\n");
                }
            }
            return reportText.toString();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return "Oops! Something went wrong!";
        }
    }

    /** Takes user back to appointments screen
     *
     * @param event takes javafx event info to get source scene/window
     * @throws IOException throws error if unable to load new screen
     */
    public void backButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Appointments");
        stage.show();
    }

    /**
     * logs out currently logged in user if they approve confirmation pop-up; resets app screen to login screen
     *
     * @param event takes javafx event info to get source scene/window
     * @throws IOException throws error if unable to load new screen
     */
    public void logout(javafx.event.ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Confirm logout");
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Parent parent = FXMLLoader.load(getClass().getResource("/view/loginScreen.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        }
    }

    /** generates a list of months for combobox population
     *
     * @return an Observable list of months as strings
     */
    public static ObservableList<String> getMonths() {
        ObservableList<String> months = FXCollections.observableArrayList();
        months.add("January");
        months.add("February");
        months.add("March");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("August");
        months.add("September");
        months.add("October");
        months.add("November");
        months.add("December");
        return months;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        contactIdsComboBox.setItems(Query.getContacts());
        customerIdsComboBox.setItems(Query.getCustomersList());
        monthsComboBox.setItems(getMonths());
    }
}
