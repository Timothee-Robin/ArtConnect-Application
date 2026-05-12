package com.project.artconnect.service.impl;

import com.project.artconnect.model.User;
import com.project.artconnect.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserService implements UserService {

    private final Map<String, User> mockDb = new HashMap<>();

    public InMemoryUserService() {
        // sample admin admin
        mockDb.put("admin", new User("admin", "admin", "admin"));
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        if (mockDb.containsKey(username) && mockDb.get(username).getPassword().equals(password)) {
            return Optional.of(mockDb.get(username));
        }
        return Optional.empty();
    }

    @Override
    public boolean register(String username, String password, String role) {
        if (mockDb.containsKey(username)) {
            return false;
        }
        mockDb.put(username, new User(username, password, role));
        return true;
    }
}
