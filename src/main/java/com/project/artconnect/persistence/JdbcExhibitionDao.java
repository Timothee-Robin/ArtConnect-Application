package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcExhibitionDao implements ExhibitionDao {

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> exhibitions = new ArrayList<>();
        String sql = "SELECT e.Exhibition_ID, e.title, e.startDate, e.endDate, e.description, e.curatorName, e.theme, " +
                     "g.gallery_id, g.name AS gallery_name, g.address AS gallery_address, g.ownername, " +
                     "g.openinghours, g.contactphone, g.rating, g.website " +
                     "FROM Exhibition e LEFT JOIN Gallery g ON e.gallery_id = g.gallery_id";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Exhibition e = new Exhibition();
                e.setId(rs.getLong("exhibition_id"));
                e.setTitle(rs.getString("title"));
                e.setStartDate(rs.getDate("startdate") != null ? rs.getDate("startdate").toLocalDate() : null);
                e.setEndDate(rs.getDate("enddate") != null ? rs.getDate("enddate").toLocalDate() : null);
                e.setDescription(rs.getString("description"));
                e.setCuratorName(rs.getString("curatorname"));
                e.setTheme(rs.getString("theme"));

                Long galleryId = rs.getLong("gallery_id");
                if (!rs.wasNull()) {
                    Gallery g = new Gallery();
                    g.setId(galleryId);
                    g.setName(rs.getString("gallery_name"));
                    g.setAddress(rs.getString("gallery_address"));
                    g.setOwnerName(rs.getString("ownername"));
                    g.setOpeningHours(rs.getString("openinghours"));
                    g.setContactPhone(rs.getString("contactphone"));
                    g.setRating(rs.getDouble("rating"));
                    g.setWebsite(rs.getString("website"));
                    e.setGallery(g);
                }
                exhibitions.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all exhibitions: " + e.getMessage());
        }
        return exhibitions;
    }

    @Override
    public void save(Exhibition exhibition) {
        String sql = "INSERT INTO Exhibition (title, startDate, endDate, description, curatorName, theme, gallery_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, exhibition.getTitle());
            ps.setDate(2, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(3, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(4, exhibition.getDescription());
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());
            if (exhibition.getGallery() != null && exhibition.getGallery().getId() != null) {
                ps.setLong(7, exhibition.getGallery().getId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) exhibition.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            System.err.println("Error saving exhibition: " + e.getMessage());
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String sql = "UPDATE Exhibition SET title=?, startDate=?, endDate=?, description=?, curatorName=?, theme=?, gallery_id=? WHERE exhibition_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exhibition.getTitle());
            ps.setDate(2, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(3, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(4, exhibition.getDescription());
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());
            if (exhibition.getGallery() != null && exhibition.getGallery().getId() != null) {
                ps.setLong(7, exhibition.getGallery().getId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            ps.setLong(8, exhibition.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating exhibition: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Exhibition WHERE exhibition_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting exhibition: " + e.getMessage());
        }
    }
}