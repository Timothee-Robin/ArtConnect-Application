package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for GalleryDao.
 * Uses PreparedStatements and properly closes resources.
 */
public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT * FROM Gallery WHERE Gallery_ID = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching gallery by id: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        String sql = "SELECT * FROM Gallery ORDER BY Gallery_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                galleries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all galleries: " + e.getMessage());
            e.printStackTrace();
        }
        return galleries;
    }

    /**
     * Maps a ResultSet row to a Gallery object.
     */
    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setName(rs.getString("name"));
        g.setAddress(rs.getString("adress"));
        g.setOwnerName(rs.getString("ownername"));
        g.setOpeningHours(rs.getString("openinghours"));
        g.setContactPhone(rs.getString("contactphone"));
        g.setRating(rs.getDouble("rating"));
        g.setWebsite(rs.getString("website"));
        return g;
    }
}
