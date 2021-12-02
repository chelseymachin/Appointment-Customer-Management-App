package controller;

import DAO.DatabaseConnection;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportsScreen implements Initializable {


    public TextArea reportTextArea;
    public Button apptsByTypeMonthButton;
    public Button consultantSchedulesButton;
    public Button backButton;
    public Button logoutButton;
    public ComboBox monthsComboBox;
    public ComboBox contactIdsComboBox;
    public ComboBox customerIdsComboBox;

    public void reportOneButtonHandler(javafx.event.ActionEvent event) throws SQLException {
        if (monthsComboBox.getValue() != null) {
            String selectedMonth = monthsComboBox.getValue().toString();
            reportTextArea.setText(getApptsByTypeMonth(selectedMonth));
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a month to see all appointment types and totals for that month!");
            a.showAndWait();
        }
    }

    public void reportTwoButtonHandler(javafx.event.ActionEvent event) throws SQLException {
        if (contactIdsComboBox.getValue() != null) {
            String selectedContactId = contactIdsComboBox.getValue().toString();
            reportTextArea.setText(getContactSchedules(selectedContactId));
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a contact ID to see that employee's schedule");
            a.showAndWait();
        }
    }

    public void reportThreeButtonHandler(javafx.event.ActionEvent event) {
        if (customerIdsComboBox.getValue() != null) {
            String selectedCustomerId = customerIdsComboBox.getValue().toString();
            reportTextArea.setText(getApptsByCustomerId(selectedCustomerId));
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a customer ID to see that customer's schedule");
            a.showAndWait();
        }
    }

    public String getContactSchedules(String contactId) throws SQLException {
        reportTextArea.clear();
        contactIdsComboBox.getSelectionModel().clearSelection();
        Connection connection = DatabaseConnection.openConnection();

        try {
            StringBuilder reportText = new StringBuilder();

            reportText.append("Contact ID: " + contactId + " - Schedule\n...................................................................................................................................................................................................\n" +
                    "Date        |         Start    |    End   |   Appt ID   |    Title     |      Type      |        Description     |     Customer ID\n.......................................................................................................................................................................................................\n");
            ResultSet results = connection.createStatement().executeQuery(String.format("SELECT *, COUNT(*) as NumberOfAppointments FROM appointments a INNER JOIN customers c ON a.Customer_ID=c.Customer_ID WHERE Contact_ID='%s' ORDER BY Start;", contactId));

            while (results.next()) {
                if (results.getInt("NumberOfAppointments") == 0) {
                    reportText.append("There's no scheduled appointments for contact ID " + contactId);
                } else {
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
            }
            return reportText.toString();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Error in generating contact schedules report");
            return "Oops! Something went wrong!";
        }
    }

    public String getApptsByCustomerId(String customerId) {
        reportTextArea.clear();
        customerIdsComboBox.getSelectionModel().clearSelection();
        Connection connection = DatabaseConnection.openConnection();

        try {
            StringBuilder reportText = new StringBuilder();
            reportText.append("Customer ID: " + customerId + " - Schedule\n...................................................................................................................................................................................................\n" +
                    "Date        |         Start    |    End   |   Appt ID   |    Title     |      Type      |        Description     |     Contact ID\n.......................................................................................................................................................................................................\n");
            ResultSet results = connection.createStatement().executeQuery(String.format("SELECT *, COUNT(*) as NumberOfAppointments FROM appointments a INNER JOIN customers c ON a.Customer_ID=c.Customer_ID WHERE a.Customer_ID='%s' ORDER BY Start;", customerId));

            while (results.next()) {
                if (results.getInt("NumberOfAppointments") == 0) {
                    reportText.append("There's no scheduled appointments for customer ID " + customerId);
                } else {
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
            }
            return reportText.toString();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Error in generating customer schedules report");
            return "Oops! Something went wrong!";
        }
    }

    public String getApptsByTypeMonth(String month) throws SQLException {
        reportTextArea.clear();
        monthsComboBox.valueProperty().set(null);
        monthsComboBox.setPromptText("Month...");
        Connection connection = DatabaseConnection.openConnection();

        try {
            StringBuilder reportText = new StringBuilder();
            reportText.append("Number of appointments by type in " + month + "\n" + "\n");
            reportText.append("# of Appts       |        Type \n...........................................................................................................\n");

            ResultSet results = connection.createStatement().executeQuery(String.format("SELECT Type, COUNT(*) as NumberOfAppointments FROM appointments WHERE MONTHNAME(Start)='%s'", month));
            while (results.next()) {
                if(results.getInt("NumberOfAppointments") == 0) {
                    reportText.append("We have no appointments in this month of any type!");
                } else {
                    reportText.append(results.getString("NumberOfAppointments") + "    |    " + results.getString("Type") + "\n");
                }
            }
            return reportText.toString();
        } catch (Exception ex) {
            System.out.println("Error in generating appts report by type for " + month);
            return "Oops! Something went wrong!";
        }
    }

    public void backButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Appointments");
        stage.show();
    }

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
