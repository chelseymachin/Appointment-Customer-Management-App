package main;

import DAO.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/loginScreen.fxml"));
        primaryStage.setTitle("Login Screen");
        primaryStage.setScene(new Scene(root, 600, 450));
        primaryStage.show();
    }

    public static void main(String[] args) {
        DatabaseConnection.openConnection();
        ResourceBundle rb = ResourceBundle.getBundle("main/Lang", Locale.getDefault());

        if(Locale.getDefault().getLanguage().equals("fr")) {
            System.out.println(rb.getString("username") + " " + rb.getString("password") + " " + rb.getString("login"));
        }

        launch(args);

        DatabaseConnection.closeConnection();

    }
}
