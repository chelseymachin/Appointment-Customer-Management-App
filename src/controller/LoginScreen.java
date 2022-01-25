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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;

import static DAO.Query.loginAttempt;

public class LoginScreen implements Initializable {
    @FXML private Button loginButton;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Label locationLabel;
    @FXML private AnchorPane loginScreenPane;
    public static User currentUser;
    Stage stage;

    // button handler functions
    /**
     * exits program
     */
    public void exitButtonClick() {
        stage = (Stage) loginScreenPane.getScene().getWindow();
        stage.close();
    }

    /**
     * validates that the username and password field have been filled in; provides error if not; checks for language default setting and translates error if in French
     * @param event accepts event input from JavaFX to get current scene and window
     * @throws IOException throws error if unable to load new screen
     */
    public void loginButtonClick(javafx.event.ActionEvent event) throws IOException {
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

                    String sql = "SELECT User_ID, User_Name From users where User_Name=?;";
                    PreparedStatement prepared = connection.prepareStatement(sql);
                    prepared.setString(1, username);
                    prepared.execute();
                    ResultSet getUserInfo = prepared.getResultSet();
                    getUserInfo.next();

                    // creates currentUser object with data of currently logged in user
                    User currentUser = new User(getUserInfo.getString("User_ID"), getUserInfo.getString("User_Name"), true);
                    this.currentUser = currentUser;
                    AppointmentScreen.passCurrentUserData(currentUser);
                    addLoginAttempt(username, true);


                    DatabaseConnection.closeConnection();
                } catch (SQLException exception) {
                    System.out.println("There was a SQL problem with logging in!");
                }
                Parent parent = FXMLLoader.load(getClass().getResource("/view/appointmentScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Appointments");
                stage.show();
                Query.checkForUpcomingAppts(currentUser.getUserId());
            } else {
                addLoginAttempt(username, false);
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

    /** function that changes the login button color when hovered in field */
    public void loginButtonHover() {
        loginButton.setStyle("-fx-background-color: #FFF");
    }

    /** function that changes the login button color back when leaving button hover */
    public void loginButtonExitHover() {
        loginButton.setStyle("-fx-background-color:  #C1CEFE");
    }

    /** creates a file */
    public void createLoginActivityFile(){
        try {
            File file = new File("logs/login_activity.txt");
            if (file.createNewFile()) {
                System.out.println("File created:" + file.getName());
            } else {
                System.out.println("File already exists. Location: "+ file.getPath());
            }
        } catch (IOException exception){
            System.out.println("Unable to create login activity file!");
        }
    }

    /**
     * adds a login attempt entry to the login activity file in the logs folder
     * @param username the username of the user attempting to login
     * @param loggedInSuccessfully the boolean value pertaining to whether or not the user successfully logged in
     */
    public void addLoginAttempt(String username, Boolean loggedInSuccessfully) {
        try {
            FileWriter fileWriter = new FileWriter("logs/login_activity.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(username + " attempted a login at " + Timestamp.valueOf(LocalDateTime.now()));
            bufferedWriter.newLine();

            if (loggedInSuccessfully) {
                bufferedWriter.write(username + "'s login attempt was a success!" );
            } else {
                bufferedWriter.write(username + "'s login attempt failed!");
            }
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException exception) {
            System.out.println("There was a problem adding a login attempt to the file");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ZoneId localZoneId = ZoneId.systemDefault();
        locationLabel.setText(String.valueOf(localZoneId));

        createLoginActivityFile();

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


