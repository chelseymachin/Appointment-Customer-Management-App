package controller;

import DAO.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import static DAO.DatabaseConnection.connection;

public class AppointmentScreen implements Initializable {
    @FXML public Button editApptButton;
    @FXML public TextField apptIdInput;
    @FXML public TextField apptTitleInput;
    @FXML public TextArea apptDescriptionInput;
    @FXML public TextField apptLocationInput;
    @FXML public TextField apptTypeInput;
    @FXML public ComboBox apptContactComboBox;
    @FXML public ComboBox apptCustomerComboBox;
    @FXML public DatePicker apptDatePicker;
    @FXML public DatePicker viewAppointmentsDatePicker;
    @FXML public ComboBox apptStartTimeComboBox;
    @FXML public ComboBox apptEndTimeComboBox;
    @FXML public Button clearButton;
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
    ObservableList<Customer> customersList = FXCollections.observableArrayList();
    ObservableList<String> apptTimesList = FXCollections.observableArrayList();
    Appointment selectedAppointment;
    User currentUser;

    /**
     * used to pass the user data about the user currently logged in from the login screen
     * @param currentUser accepts a User object of (preferably) the currently logged in user from the login screen
     */
    public static void passCurrentUserData(User currentUser) {
    }

    // button handlers for appointment screen buttons
    /**
     * logs out currently logged in user if they approve confirmation pop-up; resets app screen to login screen
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException - throws error if unable to load new screen
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

    /**
     * when delete appt button is pressed, checks to make sure there is a valid appointment selected (alerts otherwise) and then uses query function to delete the selected appt and refresh the appointment screen/table
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     */
    public void deleteApptButtonHandler(javafx.event.ActionEvent event) throws IOException {
        selectedAppointment = apptsTable.getSelectionModel().getSelectedItem();

        if(selectedAppointment instanceof Appointment) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Are you sure you want to delete appointment #" + selectedAppointment.getAppointmentId() + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Query.deleteAppt(selectedAppointment.getAppointmentId(), selectedAppointment.getType());
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

    /**
     * goes to Customers Screen
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     */
    public void customersButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/customersScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Customers");
        stage.show();
    }

    /**
     * goes to Reports Screen
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     */
    public void reportsButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/reportsScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Reports");
        stage.show();
    }

    // handlers for view change buttons on appointment screen
    /**
     * checks to make sure datePicker has a selection, then filters all appts to only show those within the same week of the selected date
     */
    public void viewByWeek() {
        if (viewAppointmentsDatePicker.getValue() == null) {
            // if there's no date picked in the datepicker, an alert is presented to user
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a date to view a list of appointments happening during that week!");
            a.showAndWait();
        } else {
            // gets date and year from datepicker element
            LocalDate selectedDate = viewAppointmentsDatePicker.getValue();

            try {
                // clears the current observable list set for appointments by week view (just in case a previous has been selected)
                appointmentsByWeekObservableList.clear();

                // prep SQL statement and insert string input from week selection
                String sql = "SELECT * FROM appointments WHERE YEARWEEK(Start)=YEARWEEK(?);";
                PreparedStatement prepared = connection.prepareStatement(sql);
                prepared.setString(1, String.valueOf(selectedDate));
                prepared.execute();
                ResultSet results = prepared.getResultSet();

                // loops through results and adds an appointment object to the view's observable list for each record that matches the input week selected
                while (results.next()) {
                    // converts all appts to user time so it can be displayed in their timezone
                    LocalDateTime apptStartConvertedToUserTime = utcToUsersLDT(results.getTimestamp("Start").toLocalDateTime());
                    LocalDateTime apptEndConvertedToUserTime = utcToUsersLDT(results.getTimestamp("End").toLocalDateTime());

                    appointmentsByWeekObservableList.add(new Appointment(
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
            } catch(SQLException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    /**
     * checks to make sure datePicker has a selection, then filters all appts to only show those within the same month of the selected date
     */
    public void viewByMonth() {
        if (viewAppointmentsDatePicker.getValue() == null) {
            // if there's no date picked in the datepicker, an alert is presented to user
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a date to view a list of appointments happening during that month!");
            a.showAndWait();
        } else {
            // gets date and year from datepicker element
            LocalDate selectedDate = viewAppointmentsDatePicker.getValue();

            try {
                // clears the current observable list set for appointments by week view (just in case a previous has been selected)
                appointmentsByMonthObservableList.clear();

                // prep SQL statement and insert string input from week selection
                String sql = "SELECT * FROM appointments WHERE MONTHNAME(Start)=MONTHNAME(?);";
                PreparedStatement prepared = connection.prepareStatement(sql);
                prepared.setString(1, String.valueOf(selectedDate));
                prepared.execute();
                ResultSet results = prepared.getResultSet();

                // loops through results and adds an appointment object to the view's observable list for each record that matches the input week selected
                while (results.next()) {
                    // converts all appts to user time so it can be displayed in their timezone
                    LocalDateTime apptStartConvertedToUserTime = utcToUsersLDT(results.getTimestamp("Start").toLocalDateTime());
                    LocalDateTime apptEndConvertedToUserTime = utcToUsersLDT(results.getTimestamp("End").toLocalDateTime());

                    appointmentsByMonthObservableList.add(new Appointment(
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
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    // helper functions that help convert timezones within the application
    /**
     * converts a generic string time and date to a LocalDateTime object
     * @param time accepts a time in a string format, preferably xx:xx:xx
     * @param date accepts a date in a string format, preferably xxxx-xx-xx
     * @return LocalDateTime object of the given string date and time; format is easy to move around and convert for timezone functionality and database records
     */
    public static LocalDateTime stringToLDTConverter(String time, String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(date + " " + time + ":00", formatter);
        return ldt;
    }

    /**
     * converts the user system's LocalDateTime to UTC timezone - this is to save the appointment to the database with the correct time
     * @param usersLocalDateTime the LocalDateTime object that needs to be converted to UTC
     * @return LocalDateTime object converted to UTC timezone
     */
    public static LocalDateTime usersLDTToUTC(LocalDateTime usersLocalDateTime) {
        return usersLocalDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }

    /**
     * converts the user system's LocalDateTime to EST timezone - this is to ensure the appointment falls within business hours (which are in EST)
     * @param usersLocalDateTime the LocalDateTime object that needs to be converted to EST
     * @return LocalDateTime object converted to EST timezone
     */
    public static LocalDateTime usersLDTToEST(LocalDateTime usersLocalDateTime) {
        return usersLocalDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("EST", ZoneId.SHORT_IDS)).toLocalDateTime();
    }

    /**
     * converts a UTC format LocalDateTime instance to the user system's LocalDateTime - this is for displaying all appts correctly in user timezone
     * @param utcLocalDateTime the LocalDateTime object in a UTC timezone that needs to be converted to the user's local timezone
     * @return LocalDateTime object converted to the user's local timezone timezone
     */
    public static LocalDateTime utcToUsersLDT(LocalDateTime utcLocalDateTime) {
        return utcLocalDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * checks to see if the start and end LocalDateTime that are input are within the stated business hours (8 - 22 EST)
     * @param start LocalDateTime object of start time of appt (in pre-converted EST is expectation)
     * @param end LocalDateTime object of end time of appt (in pre-converted EST is expectation)
     * @return if start and end fall within EST business hours (8 - 22), then returns true; otherwise returns false
     */
    public boolean isItWithinBusinessHours(LocalDateTime start, LocalDateTime end) {
        LocalTime startLDTconvertedToLT = LocalTime.parse(start.toString().substring(11, 16));
        LocalTime endLDTConvertedToLT = LocalTime.parse(end.toString().substring(11, 16));
        LocalTime openLT = LocalTime.of(07, 59);
        LocalTime closeLT = LocalTime.of(22, 01);

        if(startLDTconvertedToLT.isBefore(openLT) || endLDTConvertedToLT.isAfter(closeLT)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * validates appointment data and saves or rejects it based on validation results
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     */
    public void saveApptButtonHandler(javafx.event.ActionEvent event) throws IOException {
        // creating empty variables for all data
        String apptTitle = null;
        String apptType = null;
        String apptLocation = null;
        String apptDescription = null;
        Integer apptContact = null;
        Integer apptCustomer = null;
        LocalDateTime startLocal = null;
        LocalDateTime startEST = null;
        LocalDateTime startUTC = null;
        LocalDateTime endLocal = null;
        LocalDateTime endEST = null;
        LocalDateTime endUTC = null;
        Integer userId = currentUser.getUserId();

        // checks to see if any input areas are empty; if so, gives an error
        if (apptTypeInput.getText().isEmpty() || apptTitleInput.getText().isEmpty() || apptLocationInput.getText().isEmpty() || apptDescriptionInput.getText().isEmpty() || apptContactComboBox.getSelectionModel().isEmpty() || apptCustomerComboBox.getSelectionModel().isEmpty() || apptDatePicker.getValue() == null || apptStartTimeComboBox.getSelectionModel().isEmpty() || apptEndTimeComboBox.getSelectionModel().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a value/selection for all fields in order to save!");
            a.showAndWait();
        } else {
            // saves all the input strings/data to the empty variables from before
            apptTitle = apptTitleInput.getText();
            apptType = apptTypeInput.getText();
            apptLocation = apptLocationInput.getText();
            apptDescription = apptDescriptionInput.getText();
            apptContact = Integer.parseInt(apptContactComboBox.getValue().toString());
            apptCustomer = Integer.parseInt(apptCustomerComboBox.getValue().toString());
            LocalDate apptDate = apptDatePicker.getValue();
            String apptDateString = apptDate.toString();

            // gets appt start date from combobox as string, converts it to LocalDateTime object, then converts it to EST and UTC to use for more validation below
            String apptStart = apptStartTimeComboBox.getValue().toString();
            startLocal = stringToLDTConverter(apptStart, apptDateString);
            startEST = usersLDTToEST(startLocal);
            startUTC = usersLDTToUTC(startLocal);

            // gets appt end date from combobox as string, converts it to LocalDateTime object, then converts it to EST and UTC to use for more validation below
            String apptEnd = apptEndTimeComboBox.getValue().toString();
            endLocal = stringToLDTConverter(apptEnd, apptDateString);
            endEST = usersLDTToEST(endLocal);
            endUTC = usersLDTToUTC(endLocal);

            // if appt ID is filled in, the user is attempting to edit an existing appt; so we will act accordingly below
            if (!apptIdInput.getText().isEmpty()) {
                // passes pre-converted EST times of start/end of appt to see if proposed appt time is within EST business hours; if it is not, gives error and exits
                if (!isItWithinBusinessHours(startEST, endEST)) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("Your appointment is set to begin at " + startEST.toString().substring(11, 16) + " EST and end at " + endEST.toString().substring(11, 16) + " EST, which is outside of our regular business hours (08:00 TO 22:00 EST). Please choose another start or end time!");
                    a.showAndWait();
                    return;
                }
                // if end of appt is before the start of the appt, give error and exit
                else if (endLocal.isBefore(startLocal)) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("The end time you've selected is before your start time!  Better check that for accuracy, partner!");
                    a.showAndWait();
                    return;
                }
                // uses query function to check if appt only overlaps itself; if it overlaps any other appt but itself, gives error and exits
                else if (Query.doesItOverlapAnyExistingApptButItself(startUTC, endUTC, Integer.parseInt(apptCustomerComboBox.getValue().toString()), Integer.parseInt(apptIdInput.getText()))) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("Your appointment collides with an appointment that already exists for this customer!  Please choose a new date or time and try again!");
                    a.showAndWait();
                    return;
                }
                // if it passes all validation above, the appt ID is updated in the record with all the new details; times are recorded in UTC
                else {
                    Query.updateAppointment(
                            apptIdInput.getText(),
                            apptTitle,
                            apptType,
                            apptLocation,
                            apptDescription,
                            apptContact,
                            Integer.parseInt(apptCustomerComboBox.getValue().toString()),
                            startUTC,
                            endUTC,
                            userId
                    );
                }
                // reloads page
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Appointments");
                stage.show();
            }
            // this is the option for if the user is creating a new appt/the appt id box is blank when they've clicked save
            else {
                try {
                    // make sure proposed appt falls within business hours in EST
                    if (!isItWithinBusinessHours(startEST, endEST)) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setContentText("Your appointment is set to begin at " + startEST.toString().substring(11, 16) + " EST and end at " + endEST.toString().substring(11, 16) + " EST, which is outside of our regular business hours (08:00 TO 22:00 EST). Please choose another start or end time!");
                        a.showAndWait();
                        return;
                    }
                    // make sure appt doesn't end before it starts
                    else if (endLocal.isBefore(startLocal)) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setContentText("The end time you've selected is before your start time!  Better check that for accuracy, partner!");
                        a.showAndWait();
                        return;
                    }
                    // uses query function to check if proposed appt overlaps any other existing appts for the same customer
                    else if (Query.doesItOverlapCustomersOtherAppointments(startUTC, endUTC, apptCustomer)) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setContentText("Your appointment collides with an appointment that already exists for this customer!  Please choose a new date or time and try again!");
                        a.showAndWait();
                        return;
                    }
                    // if it passes all validation, added to the record with the times in UTC
                    else {
                        Query.addAppointment(
                                apptTitle,
                                apptType,
                                apptLocation,
                                apptDescription,
                                apptContact,
                                apptCustomer,
                                startUTC,
                                endUTC,
                                userId
                        );
                    }

                    // reload page
                    Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                    Scene scene = new Scene(parent);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Appointments");
                    stage.show();
                } catch (SQLException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        }
    }
    
    /**
     * checks to make sure an appointment has been selected then pulls all data from appointment record into appointment form on screen for editing
     */
    public void editApptButtonHandler() {
        selectedAppointment = apptsTable.getSelectionModel().getSelectedItem();

        // checks to make sure that an appointment has been selected from the appointments table
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

    /**
     * clears all fields in the form and resets selected appointment to none
     */
    public void clearButtonHandler() {
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

        this.selectedAppointment = null;
    }

    /** a callable version of the lambda runner for the button press to view all appointments */
    public void viewAllApptsRun() {
        viewAllAppts.run();
    }

    /** lambda function provides a functional interface for me to quickly view all appointments on screen initialization; putting this into a lambda function allows me to execute it as a runnable function on demand, which is handier than putting all of it in my initialization manually.  It also means that I can call it from other places on demand as well! */
    Runnable viewAllAppts = () -> {
        viewAppointmentsDatePicker.setValue(null);
        try {
            appointmentsObservableList.clear();
            ResultSet results = connection.createStatement().executeQuery("SELECT * FROM appointments, customers, users, contacts WHERE appointments.User_ID = users.User_ID AND appointments.Contact_ID = contacts.Contact_ID AND appointments.Customer_ID = customers.Customer_ID ORDER BY Start;");
            while (results.next()) {
                // converts all appts to user time so it can be displayed in their timezone
                LocalDateTime apptStartConvertedToUserTime = utcToUsersLDT(results.getTimestamp("Start").toLocalDateTime());
                LocalDateTime apptEndConvertedToUserTime = utcToUsersLDT(results.getTimestamp("End").toLocalDateTime());

                appointmentsObservableList.add(new Appointment(
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
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apptIdInput.setDisable(true);

        /** Prevents observable list from copying on page navs; uses query to generate list of customers and then adds to combo box results */
        customersList.clear();
        customersList = Query.getCustomersList();
        apptCustomerComboBox.setItems(customersList);

        /** Prevents observable list from copying on page navs; uses query to generate list of contacts and then adds to combo box results */
        contactsList.clear();
        contactsList = Query.getContacts();
        apptContactComboBox.setItems(contactsList);

        apptTimesList.clear();
        apptTimesList.setAll(Query.getApptTimes());

        apptStartTimeComboBox.setItems(apptTimesList);
        apptEndTimeComboBox.setItems(apptTimesList);


        /** lambda function to set date picker to ONLY allow selections from future dates and non-weekend days; this lambda is a lot easier than writing out two separate statements to complete this effect.  It's the perfect use for it because we're altering something using a function. */
        apptDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate selectedDate, boolean empty) {
                super.updateItem(selectedDate, empty);
                LocalDate currentDate = LocalDate.now();
                setDisable(empty || selectedDate.compareTo(currentDate) < 0);
            }
        });
        apptsTable.getItems().clear();
        viewAllAppts.run();
    }
}
