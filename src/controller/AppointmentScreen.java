package controller;

import DAO.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Appointment;
import model.Contact;
import model.Customer;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentScreen implements Initializable {
    public Button editApptButton;
    public TextField apptIdInput;
    public TextField apptTitleInput;
    public TextArea apptDescriptionInput;
    public TextField apptLocationInput;
    public TextField apptTypeInput;
    public ComboBox apptContactComboBox;
    public ComboBox apptCustomerComboBox;
    public DatePicker apptDatePicker;
    public ComboBox apptStartTimeComboBox;
    public ComboBox apptEndTimeComboBox;
    public Button clearButton;
    @FXML private Button saveApptButton;
    @FXML private Button deleteApptButton;
    @FXML private Button customersButton;
    @FXML private Button reportsButton;
    @FXML private Button logoutButton;
    @FXML private AnchorPane apptsScreenPane;
    @FXML private TableView<Appointment> apptsTable;
    @FXML private TableColumn<Appointment, String> apptIdCol;
    @FXML private TableColumn<Appointment, String> titleCol;
    @FXML private TableColumn<Appointment, String> descriptionCol;
    @FXML private TableColumn<Appointment, String> locationCol;
    @FXML private TableColumn<Appointment, String> contactCol;
    @FXML private TableColumn<Appointment, String> typeCol;
    @FXML private TableColumn<Appointment, String> dateCol;
    @FXML private TableColumn<Appointment, String> startTimeCol;
    @FXML private TableColumn<Appointment, String> endTimeCol;
    @FXML private TableColumn<Appointment, String> customerIdCol;
    @FXML private TableColumn<Appointment, String> userIdCol;
    ObservableList<Appointment> appointmentsObservableList = FXCollections.observableArrayList();
    ObservableList<Contact> contactsList = FXCollections.observableArrayList();
    ObservableList<Customer> customersList = FXCollections.observableArrayList();
    Appointment selectedAppointment;
    Stage stage;
    Timestamp startTimestamp = null;
    Timestamp endTimestamp = null;
    User currentUser;

    public static void passCurrentUserData(User currentUser) {
    }

    @FXML public void logout(javafx.event.ActionEvent event) throws IOException {
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

    @FXML public void deleteApptButtonHandler(javafx.event.ActionEvent event) throws IOException{
        selectedAppointment = apptsTable.getSelectionModel().getSelectedItem();

        if(selectedAppointment instanceof Appointment) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Are you sure you want to delete appointment #" + selectedAppointment.getAppointmentId() + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Query.deleteAppt(selectedAppointment.getAppointmentId());

                System.out.println("Deleted appt! Reload!");
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }

        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select an appointment in order to delete it!");
            a.showAndWait();
        }
    }

    @FXML public void customersButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/customersScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Customers");
        stage.show();
    }

    @FXML public void reportsButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/reportsScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Reports");
        stage.show();
    }

    public void viewAllAppts() {
        Connection connection;
        try {
            appointmentsObservableList.clear();
            connection = DAO.DatabaseConnection.openConnection();
            ResultSet results = connection.createStatement().executeQuery("SELECT * FROM appointments, customers, users, contacts WHERE appointments.User_ID = users.User_ID AND appointments.Contact_ID = contacts.Contact_ID AND appointments.Customer_ID = customers.Customer_ID ORDER BY Start;");
            while (results.next()) {
                appointmentsObservableList.add(new Appointment(
                        results.getString("Appointment_ID"),
                        results.getString("Customer_ID"),
                        results.getString("Title"),
                        results.getString("Description"),
                        results.getString("Location"),
                        results.getString("Type"),
                        results.getString("Start"),
                        results.getString("Start"),
                        results.getString("End"),
                        results.getString("User_ID"),
                        results.getString("Contact_ID")));
            }
            apptsTable.setItems(appointmentsObservableList);
        } catch (SQLException ex) {
            System.out.println("Error getting all appointments");
        }
    }

    public void saveApptButtonHandler(javafx.event.ActionEvent event) throws SQLException, IOException {
        Connection connection;
        connection = DAO.DatabaseConnection.openConnection();

        PreparedStatement prepared = null;
        String sql = null;
        String apptType = apptTypeInput.getText();
        String apptTitle = apptTitleInput.getText();
        String apptLocation = apptLocationInput.getText();
        String apptDescription = apptDescriptionInput.getText();
        String apptContact = apptContactComboBox.getValue().toString();
        String apptCustomer = apptCustomerComboBox.getValue().toString();
        // add a customer contact here that mirrors above (might need new combo box for just customer IDs)
        LocalDate apptDate = apptDatePicker.getValue();
        LocalDateTime apptStart = LocalDateTime.of(apptDate, LocalTime.parse(apptStartTimeComboBox.getValue().toString().substring(0,5)));
        startTimestamp = Timestamp.valueOf(apptStart);
        LocalDateTime apptEnd = LocalDateTime.of(apptDate, LocalTime.parse(apptEndTimeComboBox.getValue().toString().substring(0, 5)));
        endTimestamp = Timestamp.valueOf(apptEnd);
        Integer userId = currentUser.getUserId();

        if (!apptIdInput.getText().isEmpty()) {
            sql = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Update = NOW(), Last_Updated_By = ?, Contact_ID = ? WHERE Appointment_ID = ?";
            prepared = connection.prepareStatement(sql);
            prepared.setString(1, apptTitle);
            prepared.setString(2, apptDescription);
            prepared.setString(3, apptLocation);
            prepared.setString(4, apptType);
            prepared.setTimestamp(5, startTimestamp);
            prepared.setTimestamp(6, endTimestamp);
            prepared.setInt(7, userId);
            prepared.setString(8, apptContact);
            prepared.setInt(9, Integer.parseInt(this.apptIdInput.getText()));

            int result = prepared.executeUpdate();
            if (result > 0) {
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Appointments");
                stage.show();
            } else {
                System.out.println("No update happened!");
            }
        } else {
            sql = "INSERT INTO appointments (Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, User_ID, Contact_ID, Customer_ID) VALUES  (?, ?, ?, ?, ?, ?, NOW(), ?, NOW(), ?, ?, ?, ?)";
            prepared = connection.prepareStatement(sql);
            prepared.setString(1, apptTitle);
            prepared.setString(2, apptDescription);
            prepared.setString(3, apptLocation);
            prepared.setString(4, apptType);
            prepared.setTimestamp(5, startTimestamp);
            prepared.setTimestamp(6, endTimestamp);
            prepared.setInt(7, userId);
            prepared.setInt(8, userId);
            prepared.setInt(9, Integer.valueOf(userId));
            prepared.setString(10, apptContact);
            prepared.setInt(11, Integer.valueOf(apptCustomer));

            int result = prepared.executeUpdate();
            if (result > 0) {
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Appointments");
                stage.show();
            } else {
                System.out.println("No appts were made!");
            }

        }

    }

    public void editApptButtonHandler(ActionEvent actionEvent) {
        selectedAppointment = apptsTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment instanceof Appointment) {
            apptIdInput.setText(selectedAppointment.getAppointmentId());
            apptTypeInput.setText(selectedAppointment.getType());
            apptTitleInput.setText(selectedAppointment.getTitle());
            apptLocationInput.setText(selectedAppointment.getLocation());
            apptDescriptionInput.setText(selectedAppointment.getDescription());
            apptContactComboBox.setValue(selectedAppointment.getContactId());
            apptDatePicker.setValue(selectedAppointment.getDate());
            apptStartTimeComboBox.setValue(selectedAppointment.getStartTime());
            apptEndTimeComboBox.setValue(selectedAppointment.getEndTime());
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select an appointment in order to delete it!");
            a.showAndWait();
        }
    }

    public void clearButtonHandler(ActionEvent actionEvent) {
        apptIdInput.clear();
        apptTypeInput.clear();
        apptTitleInput.clear();
        apptLocationInput.clear();
        apptDescriptionInput.clear();
        apptContactComboBox.setValue(null);
        apptDatePicker.setValue(null);
        apptStartTimeComboBox.setValue(null);
        apptEndTimeComboBox.setValue(null);

        apptIdInput.setDisable(false);

        this.selectedAppointment = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apptIdInput.setDisable(true);
        customersList.clear();
        customersList = DAO.Query.getCustomersList();
        contactsList.clear();
        contactsList = DAO.Query.getContacts();


        apptStartTimeComboBox.setItems(DAO.Query.getStartTimes());
        apptContactComboBox.setItems(contactsList);
        apptCustomerComboBox.setItems(customersList);
        apptEndTimeComboBox.setItems(DAO.Query.getStartTimes());
        viewAllAppts();

        apptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactId"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
    }



}
