package controller;

import DAO.DatabaseConnection;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportsScreen implements Initializable {


    public TextArea reportTextArea;
    public Button apptsByTypeMonthButton;
    public Button consultantSchedulesButton;
    public Button backButton;
    public Button logoutButton;

    public void reportOneButtonHandler(javafx.event.ActionEvent event) throws IOException, SQLException {
        reportTextArea.setText(getApptsByTypeMonth());
    }

    public String getApptsByTypeMonth() throws SQLException {
        Connection connection = DatabaseConnection.openConnection();

        try {
            StringBuilder reportText = new StringBuilder();
            reportText.append("Month       |  # of Appts    |  Type \n_________________________________\n");

            ResultSet results = connection.createStatement().executeQuery(String.format("SELECT MONTHNAME(start) as Month, Type, COUNT(*) as Amount FROM appointments GROUP BY MONTH(start), type;"));
            while (results.next()) {
                reportText.append(results.getString("Month") + "  |  " + results.getString("Amount") + "  |  " + results.getString("Type") + "\n");
            }
            return reportText.toString();
        } catch (Exception ex) {
            System.out.println("Error in generating appts report by type and month");
            return "Oops! Something went wrong!";
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

    }
}
