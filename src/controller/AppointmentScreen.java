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
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
    ObservableList<String> contactsList = FXCollections.observableArrayList();
    ObservableList<String> customersList = FXCollections.observableArrayList();
    ObservableList<String> apptTimesList = FXCollections.observableArrayList();
    Appointment selectedAppointment;
    private ToggleGroup viewSelection;
    private boolean isViewByWeek;
    private boolean isViewByMonth;
    Timestamp startTimestamp = null;
    Timestamp endTimestamp = null;
    User currentUser;

    public static void passCurrentUserData(User currentUser) {}

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
        } catch (SQLException ex) {
            System.out.println("Error getting all appointments");
        }
    }

    public void saveApptButtonHandler(javafx.event.ActionEvent event) throws SQLException, IOException {
        String apptId;
        String apptTitle = null;
        String apptType = null;
        String apptLocation = null;
        String apptDescription = null;
        Integer apptContact = null;
        Integer apptCustomer = null;
        LocalDateTime start = null;
        LocalDateTime end = null;
        Integer userId = currentUser.getUserId();


        if (apptTypeInput.getText().isEmpty() || apptTitleInput.getText().isEmpty() || apptLocationInput.getText().isEmpty() || apptDescriptionInput.getText().isEmpty() || apptContactComboBox.getSelectionModel().isEmpty() || apptCustomerComboBox.getSelectionModel().isEmpty() || apptDatePicker.getValue() == null || apptStartTimeComboBox.getSelectionModel().isEmpty() || apptEndTimeComboBox.getSelectionModel().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a value/selection for all fields in order to save!");
            a.showAndWait();
        } else {
            apptTitle = apptTitleInput.getText();
            apptType = apptTypeInput.getText();
            apptLocation = apptLocationInput.getText();
            apptDescription = apptDescriptionInput.getText();
            apptContact = Integer.parseInt(apptContactComboBox.getValue().toString());
            apptCustomer = Integer.parseInt(apptCustomerComboBox.getValue().toString());

            LocalDate apptDate = apptDatePicker.getValue();
            LocalTime apptStart = LocalTime.parse(apptStartTimeComboBox.getValue().toString());
            LocalTime apptEnd = LocalTime.parse(apptEndTimeComboBox.getValue().toString());
            start = LocalDateTime.of(apptDate, apptStart);
            end = LocalDateTime.of(apptDate, apptEnd);
        }

        if (!apptIdInput.getText().isEmpty()) {
            apptId = apptIdInput.getText();
            Query.updateAppointment(
                    apptId,
                    apptTitle,
                    apptType,
                    apptLocation,
                    apptDescription,
                    apptContact,
                    apptCustomer,
                    start,
                    end,
                    userId
            );
        } else {
            try {
                Query.addAppointment(
                        apptTitle,
                        apptType,
                        apptLocation,
                        apptDescription,
                        apptContact,
                        apptCustomer,
                        start,
                        end,
                        userId
                );
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

        Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Appointments");
        stage.show();

    }

    public void editApptButtonHandler(ActionEvent actionEvent) {
        selectedAppointment = apptsTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment instanceof Appointment) {
            apptIdInput.setText(selectedAppointment.getAppointmentId());
            apptTypeInput.setText(selectedAppointment.getType());
            apptTitleInput.setText(selectedAppointment.getTitle());
            apptLocationInput.setText(selectedAppointment.getLocation());
            apptDescriptionInput.setText(selectedAppointment.getDescription());
            apptContactComboBox.getSelectionModel().select(selectedAppointment.getContactId());
            apptCustomerComboBox.getSelectionModel().select(selectedAppointment.getCustomerId());
            apptDatePicker.setValue(selectedAppointment.getDate());
            apptStartTimeComboBox.getSelectionModel().select(selectedAppointment.getStartTime());
            apptEndTimeComboBox.getSelectionModel().select(selectedAppointment.getEndTime());
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select an appointment in order to edit it!");
            a.showAndWait();
        }
    }

    public void clearButtonHandler(ActionEvent actionEvent) {
        apptIdInput.clear();
        apptTypeInput.clear();
        apptTitleInput.clear();
        apptLocationInput.clear();
        apptDescriptionInput.clear();
        apptContactComboBox.valueProperty().set(null);
        apptCustomerComboBox.valueProperty().set(null);
        apptDatePicker.setValue(null);
        apptStartTimeComboBox.setValue(null);
        apptEndTimeComboBox.setValue(null);

        apptIdInput.setDisable(false);

        this.selectedAppointment = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apptIdInput.setDisable(true);

        // Prevents observable list from copying on page navs; uses query to generate list of customers and then adds to combo box results
        customersList.clear();
        customersList = Query.getCustomersList();
        apptCustomerComboBox.setItems(customersList);

        // Prevents observable list from copying on page navs; uses query to generate list of contacts and then adds to combo box results
        contactsList.clear();
        contactsList = Query.getContacts();
        apptContactComboBox.setItems(contactsList);
        apptTimesList.clear();

        apptStartTimeComboBox.setItems(Query.getApptStartTimes());
        apptEndTimeComboBox.setItems(Query.getApptEndTimes());

        viewAllAppts();

    }



}
