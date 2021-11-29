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
    ObservableList<Country> countriesObservableList = FXCollections.observableArrayList();
    ObservableList<FirstLevelDivision> fldObservableList = FXCollections.observableArrayList();
    private Customer selectedCustomer;
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
        Connection connection;
        connection = DAO.DatabaseConnection.openConnection();

        PreparedStatement prepared = null;
        String sql = null;
        Integer customerId;

        if (!customerIdInput.getText().isEmpty()) {
            customerId = Integer.parseInt(customerIdInput.getText());
        }

        String customerName = customerNameInput.getText();
        String address = customerAddressInput.getText();
        FirstLevelDivision fld = new FirstLevelDivision(
                Query.getFirstLevelDivisionId(stateComboBox.getSelectionModel().getSelectedItem().toString()),
                stateComboBox.getSelectionModel().getSelectedItem().toString()
        );
        Country country = new Country(
                Query.getCountryId(countryComboBox.getSelectionModel().getSelectedItem().toString()),
                countryComboBox.getSelectionModel().getSelectedItem().toString()
        );
        String zip = postalCodeInput.getText();
        String countryName = country.countryName;
        String phone = customerPhoneInput.getText();
        Integer userId = currentUser.getUserId();

        if (!customerIdInput.getText().isEmpty()) {
            sql = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Last_Update = NOW(), Last_Updated_By = ? WHERE Customer_ID = ?";
            prepared = connection.prepareStatement(sql);
            prepared.setString(1, customerName);
            prepared.setString(2, address);
            prepared.setString(3, zip);
            prepared.setString(4, phone);
            prepared.setInt(5, userId);
            prepared.setInt(6, Integer.parseInt(this.customerIdInput.getText()));

            int result = prepared.executeUpdate();
            if (result > 0) {
                Parent parent = FXMLLoader.load(getClass().getResource("/view/customersScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Customers");
                stage.show();
            } else {
                System.out.println("No update happened!");
            }
        } else {
            sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) VALUES (?, ?, ?, ?, NOW(), ?, NOW(), ?, ?)";
            prepared = connection.prepareStatement(sql);
            prepared.setString(1, customerName);
            prepared.setString(2, address);
            prepared.setString(3, zip);
            prepared.setString(4, phone);
            prepared.setInt(5, userId);
            prepared.setInt(6, userId);
            prepared.setInt(7, fld.firstLevelDivisionId);

            int result = prepared.executeUpdate();
            if (result > 0) {
                Parent parent = FXMLLoader.load(getClass().getResource("/view/customersScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Customers");
                stage.show();
            } else {
                System.out.println("No new customers were made!");
            }
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
            countryComboBox.getSelectionModel().select(selectedCustomer.getCountryName());
            stateComboBox.getSelectionModel().select(selectedCustomer.getFirstLevelDivisionName());
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
