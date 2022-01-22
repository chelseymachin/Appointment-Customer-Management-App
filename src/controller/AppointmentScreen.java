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
import model.Contact;
import model.Customer;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentScreen implements Initializable {
    /** button to edit when an appointment from the table has been selected */
    @FXML public Button editApptButton;
    /** text field to push the selected appointment's ID when one is selected to edit */
    @FXML public TextField apptIdInput;
    /** text field to edit the title of a selected appointment */
    @FXML public TextField apptTitleInput;
    /** text area to edit the description of a selected appointment */
    @FXML public TextArea apptDescriptionInput;
    /** text field to edit the location of a selected appointment */
    @FXML public TextField apptLocationInput;
    /** text field to edit the type of a selected appointment */
    @FXML public TextField apptTypeInput;
    /** combo box to hold the contacts available to select */
    @FXML public ComboBox<Contact> apptContactComboBox;
    /** combo box to hold the customers available to select */
    @FXML public ComboBox<Customer> apptCustomerComboBox;
    /** combo box to hold the users available to select */
    @FXML public ComboBox<User> apptUserComboBox;
    /** date picker for the user to select a date for the appointment or for a selected appointment to display it's saved date */
    @FXML public DatePicker apptDatePicker;
    /** combo box to hold the appointment start times aviailable */
    @FXML public ComboBox apptStartTimeComboBox;
    /** combo box to hold the appointment end times available */
    @FXML public ComboBox apptEndTimeComboBox;
    /** button to clear all fields and currently selected appointments */
    @FXML public Button clearButton;
    /** toggle group for view selection toggle buttons */
    @FXML public ToggleGroup viewSelection;
    /** toggle button to view all appts */
    @FXML public ToggleButton viewAllToggle;
    /** toggle button to view monthly appts */
    @FXML public ToggleButton viewMonthToggle;
    /** toggle button to view weeklyk appts */
    @FXML public ToggleButton viewWeekToggle;
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

    ObservableList<User> usersList = FXCollections.observableArrayList();
    ObservableList<Contact> contactsList = FXCollections.observableArrayList();
    ObservableList<Customer> customersList = FXCollections.observableArrayList();
    ObservableList<LocalTime> apptTimesList = FXCollections.observableArrayList();
    Appointment selectedAppointment;
    public static User currentUser;
    public boolean isMonthlyView = false;
    public boolean isWeeklyView = false;
    public boolean isViewAll = true;

    /**
     * used to pass the user data about the user currently logged in from the login screen
     * @param loginUser accepts a User object of (preferably) the currently logged in user from the login screen
     */
    public static void passCurrentUserData(User loginUser) {
        User newUser = new User(
                loginUser.getUserId(),
                loginUser.getUsername()
        );
        currentUser = newUser;
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
     * executes when view by week is selected; calls the viewWeeklyAppts runnable function and sets boolean value of isWeeklyView to true (and all other view values to false)
     */
    public void viewByWeek() {
        viewWeeklyApptsRun();
        isWeeklyView = true;
        isMonthlyView = false;
        isViewAll = false;
        viewWeekToggle.isSelected();
    }

    /**
     * executes when view by month is selected; calls the viewMonthlyAppts runnable function and sets boolean value of isMonthlyView to true (and all other view values to false)
     */
    public void viewByMonth() {
        viewMonthlyApptsRun();
        isWeeklyView = false;
        isMonthlyView = true;
        isViewAll = false;
        viewMonthToggle.isSelected();
    }

    /**
     *  executes when view all is selected; calls the viewAllAppts runnable function and sets boolean value of isViewAll to true (and all other view values to false)
     */
    public void viewAll() {
        viewAllApptsRun();
        isWeeklyView = false;
        isMonthlyView = false;
        isViewAll = true;
        viewAllToggle.isSelected();
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
     * checks to see if the start and end LocalDateTime that are input are within the stated business hours (8 - 22 EST)
     * @param start LocalDateTime object of start time of appt (in pre-converted EST is expectation)
     * @param end LocalDateTime object of end time of appt (in pre-converted EST is expectation)
     * @return if start and end fall within EST business hours (8 - 22), then returns true; otherwise returns false
     */
    public boolean isItWithinBusinessHours(LocalDateTime start, LocalDateTime end) {
        LocalTime startLDTconvertedToLT = start.toLocalTime();
        LocalTime endLDTConvertedToLT = end.toLocalTime();
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
        LocalDateTime startTime = null;
        LocalDateTime startEST = null;
        LocalDateTime startUTC = null;
        LocalDateTime endTime = null;
        LocalDateTime endEST = null;
        LocalDateTime endUTC = null;
        Integer userId = null;

        // checks to see if any input areas are empty; if so, gives an error
        if (apptTypeInput.getText().isEmpty() || apptTitleInput.getText().isEmpty() || apptLocationInput.getText().isEmpty() || apptDescriptionInput.getText().isEmpty() || apptUserComboBox.getSelectionModel().isEmpty() || apptContactComboBox.getSelectionModel().isEmpty() || apptCustomerComboBox.getSelectionModel().isEmpty() || apptDatePicker.getValue() == null || apptStartTimeComboBox.getSelectionModel().isEmpty() || apptEndTimeComboBox.getSelectionModel().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a value/selection for all fields in order to save!");
            a.showAndWait();
        } else {
            // saves all the input strings/data to the empty variables from before
            apptTitle = apptTitleInput.getText();
            apptType = apptTypeInput.getText();
            apptLocation = apptLocationInput.getText();
            apptDescription = apptDescriptionInput.getText();
            userId = apptUserComboBox.getValue().getUserId();
            apptContact = apptContactComboBox.getValue().getContactID();
            apptCustomer = apptCustomerComboBox.getValue().getCustomerId();
            LocalDate apptDate = apptDatePicker.getValue();
            String apptDateString = apptDate.toString();

            // gets appt start time as LocalTime object from combobox, converts it to LocalDateTime object, then converts it to EST and UTC to use for more validation below
            String apptStart = apptStartTimeComboBox.getValue().toString();
            startTime = stringToLDTConverter(apptStart, apptDateString);
            startEST = Query.convertFromUserTimeZoneToEST(startTime);
            startUTC = Query.convertFromUserTimeZoneToUTC(startTime);


            // gets appt end time as LocalTime object from combobox, converts it to LocalDateTime object, then converts it to EST and UTC to use for more validation below
            String apptEnd = apptEndTimeComboBox.getValue().toString();
            endTime = stringToLDTConverter(apptEnd, apptDateString);
            endEST = Query.convertFromUserTimeZoneToEST(endTime);
            endUTC = Query.convertFromUserTimeZoneToUTC(endTime);


            // if appt ID is filled in, the user is attempting to edit an existing appt; so we will act accordingly below
            if (!apptIdInput.getText().isEmpty()) {
                // passes pre-converted EST times of start/end of appt to see if proposed appt time is within EST business hours; if it is not, gives error and exits
                if (!isItWithinBusinessHours(startEST, endEST)) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("Your appointment is set to begin at " + startEST.toString().substring(11, 16) + " EST and end at " + endEST.toString().substring(11, 16) + " EST, which is outside of our regular business hours (08:00 TO 22:00 EST). Please choose another start or end time!");
                    a.showAndWait();
                    return;
                }
                // if start and end of appt is the same time, give error and exit
                else if (endTime.isEqual(startTime)) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("You're trying to save an appointment with the same start and end time!  Please check again and adjust! Thanks!");
                    a.showAndWait();
                    return;
                }
                // if end of appt is before the start of the appt, give error and exit
                else if (endTime.isBefore(startTime)) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("The end time you've selected is before your start time!  Better check that for accuracy, partner!");
                    a.showAndWait();
                    return;
                }
                // uses query function to check if appt only overlaps itself; if it overlaps any other appt but itself, gives error and exits
                else if (Query.doesItOverlapAnyExistingApptButItself(startUTC, endUTC, apptCustomer, Integer.parseInt(apptIdInput.getText()))) {
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
                            apptCustomer,
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
                    // if start and end of appt is the same time, give error and exit
                    else if (endTime.isEqual(startTime)) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setContentText("You're trying to save an appointment with the same start and end time!  Please check again and adjust! Thanks!");
                        a.showAndWait();
                        return;
                    }
                    // make sure appt doesn't end before it starts
                    else if (endTime.isBefore(startTime)) {
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
                    System.out.println("There was a problem saving the appointment!");
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
            apptIdInput.setText(selectedAppointment.getAppointmentId().toString());
            apptTypeInput.setText(selectedAppointment.getType());
            apptTitleInput.setText(selectedAppointment.getTitle());
            apptLocationInput.setText(selectedAppointment.getLocation());
            apptDescriptionInput.setText(selectedAppointment.getDescription());

            Integer selectedAppointmentUserID = selectedAppointment.getUserId();

            for (User user : apptUserComboBox.getItems()) {
                if (selectedAppointmentUserID == user.getUserId()) {
                    apptUserComboBox.setValue(user);
                    break;
                }
            }

            Integer selectedAppointmentContactID = selectedAppointment.getContactId();
            for (Contact contact : apptContactComboBox.getItems()) {
                if (selectedAppointmentContactID == contact.getContactID()) {
                    apptContactComboBox.setValue(contact);
                    break;
                }
            }

            Integer selectedAppointmentCustomerID = selectedAppointment.getCustomerId();
            for (Customer customer : apptCustomerComboBox.getItems()) {
                if (selectedAppointmentCustomerID == customer.getCustomerId()) {
                    apptCustomerComboBox.setValue(customer);
                    break;
                }
            }

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
        apptUserComboBox.valueProperty().set(null);
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

    /** a callable version of the lambda runner for the button press to view weekly appointments */
    public void viewWeeklyApptsRun() {
        viewWeeklyAppts.run();
    }

    /** a callable version of the lambda runner for the button press to view monthly appointments */
    public void viewMonthlyApptsRun() {
        viewMonthlyAppts.run();
    }

    /** lambda function provides a functional interface for me to quickly view all appointments on screen initialization; putting this into a lambda function allows me to execute it as a runnable function on demand, which is handier than putting all of it in my initialization manually.  It also means that I can call it from other places on demand as well! */
    Runnable viewAllAppts = () -> {
        appointmentsObservableList = Query.getAllAppointments();
        apptsTable.setItems(appointmentsObservableList);
    };

    /** lambda function provides a functional interface for me to quickly get weekly appointments and populate the table with them; putting this into a lambda function allows me to execute it as a runnable function on demand, which is handier than putting all of it in my initialization manually or using it directly in the button press.  It also means that I can call it from other places on demand as well! */
    Runnable viewWeeklyAppts = () -> {
        appointmentsByWeekObservableList = Query.getAppointmentsThisWeek();
        apptsTable.setItems(appointmentsByWeekObservableList);
    };

    /** lambda function provides a functional interface for me to quickly get monthly appointments and populate the table with them; putting this into a lambda function allows me to execute it as a runnable function on demand, which is handier than putting all of it in my initialization manually or using it directly in the button press.  It also means that I can call it from other places on demand as well! */
    Runnable viewMonthlyAppts = () -> {
        appointmentsByMonthObservableList = Query.getAppointmentsThisMonth();
        apptsTable.setItems(appointmentsByMonthObservableList);
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (isWeeklyView) {
            viewWeekToggle.isSelected();
            viewWeeklyApptsRun();
        } else if (isMonthlyView) {
            viewMonthToggle.isSelected();
            viewMonthlyApptsRun();
        } else {
            viewAllToggle.isSelected();
            viewAllApptsRun();
        }

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

        apptIdInput.setDisable(true);

        usersList.clear();
        usersList = Query.getUsersList();
        apptUserComboBox.setItems(usersList);

        /** Prevents observable list from copying on page navs; uses query to generate list of customers and then adds to combo box results */
        customersList.clear();
        customersList = Query.getCustomersList();
        apptCustomerComboBox.setItems(customersList);

        /** Prevents observable list from copying on page navs; uses query to generate list of contacts and then adds to combo box results */
        contactsList.clear();
        contactsList = Query.getContacts();
        apptContactComboBox.setItems(contactsList);

        /** Prevents observable list from copying on page navs; uses query to generate list of appointment times and then addds to combo box results */
        apptTimesList.clear();
        apptTimesList = Query.getApptTimes();
        apptStartTimeComboBox.setItems(apptTimesList);
        apptEndTimeComboBox.setItems(apptTimesList);


        /** lambda function to set date picker to ONLY allow selections from future dates and non-weekend days; this lambda is a lot easier than writing out two separate statements to complete this effect.  It's the perfect use for it because we're altering something using a function. */
        apptDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate selectedDate, boolean empty) {
                super.updateItem(selectedDate, empty);
                LocalDate currentDate = LocalDate.now();
                setDisable(empty || selectedDate.compareTo(currentDate) < 0 || selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY);
            }
        });
        apptDatePicker.setEditable(false);

        Query.checkForUpcomingAppts(currentUser.getUserId());
    }
}
