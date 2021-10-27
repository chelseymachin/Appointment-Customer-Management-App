package main;

import DAO.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;

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

        Appointment appt;
        appt = new Appointment("4375");
        Customer cust;
        cust = new Customer("43", "Tina Sanchez", "4375 Havenview Parkway", "Tacoma", "98406", "USA", "3609797099");
        System.out.println(appt.getAppointmentId());
        System.out.println(cust.getAddress());
        launch(args);

        DatabaseConnection.closeConnection();

    }
}
