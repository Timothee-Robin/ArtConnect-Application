package com.project.artconnect.service;

import com.project.artconnect.model.User;
import java.util.Optional;

public interface UserService {
    /**
     * Authenticates a user with username and password.
     * @return the User if successful, or empty if invalid logic.
     */
    Optional<User> authenticate(String username, String password);

    /**
     * Registers a new user.
     * @return true if successful, false if the username already exists.
     */
    boolean register(String username, String password, String role);
}
