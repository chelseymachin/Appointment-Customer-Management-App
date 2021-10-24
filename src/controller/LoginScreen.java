package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginScreen implements Initializable {
    @FXML private Button loginButton;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Label locationLabel;



    @FXML public void loginButtonClick(javafx.event.ActionEvent event) throws IOException {
        if (usernameTextField.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            if (Locale.getDefault().getLanguage().equals("fr")) {
                ResourceBundle rb = ResourceBundle.getBundle("main/Lang", Locale.getDefault());
                a.setContentText(rb.getString("usernameError"));
            } else {
                a.setContentText("You must enter a username in order to login!");
            }
            a.showAndWait();
        } else if (passwordTextField.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            if (Locale.getDefault().getLanguage().equals("fr")) {
                ResourceBundle rb = ResourceBundle.getBundle("main/Lang", Locale.getDefault());
                a.setContentText(rb.getString("passwordError"));
            } else {
                a.setContentText("You must enter a password in order to login!");
            }
            a.showAndWait();
        } else {
            Parent parent = FXMLLoader.load(getClass().getResource("/view/CalendarScreen.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        };
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


        locationLabel.setText(Locale.getDefault().getDisplayCountry());

        if(Locale.getDefault().getLanguage().equals("fr")) {
            ResourceBundle rb = ResourceBundle.getBundle("main/Lang", Locale.getDefault());
            loginButton.setText(rb.getString("login"));
            loginButton.setPrefWidth(200);
            loginButton.setLayoutX(200);
            usernameTextField.setPromptText(rb.getString("username"));
            passwordTextField.setPromptText(rb.getString("password"));
        }
    }

}


