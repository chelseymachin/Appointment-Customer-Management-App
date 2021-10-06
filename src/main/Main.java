package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/firstScreen.fxml"));
        primaryStage.setTitle("First View");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Appointment appt;
        appt = new Appointment("4375");
        Customer cust;
        cust = new Customer("43", "Tina Sanchez", "4375 Havenview Parkway", "#2", "Tacoma", "98406", "USA", "3609797099");
        System.out.println(appt.getAppointmentId());
        System.out.println(cust.getAddress());
        launch(args);

    }
}
