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
import model.Country;
import model.Customer;
import model.FirstLevelDivision;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
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
    @FXML private TextField customerIdInput;
    @FXML private TextField customerNameInput;
    @FXML private TextField customerAddressInput;
    @FXML private TextField postalCodeInput;
    @FXML private TextField customerPhoneInput;
    @FXML private ComboBox stateComboBox;
    @FXML private ComboBox countryComboBox;
    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private AnchorPane customersScreenPane;
    ObservableList<Customer> customersObservableList = FXCollections.observableArrayList();
    ObservableList<String> countriesObservableList = FXCollections.observableArrayList();
    ObservableList<String> fldObservableList = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    private FirstLevelDivision selectedCustomerFLD;
    private Country selectedCustomerCountry;
    private User currentUser;
    Stage stage;

    public void viewAllCustomers() {
        Connection connection;

        try {
            customersObservableList.clear();
            connection = DAO.DatabaseConnection.openConnection();
            ResultSet results = connection.createStatement().executeQuery("SELECT * FROM customers AS c INNER JOIN first_level_divisions AS d ON c.Division_ID = d.Division_ID INNER JOIN countries AS co ON co.Country_ID=d.COUNTRY_ID;");
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
        } catch (Exception ex) {
            System.out.println("Error with viewing all customers");
        }
    }

    public void saveButtonHandler(javafx.event.ActionEvent event) throws IOException, SQLException {
        String customerId;
        String customerName = null;
        String address = null;
        String fld = null;
        String zip = null;
        String phone = null;


        if (customerNameInput.getText().isEmpty() || customerAddressInput.getText().isEmpty() || stateComboBox.getSelectionModel().isEmpty() || countryComboBox.getSelectionModel().isEmpty() || postalCodeInput.getText().isEmpty() || customerPhoneInput.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please enter a value/selection for all fields in order to save!");
            a.showAndWait();
        } else {
            customerName = customerNameInput.getText();
            address = customerAddressInput.getText();
            fld = stateComboBox.getValue().toString();
            zip = postalCodeInput.getText();
            phone = customerPhoneInput.getText();
        }

        Integer userId = currentUser.getUserId();

        if (!customerIdInput.getText().isEmpty()) {
            customerId = customerIdInput.getText();
            Query.updateCustomer(
                    customerId,
                    customerName,
                    address,
                    zip,
                    phone,
                    Query.getFirstLevelDivisionId(fld),
                    userId
            );


        } else {
            try {
                Query.addCustomer(
                        customerName,
                        address,
                        zip,
                        phone,
                        Query.getFirstLevelDivisionId(fld),
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

    public void deleteButtonHandler(javafx.event.ActionEvent event) throws IOException {
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

    @FXML void filterFLDByCountry(ActionEvent event) {
        if (!countryComboBox.getSelectionModel().isEmpty()) {
            fldObservableList.clear();
            try {
                ObservableList<FirstLevelDivision> firstLevelDivisions = Query.getFirstLevelDivisionsByCountry(countryComboBox.getSelectionModel().getSelectedItem().toString());
                if (firstLevelDivisions != null) {
                    for (FirstLevelDivision firstLevelDivision: firstLevelDivisions) {
                        fldObservableList.add(firstLevelDivision.firstLevelDivisionName);
                    }
                }
                stateComboBox.setItems(fldObservableList);
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    @FXML public void editButtonHandler(javafx.event.ActionEvent event) throws IOException {
        selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        selectedCustomerCountry = selectedCustomer.getCustomerCountry();
        selectedCustomerFLD = selectedCustomer.getCustomerFirstLevelDivision();

        if(selectedCustomer instanceof Customer) {
            customerIdInput.setDisable(true);
            customerIdInput.setText(selectedCustomer.getCustomerId().toString());
            customerNameInput.setText(selectedCustomer.getName());
            customerAddressInput.setText(selectedCustomer.getAddress());
            postalCodeInput.setText(selectedCustomer.getZip());
            countryComboBox.setValue(selectedCustomer.getCountryName());
            stateComboBox.setValue(selectedCustomer.getFirstLevelDivisionName());
            customerPhoneInput.setText(selectedCustomer.getPhoneNumber());
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a customer in order to edit!");
            a.showAndWait();
        }
    }

    public void clearButtonHandler(ActionEvent actionEvent) {
        customerIdInput.clear();
        customerNameInput.clear();
        customerAddressInput.clear();
        postalCodeInput.clear();
        stateComboBox.valueProperty().set(null);
        stateComboBox.setPromptText("State/Province");
        countryComboBox.valueProperty().set(null);
        countryComboBox.setPromptText("Country");
        customerPhoneInput.clear();

        customerIdInput.setDisable(false);

        this.selectedCustomer = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customerIdInput.setDisable(true);
        fldObservableList.clear();
        fldObservableList = Query.getFirstLevelDivisionsList();
        stateComboBox.setItems(fldObservableList);

        countriesObservableList.clear();
        countriesObservableList = Query.getCountriesList();
        countryComboBox.setItems(countriesObservableList);
        viewAllCustomers();

        customersIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customersNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        customersAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customersPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        customersPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
    }

}
