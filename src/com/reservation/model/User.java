package com.reservation.model;

/**
 * Model class representing a User (either a Customer or an Administrator).
 * Demonstrates encapsulation with private attributes and public getters/setters.
 */
public class User {
    private String username;
    private String password;
    private String role; // USER or ADMIN
    private String name;
    private String phone;

    // Constructors
    public User() {
    }

    public User(String username, String password, String role, String name, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.phone = phone;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Checks if this user has Administrator privileges.
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
