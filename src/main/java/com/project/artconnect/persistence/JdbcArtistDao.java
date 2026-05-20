package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtistDao.
 * Uses PreparedStatements for all operations and properly closes resources.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM Artist ORDER BY Artist_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                artists.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all artists: " + e.getMessage());
            e.printStackTrace();
        }
        return artists;
    }

    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO Artist (name, bio, contact_email, phone, city, website, socialMedia, isActive, birthYear) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBio());
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getPhone());
            ps.setString(5, artist.getCity());
            ps.setString(6, artist.getWebsite());
            ps.setString(7, artist.getSocialMedia());
            ps.setBoolean(8, artist.isActive());
            if (artist.getBirthYear() != null) {
                ps.setInt(9, artist.getBirthYear());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving artist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Artist artist) {
        String sql = "UPDATE Artist SET name = ?, bio = ?, contact_email = ?, phone = ?, city = ?, "
                   + "website = ?, socialMedia = ?, isActive = ?, birthYear = ? WHERE Artist_ID = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBio());
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getPhone());
            ps.setString(5, artist.getCity());
            ps.setString(6, artist.getWebsite());
            ps.setString(7, artist.getSocialMedia());
            ps.setBoolean(8, artist.isActive());
            if (artist.getBirthYear() != null) {
                ps.setInt(9, artist.getBirthYear());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setLong(10, artist.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating artist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String deactivateSql = "UPDATE Artist SET isActive = false WHERE Artist_ID = ?";
        String deleteSql = "DELETE FROM Artist WHERE Artist_ID = ?";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // First deactivate (satisfies the trigger)
                try (PreparedStatement ps = conn.prepareStatement(deactivateSql)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                // Then delete
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting artist: " + e.getMessage());
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM Artist WHERE city = ? ORDER BY Artist_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    artists.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching artists by city: " + e.getMessage());
            e.printStackTrace();
        }
        return artists;
    }

    /**
     * Maps a ResultSet row to an Artist object.
     */
    private Artist mapRow(ResultSet rs) throws SQLException {
        Artist a = new Artist();
        a.setId(rs.getLong("artist_id"));
        a.setName(rs.getString("name"));
        a.setBio(rs.getString("bio"));
        a.setContactEmail(rs.getString("contact_email"));
        a.setPhone(rs.getString("phone"));
        a.setCity(rs.getString("city"));
        a.setWebsite(rs.getString("website"));
        a.setSocialMedia(rs.getString("socialmedia"));
        a.setActive(rs.getBoolean("isactive"));
        int birthYear = rs.getInt("birthyear");
        a.setBirthYear(rs.wasNull() ? null : birthYear);
        return a;
    }
}