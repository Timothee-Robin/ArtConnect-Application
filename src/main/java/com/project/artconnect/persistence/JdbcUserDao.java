package com.project.artconnect.persistence;

import com.project.artconnect.dao.UserDao;
import com.project.artconnect.model.User;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM app_users WHERE username = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    user.setId(rs.getInt("id"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO app_users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // In a real app this should be hashed
            pstmt.setString(3, user.getRole());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
