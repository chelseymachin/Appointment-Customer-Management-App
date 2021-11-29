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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Country;
import model.Customer;
import model.FirstLevelDivision;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
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
    @FXML private ComboBox<String> stateComboBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private AnchorPane customersScreenPane;
    ObservableList<Customer> customersObservableList = FXCollections.observableArrayList();
    ObservableList<String> statesList = FXCollections.observableArrayList();
    ObservableList<String> countryList = FXCollections.observableArrayList();
    private Customer selectedCustomer;
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

    public void deleteButtonHandler(javafx.event.ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Confirm deletion");
        alert.setContentText("Are you sure you want to delete this customer?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {

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

    @FXML public void editButtonHandler(javafx.event.ActionEvent event) throws IOException {
        selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

        if(selectedCustomer instanceof Customer) {
            customerIdInput.setDisable(true);
            customerIdInput.setText(selectedCustomer.getCustomerId().toString());
            customerNameInput.setText(selectedCustomer.getName());
            customerAddressInput.setText(selectedCustomer.getAddress());
            postalCodeInput.setText(selectedCustomer.getZip());
            Country customerCountry = selectedCustomer.getCustomerCountry();
            System.out.println(customerCountry);
            FirstLevelDivision customerFLD = selectedCustomer.getCustomerFirstLevelDivision();
            System.out.println(customerFLD);
            countryComboBox.setValue(customerCountry);
            stateComboBox.setValue(customerFLD);
            customerPhoneInput.setText(selectedCustomer.getPhoneNumber());
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a customer in order to edit!");
            a.showAndWait();
        }
    }

    public void searchByCustomerId(KeyEvent keyEvent) {
    }

    public void searchByCustomerName(KeyEvent keyEvent) {
    }

    public void searchByCustomerPhone(KeyEvent keyEvent) {
    }

    private void setFLDComboItems(){
        ObservableList<String> fldList = FXCollections.observableArrayList();

        try {
            ObservableList<FirstLevelDivision> firstLevelDivisions = Query.getFirstLevelDivisionsList();;
            if (firstLevelDivisions != null) {
                for (FirstLevelDivision firstLevelDivision: firstLevelDivisions) {
                    fldList.add(firstLevelDivision.getFirstLevelDivisionName());
                }
            }
        } catch (Exception ex) {
            System.out.println("Error getting FLD combo box items");
        }

        stateComboBox.setItems(fldList);
    }


    public void clearButtonHandler(ActionEvent actionEvent) {
        customerIdInput.clear();
        customerNameInput.clear();
        customerAddressInput.clear();
        postalCodeInput.clear();
        stateComboBox.setValue(null);
        countryComboBox.setValue(null);
        customerPhoneInput.clear();

        customerIdInput.setDisable(false);

        this.selectedCustomer = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        viewAllCustomers();

        customersIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customersNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        customersAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customersPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        customersPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));



    }

}
