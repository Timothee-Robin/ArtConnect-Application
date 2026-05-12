package com.project.artconnect.dao;

import com.project.artconnect.model.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    void save(User user);
}
