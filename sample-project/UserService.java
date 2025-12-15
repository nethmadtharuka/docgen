package com.example.service;

import com.example.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserService - Handles user-related business logic.
 *
 * This service provides methods to:
 * - Create new users
 * - Find users by ID or username
 * - List all users
 * - Deactivate users
 */
public class UserService {

    // In-memory storage for demo purposes
    private List<User> users = new ArrayList<>();
    private Long nextId = 1L;

    /**
     * Create a new user
     *
     * @param username The username (must be unique)
     * @param email The user's email address
     * @return The created user with assigned ID
     * @throws IllegalArgumentException if username already exists
     */
    public User createUser(String username, String email) {
        // Check if username exists
        if (findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User(nextId++, username, email);
        users.add(user);
        return user;
    }

    /**
     * Find a user by their ID
     *
     * @param id The user ID to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    /**
     * Find a user by username
     *
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Get all users
     *
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Get only active users
     *
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        return users.stream()
                .filter(User::isActive)
                .toList();
    }

    /**
     * Deactivate a user by ID
     *
     * @param id The user ID
     * @return true if user was found and deactivated
     */
    public boolean deactivateUser(Long id) {
        Optional<User> user = findById(id);
        if (user.isPresent()) {
            user.get().setActive(false);
            return true;
        }
        return false;
    }

    /**
     * Get total user count
     *
     * @return Number of users in the system
     */
    public int getUserCount() {
        return users.size();
    }
}