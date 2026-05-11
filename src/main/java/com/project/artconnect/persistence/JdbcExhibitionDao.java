package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ExhibitionDao.
 * Uses PreparedStatements and properly closes resources.
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> exhibitions = new ArrayList<>();
        String sql = "SELECT e.*, g.name AS gallery_name, g.adress AS gallery_address, g.rating AS gallery_rating "
                   + "FROM Exhibition e "
                   + "LEFT JOIN Gallery g ON e.Gallery_ID = g.Gallery_ID "
                   + "ORDER BY e.Exhibition_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                exhibitions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all exhibitions: " + e.getMessage());
            e.printStackTrace();
        }
        return exhibitions;
    }

    @Override
    public void save(Exhibition exhibition) {
        String sql = "INSERT INTO Exhibition (title, startDate, endDate, Description, curatorName, theme, Gallery_ID) "
                   + "VALUES (?, ?, ?, ?, ?, ?, (SELECT Gallery_ID FROM Gallery WHERE name = ?))";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, exhibition.getTitle());
            ps.setDate(2, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(3, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(4, exhibition.getDescription());
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());
            ps.setString(7, exhibition.getGallery() != null ? exhibition.getGallery().getName() : null);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String sql = "UPDATE Exhibition SET startDate = ?, endDate = ?, Description = ?, "
                   + "curatorName = ?, theme = ? WHERE title = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(2, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(3, exhibition.getDescription());
            ps.setString(4, exhibition.getCuratorName());
            ps.setString(5, exhibition.getTheme());
            ps.setString(6, exhibition.getTitle());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM Exhibition WHERE title = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maps a ResultSet row to an Exhibition object, including the linked Gallery.
     */
    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition e = new Exhibition();
        e.setTitle(rs.getString("title"));

        Date startDate = rs.getDate("startdate");
        if (startDate != null) e.setStartDate(startDate.toLocalDate());

        Date endDate = rs.getDate("enddate");
        if (endDate != null) e.setEndDate(endDate.toLocalDate());

        e.setDescription(rs.getString("description"));
        e.setCuratorName(rs.getString("curatorname"));
        e.setTheme(rs.getString("theme"));

        // Map the joined Gallery
        String galleryName = rs.getString("gallery_name");
        if (galleryName != null) {
            Gallery g = new Gallery();
            g.setName(galleryName);
            g.setAddress(rs.getString("gallery_address"));
            g.setRating(rs.getDouble("gallery_rating"));
            e.setGallery(g);
        }

        return e;
    }
}
