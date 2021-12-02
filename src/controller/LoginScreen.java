package controller;

import DAO.DatabaseConnection;
import DAO.Query;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import static DAO.Query.loginAttempt;

public class LoginScreen implements Initializable {
    @FXML private Button loginButton;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Label locationLabel;
    @FXML private AnchorPane loginScreenPane;
    User currentUser;
    Stage stage;

    @FXML public void exitButtonClick(javafx.event.ActionEvent event) throws IOException {
        stage = (Stage) loginScreenPane.getScene().getWindow();
        stage.close();
    }



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
            String username, password;
            username = usernameTextField.getText();
            password = passwordTextField.getText();

            if(loginAttempt(username, password)) {
                Connection connection;
                try {
                    connection = DatabaseConnection.openConnection();
                    ResultSet getUserInfo = connection.createStatement().executeQuery(String.format("SELECT User_ID, User_Name FROM users WHERE User_Name='%s'", username));
                    getUserInfo.next();
                    User currentUser = new User(getUserInfo.getString("User_ID"), getUserInfo.getString("User_Name"), true);
                    System.out.println("Current userId: " + currentUser.getUserId() + " userName: " + currentUser.getUsername());
                    this.currentUser = currentUser;
                    AppointmentScreen.passCurrentUserData(this.currentUser);
                } catch (SQLException ex) {
                    System.out.println("login failed");
                }
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Appointments");
                stage.show();
                Query.checkForUpcomingAppts();
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR);
                if (Locale.getDefault().getLanguage().equals("fr")) {
                    ResourceBundle rb = ResourceBundle.getBundle("main/Lang", Locale.getDefault());
                    a.setContentText(rb.getString("loginError"));
                } else {
                    a.setContentText("This username and password combination was not found!  Please try again!");
                }
                a.showAndWait();
            }
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


