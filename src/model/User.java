package model;

public class User {
    private static String userId;
    private static String username;
    private static boolean isLoggedIn;


    public User(String userId, String username, boolean isLoggedIn) {
        this.userId = userId;
        this.username = username;
        this.isLoggedIn = isLoggedIn;
    }

    public static String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static boolean isIsLoggedIn() {
        return isLoggedIn;
    }

    public static void setIsLoggedIn(boolean isLoggedIn) {
        User.isLoggedIn = isLoggedIn;
    }

    public static String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
