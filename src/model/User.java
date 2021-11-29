package model;

public class User {
    private static Integer userId;
    private static String username;
    private static boolean isLoggedIn;


    public User(String userId, String username, boolean isLoggedIn) {
        this.userId = Integer.parseInt(userId);
        this.username = username;
        this.isLoggedIn = isLoggedIn;
    }

    public User(Integer userId, String username) {
        this.userId = userId;
        this.username = username;
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

    public static Integer getUserId() {
        return userId;
    }


    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override public String toString() {
        return (Integer.toString(userId) + " - " + username);
    }
}
