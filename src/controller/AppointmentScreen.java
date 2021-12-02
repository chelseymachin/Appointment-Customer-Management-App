package controller;

import DAO.DatabaseConnection;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
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
    public DatePicker viewAppointmentsDatePicker;
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
    ObservableList<Appointment> appointmentsByMonthObservableList = FXCollections.observableArrayList();
    ObservableList<Appointment> appointmentsByWeekObservableList = FXCollections.observableArrayList();
    ObservableList<String> contactsList = FXCollections.observableArrayList();
    ObservableList<String> customersList = FXCollections.observableArrayList();
    ObservableList<String> apptTimesList = FXCollections.observableArrayList();
    Appointment selectedAppointment;
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

    public void viewByWeek() {
        if (viewAppointmentsDatePicker.getValue() == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a date to view a list of appointments happening during that week!");
            a.showAndWait();
        } else {
            LocalDate selectedDate = viewAppointmentsDatePicker.getValue();
            String selectedYear = selectedDate.toString().substring(0, 4);
            WeekFields weeksList = WeekFields.of(Locale.US);
            Integer weekIndex = selectedDate.get(weeksList.weekOfWeekBasedYear());
            String selectedWeek = Integer.toString(weekIndex);
            Connection connection;
            try {
                appointmentsByWeekObservableList.clear();
                connection = DatabaseConnection.openConnection();
                ResultSet results = connection.createStatement().executeQuery(String.format("SELECT * FROM customers c INNER JOIN appointments a ON c.Customer_ID = a.Customer_ID WHERE WEEK(DATE(Start))+1 = '%s' AND YEAR(Start) = '%s' ORDER BY Start;", selectedWeek, selectedYear));
                while (results.next()) {
                    appointmentsByWeekObservableList.add(new Appointment(
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
                apptsTable.setItems(appointmentsByWeekObservableList);

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

            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void viewByMonth() {
        if (viewAppointmentsDatePicker.getValue() == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a date to view a list of appointments happening during that month!");
            a.showAndWait();
        } else {
            LocalDate selectedDate = viewAppointmentsDatePicker.getValue();
            String selectedMonth = selectedDate.toString().substring(5,7);
            String selectedYear = selectedDate.toString().substring(0,4);
            Connection connection;
            try {
                appointmentsByMonthObservableList.clear();
                connection = DatabaseConnection.openConnection();
                ResultSet results = connection.createStatement().executeQuery(String.format("SELECT * FROM customers c INNER JOIN appointments a ON c.Customer_ID = a.Customer_ID WHERE MONTH(Start) = '%s' AND YEAR(Start) = '%s' ORDER BY Start", selectedMonth, selectedYear));
                while (results.next()) {
                    appointmentsByMonthObservableList.add(new Appointment(
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
                apptsTable.setItems(appointmentsByMonthObservableList);

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
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void viewAllAppts() {
        Connection connection;
        try {
            appointmentsObservableList.clear();
            connection = DatabaseConnection.openConnection();
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

    public static LocalDateTime stringDateToConvertedLDT(String time, String date) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime convertedLDT =  LocalDateTime.parse(date + " " + time + ":00", format).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        return convertedLDT;
    }

    public boolean isItWithinBusinessHours(String selectedDate, String apptStart, String apptEnd) {
        LocalTime businessOpen = LocalTime.parse("07:59");
        LocalTime businessClose = LocalTime.parse("22:01");

        LocalDateTime LDTofStartConverted = stringDateToConvertedLDT(apptStart, selectedDate);
        LocalDateTime LDTofEndConverted = stringDateToConvertedLDT(apptEnd, selectedDate);

        String convertedStartString = LDTofStartConverted.toString().substring(11, 16);
        String convertedEndString = LDTofEndConverted.toString().substring(11, 16);

        LocalTime startStringConvertedToLT = LocalTime.parse(convertedStartString);
        LocalTime endStringConvertedToLT = LocalTime.parse(convertedEndString);

        if(startStringConvertedToLT.isBefore(businessOpen) || endStringConvertedToLT.isAfter(businessClose)) {
            return false;
        } else {
            return true;
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
            String apptDateString = apptDate.toString();

            String apptStart = apptStartTimeComboBox.getValue().toString();
            String apptEnd = apptEndTimeComboBox.getValue().toString();

            start = stringDateToConvertedLDT(apptStart, apptDateString);
            end = stringDateToConvertedLDT(apptEnd, apptDateString);

            if (!isItWithinBusinessHours(apptDateString, apptStart, apptEnd)) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Your appointment is set to begin at " + start.toString().substring(11,16) + " UTC and end at " + end.toString().substring(11,16) + " UTC, which is outside of our regular business hours. Please choose another start or end time!");
                a.showAndWait();
                return;
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
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Appointments");
                stage.show();
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
                    Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                    Scene scene = new Scene(parent);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Appointments");
                    stage.show();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
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
        apptTimesList.setAll(Query.getApptTimes());

        apptStartTimeComboBox.setItems(apptTimesList);
        apptEndTimeComboBox.setItems(apptTimesList);

        // lambda function to set date picker to ONLY allow selections from future dates and non-weekend days
        apptDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate selectedDate, boolean empty) {
                super.updateItem(selectedDate, empty);
                LocalDate currentDate = LocalDate.now();
                setDisable(empty || selectedDate.compareTo(currentDate) < 0);
                if(selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY)
                    setDisable(true);
            }
        });

        viewAllAppts();

    }



}
