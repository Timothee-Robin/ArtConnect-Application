package com.project.artconnect.service.impl;

import com.project.artconnect.dao.UserDao;
import com.project.artconnect.model.User;
import com.project.artconnect.service.UserService;

import java.util.Optional;

public class DbUserService implements UserService {

    private final UserDao userDao;

    public DbUserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> optUser = userDao.findByUsername(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            // Real apps use bcrypt, not plaintext
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean register(String username, String password, String role) {
        if (userDao.findByUsername(username).isPresent()) {
            return false; // Username taken
        }
        User newUser = new User(username, password, role);
        userDao.save(newUser);
        return true;
    }
}
