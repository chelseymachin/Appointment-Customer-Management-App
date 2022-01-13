package model;

public class User {
    private Integer userId;
    private String username;
    private boolean isLoggedIn;


    public User(String userId, String username, boolean isLoggedIn) {
        this.userId = Integer.parseInt(userId);
        this.username = username;
        this.isLoggedIn = isLoggedIn;
    }

    public User(Integer userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public Integer getUserId() {
        return userId;
    }


    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override public String toString() {
        return (Integer.toString(userId) + " - " + username);
    }
}
