package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for CommunityMemberDao.
 * Uses PreparedStatements and properly closes resources.
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findById(Long id) {
        String sql = "SELECT * FROM Community_Member WHERE CommunityMember_ID = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching community member by id: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> members = new ArrayList<>();
        String sql = "SELECT * FROM Community_Member ORDER BY CommunityMember_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all community members: " + e.getMessage());
            e.printStackTrace();
        }
        return members;
    }

    @Override
    public Optional<CommunityMember> findByName(String name) {
        String sql = "SELECT * FROM Community_Member WHERE name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(CommunityMember member) {
        String sql = "INSERT INTO Community_Member (name, email, password, birthYear, phone, city, membershipType, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPassword());
            if (member.getBirthYear() != null) {
                ps.setInt(4, member.getBirthYear());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, member.getPhone());
            ps.setString(6, member.getCity());
            ps.setString(7, member.getMembershipType());
            ps.setString(8, member.getRole());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maps a ResultSet row to a CommunityMember object.
     */
    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember m = new CommunityMember();
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        try {
            m.setPassword(rs.getString("password"));
        } catch (SQLException ignore) { /* Handle missing column softly here or ignore */ }
        int birthYear = rs.getInt("birthyear");
        m.setBirthYear(rs.wasNull() ? null : birthYear);
        m.setPhone(rs.getString("phone"));
        m.setCity(rs.getString("city"));
        m.setMembershipType(rs.getString("membershiptype"));
        m.setRole(rs.getString("role"));
        return m;
    }
}
