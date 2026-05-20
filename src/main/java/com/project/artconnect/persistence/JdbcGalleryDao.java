package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT gallery_id, name, address, ownerName, openingHours, contactPhone, rating, website FROM Gallery WHERE gallery_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding gallery by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        String sql = "SELECT gallery_id, name, address, ownerName, openingHours, contactPhone, rating, website FROM Gallery";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                galleries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all galleries: " + e.getMessage());
        }
        return galleries;
    }

    @Override
    public void save(Gallery gallery) {
        String sql = "INSERT INTO Gallery (name, address, ownerName, openingHours, contactPhone, rating, website) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gallery.getName());
            ps.setString(2, gallery.getAddress());
            ps.setString(3, gallery.getOwnerName());
            ps.setString(4, gallery.getOpeningHours());
            ps.setString(5, gallery.getContactPhone());
            ps.setDouble(6, gallery.getRating());
            ps.setString(7, gallery.getWebsite());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) gallery.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            System.err.println("Error saving gallery: " + e.getMessage());
        }
    }

    @Override
    public void update(Gallery gallery) {
        String sql = "UPDATE Gallery SET name=?, address=?, ownerName=?, openingHours=?, contactPhone=?, rating=?, website=? WHERE gallery_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gallery.getName());
            ps.setString(2, gallery.getAddress());
            ps.setString(3, gallery.getOwnerName());
            ps.setString(4, gallery.getOpeningHours());
            ps.setString(5, gallery.getContactPhone());
            ps.setDouble(6, gallery.getRating());
            ps.setString(7, gallery.getWebsite());
            ps.setLong(8, gallery.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating gallery: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Gallery WHERE gallery_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting gallery: " + e.getMessage());
        }
    }

    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setId(rs.getLong("gallery_id"));
        g.setName(rs.getString("name"));
        g.setAddress(rs.getString("address"));
        g.setOwnerName(rs.getString("ownername"));
        g.setOpeningHours(rs.getString("openinghours"));
        g.setContactPhone(rs.getString("contactphone"));
        g.setRating(rs.getDouble("rating"));
        g.setWebsite(rs.getString("website"));
        return g;
    }
}