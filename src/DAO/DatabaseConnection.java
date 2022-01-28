package DAO;

import java.sql.Connection;
import java.sql.DriverManager;

public abstract class DatabaseConnection {
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?useLegacyDatetimeCode=false&serverTimezone=UTC&connectionTimeZone = LOCAL"; // LOCAL
    private static final String driver = "com.mysql.cj.jdbc.Driver"; // Driver reference
    private static final String userName = "sqlUser"; // Username
    private static String password = "Passw0rd!"; // Password
    public static Connection connection;  // Connection Interface

    /**
     * This function opens the database connection to the SQL server and returns it
     * @return JDBC Connection object that is open
     */
    public static Connection openConnection() {
        try {
            Class.forName(driver); // Locate Driver
            connection = DriverManager.getConnection(jdbcUrl, userName, password); // Reference Connection object
        }
        catch(Exception exception)
        {
            System.out.println("Problem opening DB connection");
        }
        return connection;
    }

    /** This function closes an already open database connection */
    public static void closeConnection() {
        try {
            connection.close();
        }
        catch(Exception exception)
        {
            System.out.println("Problem closing DB connection");
        }
    }
}
