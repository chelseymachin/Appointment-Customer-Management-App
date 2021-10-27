package controller;

import DAO.DatabaseConnection;
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
import model.Customer;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomersScreen implements Initializable {
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
    @FXML private ComboBox<?> firstLevelDivisionComboBox;
    @FXML private ComboBox<?> countryComboBox;
    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private Button searchButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private AnchorPane customersScreenPane;
    ObservableList<Customer> customersObservableList = FXCollections.observableArrayList();
    Stage stage;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // initially, buttons are set to disabled until a customer is selected (for save/delete) or until a text field is entered (search button)
        searchButton.setDisable(true);
        saveButton.setDisable(true);
        deleteButton.setDisable(true);

        Connection connection;

        try {
            connection = DatabaseConnection.openConnection();
            ResultSet results = connection.createStatement().executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Division_ID, Phone FROM customers;");

            while (results.next()) {
                customersObservableList.add(new Customer(
                        results.getString("Customer_ID"),
                        results.getString("Customer_Name"),
                        results.getString("Address"),
                        results.getString("Division_ID"),
                        results.getString("Postal_Code"),
                        results.getString("Division_ID"),
                        results.getString("Phone"))
                );
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        customersIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customersNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        customersAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customersPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        customersPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        customersTable.setItems(customersObservableList);


    }
}
