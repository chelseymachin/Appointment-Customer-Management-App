package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginScreen implements Initializable {
    @FXML private Button loginButton;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Label locationLabel;



    @FXML public void loginButtonClick(javafx.event.ActionEvent event) {
        if (usernameTextField.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("You must enter a username in order to login!");
            a.showAndWait();
        } else if (passwordTextField.getText().isEmpty()) {
            Alert b = new Alert(Alert.AlertType.ERROR);
            b.setContentText("You must enter a password in order to login!");
            b.showAndWait();
        } else System.out.println("Username and password fields are filled!");
    }

    @FXML public void loginButtonHover(javafx.event.Event event) {
        loginButton.setStyle("-fx-background-color: #FFF");
    }

    @FXML public void loginButtonExitHover(javafx.event.Event event) {
        loginButton.setStyle("-fx-background-color:  #C1CEFE");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Login screen initialized");
    }

}


