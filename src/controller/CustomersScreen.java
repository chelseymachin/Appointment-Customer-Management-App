package controller;

import DAO.DatabaseConnection;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Country;
import model.Customer;
import model.FirstLevelDivision;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomersScreen implements Initializable {
    public Button clearButton;
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, Integer> customersIdCol;
    @FXML private TableColumn<Customer, String> customersNameCol;
    @FXML private TableColumn<Customer, String> customersAddressCol;
    @FXML private TableColumn<Customer, String> customersPostalCodeCol;
    @FXML private TableColumn<Customer, String> customersPhoneCol;
    @FXML private TableColumn<Customer, String> customersFirstLevelDivisionCol;
    @FXML private TableColumn<Customer, String> customersCountryCol;
    @FXML private TextField customerIdInput;
    @FXML private TextField customerNameInput;
    @FXML private TextField customerAddressInput;
    @FXML private TextField postalCodeInput;
    @FXML private TextField customerPhoneInput;
    @FXML private ComboBox<FirstLevelDivision> stateComboBox;
    @FXML private ComboBox<Country> countryComboBox;
    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private AnchorPane customersScreenPane;
    ObservableList<Customer> customersObservableList = FXCollections.observableArrayList();
    ObservableList<Country> countriesObservableList = FXCollections.observableArrayList();
    ObservableList<FirstLevelDivision> fldObservableList = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    private FirstLevelDivision selectedCustomerFLD;
    private Country selectedCustomerCountry;
    public static User currentUser;
    Stage stage;

    // button handler functions
    /**
     * validates customer data and saves or rejects it based on validation results
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     * @throws SQLException throws error if unable to save data to record
     */
    public void saveButtonHandler(javafx.event.ActionEvent event) throws IOException, SQLException {
        String customerId;
        String customerName = null;
        String address = null;
        String zip = null;
        String phone = null;
        Integer fldId = null;

        if (customerNameInput.getText().isEmpty() || customerAddressInput.getText().isEmpty() || stateComboBox.getSelectionModel().isEmpty() || countryComboBox.getSelectionModel().isEmpty() || postalCodeInput.getText().isEmpty() || customerPhoneInput.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a value/selection for all fields in order to save!");
            a.showAndWait();
            return;
        } else {
            customerName = customerNameInput.getText();
            address = customerAddressInput.getText();
            fldId = stateComboBox.getValue().getFirstLevelDivisionId();
            zip = postalCodeInput.getText();
            phone = customerPhoneInput.getText();
        }

        Integer userId = LoginScreen.currentUser.getUserId();

        if (!customerIdInput.getText().isEmpty()) {
            customerId = customerIdInput.getText();
            Query.updateCustomer(
                    customerId,
                    customerName,
                    address,
                    zip,
                    phone,
                    fldId,
                    userId
            );
        } else {
            try {
                Query.addCustomer(
                        customerName,
                        address,
                        zip,
                        phone,
                        fldId,
                        userId
                );
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }
        }
        Parent parent = FXMLLoader.load(getClass().getResource("/view/customersScreen.fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Customers");
        stage.show();
    }

    /**
     * makes sure a customer has been selected, then attempts to delete
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     */
    public void deleteButtonHandler(javafx.event.ActionEvent event) throws IOException {
        selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        if(selectedCustomer instanceof Customer) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Confirm deletion");
            alert.setContentText("Are you sure you want to delete this customer?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Query.deleteCustomer(selectedCustomer.getCustomerId().toString());
                Parent parent = FXMLLoader.load(getClass().getResource("/view/customersScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Customers");
                stage.show();
            }
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("You must first select a customer to delete from the table!");
            a.showAndWait();
        }
    }

    /**
     * brings user back to the appointments screen
     * @param event accepts event input from JavaFX to get current scene and windoW
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
     * confirms logout; on confirmation, logs out user and returns them to login screen
     * @param event accepts event input from JavaFX to get current scene and window
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

    /**
     * validates that a customer has been selected from the table, then generates a selected country and first level division to populate combo boxes with before filling in all the form fields with the selected customer data
     */
    public void editButtonHandler() {
        selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        selectedCustomerCountry = selectedCustomer.getCustomerCountry();
        selectedCustomerFLD = selectedCustomer.getCustomerFirstLevelDivision();

        if(selectedCustomer instanceof Customer) {
            customerIdInput.setDisable(true);
            customerIdInput.setText(selectedCustomer.getCustomerId().toString());
            customerNameInput.setText(selectedCustomer.getName());
            customerPhoneInput.setText(selectedCustomer.getPhoneNumber());
            customerAddressInput.setText(selectedCustomer.getAddress());
            postalCodeInput.setText(selectedCustomer.getZip());

            String selectedCustomerCountry = selectedCustomer.getCountryName();
            for (Country country : countryComboBox.getItems()) {
                if (selectedCustomerCountry.equals(country.getCountryName())) {
                    countryComboBox.setValue(country);
                    break;
                }
            }

            String selectedCustomerFLD = selectedCustomer.getFirstLevelDivisionName();
            for (FirstLevelDivision fld : stateComboBox.getItems()) {
                if (selectedCustomerFLD.equals(fld.getFirstLevelDivisionName())) {
                    stateComboBox.setValue(fld);
                    break;
                }
            }


        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a customer in order to edit!");
            a.showAndWait();
        }
    }

    /**
     * clears all input fields and selected customer
     */
    public void clearButtonHandler() {
        customerIdInput.clear();
        customerNameInput.clear();
        customerAddressInput.clear();
        postalCodeInput.clear();
        stateComboBox.valueProperty().set(null);
        stateComboBox.setPromptText("State/Province");
        countryComboBox.valueProperty().set(null);
        countryComboBox.setPromptText("Country");
        customerPhoneInput.clear();

        this.selectedCustomer = null;
    }

    // helper functions for combobox filling and table data filling
    /**
     * Uses query function to filter combo box results for the first level division combo box when the country changes
     */
    public void filterFLDByCountry() {
        if (!countryComboBox.getSelectionModel().isEmpty()) {
            fldObservableList.clear();
            try {
                ObservableList<FirstLevelDivision> firstLevelDivisions = Query.getFirstLevelDivisionsByCountry(countryComboBox.getValue().getCountryName());
                if (firstLevelDivisions != null) {
                    for (FirstLevelDivision firstLevelDivision: firstLevelDivisions) {
                        FirstLevelDivision newFLD = new FirstLevelDivision(
                                firstLevelDivision.getFirstLevelDivisionId(),
                                firstLevelDivision.getCountryId(),
                                firstLevelDivision.getFirstLevelDivisionName()
                        );
                        fldObservableList.add(newFLD);
                    }
                }
                stateComboBox.setItems(fldObservableList);
            } catch (SQLException exception){
                System.out.println(exception.getMessage());
            }
        }
    }

    /**
     * lambda function provides a functional interface for me to quickly view all customers on screen initialization
     */
    Runnable viewAllCustomers = () -> {
        Connection connection;
        try {
            connection = DatabaseConnection.openConnection();
            customersObservableList.clear();

            String sql = "SELECT * FROM customers INNER JOIN first_level_divisions ON customers.Division_ID = first_level_divisions.Division_ID INNER JOIN countries ON countries.Country_ID=first_level_divisions.COUNTRY_ID;";
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.execute();
            ResultSet results = prepared.getResultSet();

            while (results.next()) {
                customersObservableList.add(new Customer(
                        results.getInt("Customer_ID"),
                        results.getString("Customer_Name"),
                        results.getString("Address"),
                        results.getString("Division"),
                        results.getInt("Division_ID"),
                        results.getString("Postal_Code"),
                        results.getString("Country"),
                        results.getString("Phone")
                ));
            }
            customersTable.setItems(customersObservableList);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        } finally {
            DatabaseConnection.closeConnection();
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customerIdInput.setDisable(true);
        fldObservableList.clear();
        fldObservableList = Query.getFirstLevelDivisionsList();
        stateComboBox.setItems(fldObservableList);

        countriesObservableList.clear();
        countriesObservableList = Query.getCountriesList();
        countryComboBox.setItems(countriesObservableList);
        viewAllCustomers.run();

        customersIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customersNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        customersAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customersPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        customersPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        customersFirstLevelDivisionCol.setCellValueFactory(new PropertyValueFactory<>("firstLevelDivisionName"));
        customersCountryCol.setCellValueFactory(new PropertyValueFactory<>("countryName"));
    }
}
