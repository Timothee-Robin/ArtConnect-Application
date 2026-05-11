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

    /**
     * Maps a ResultSet row to a CommunityMember object.
     */
    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember m = new CommunityMember();
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        int birthYear = rs.getInt("birthyear");
        m.setBirthYear(rs.wasNull() ? null : birthYear);
        m.setPhone(rs.getString("phone"));
        m.setCity(rs.getString("city"));
        m.setMembershipType(rs.getString("membershiptype"));
        return m;
    }
}
