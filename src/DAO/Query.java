package DAO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static DAO.DatabaseConnection.connection;

public class Query {
    private static ResultSet selection;
    private static String input;
    private static Statement statement;

    public static boolean loginAttempt(String username, String password) {
        try{
            DatabaseConnection.openConnection();
            PreparedStatement pst = connection.prepareStatement("SELECT * FROM users WHERE User_Name=? AND Password=?");
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

}
