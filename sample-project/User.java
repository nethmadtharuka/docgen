package com.example.model;

/**
 * User - Represents a user in the system.
 *
 * This is a sample file to test our documentation generator.
 */
public class User {

    private Long id;
    private String username;
    private String email;
    private boolean active;

    /**
     * Default constructor
     */
    public User() {
    }

    /**
     * Create a new user with all fields
     *
     * @param id User ID
     * @param username User's username
     * @param email User's email address
     */
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.active = true;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}