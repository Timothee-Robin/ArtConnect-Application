package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtworkDao.
 * Uses PreparedStatements for all operations and properly closes resources.
 */
public class JdbcArtworkDao implements ArtworkDao {

    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.*, a.Artist_ID AS artist_id, a.name AS artist_name, a.bio AS artist_bio, "
                   + "a.contact_email AS artist_email, a.city AS artist_city, a.birthyear AS artist_birthyear "
                   + "FROM Artwork aw "
                   + "LEFT JOIN Artist a ON aw.Artist_ID = a.Artist_ID "
                   + "ORDER BY aw.Artwork_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                artworks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all artworks: " + e.getMessage());
            e.printStackTrace();
        }
        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        String sql = "INSERT INTO Artwork (title, creationYear, type, medium, dimension, description, price, statut, Exhibition_ID, Artist_ID) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artwork.getTitle());
            if (artwork.getCreationYear() != null) {
                ps.setInt(2, artwork.getCreationYear());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, artwork.getType());
            ps.setString(4, artwork.getMedium());
            ps.setString(5, artwork.getDimensions());
            ps.setString(6, artwork.getDescription());
            ps.setDouble(7, artwork.getPrice());
            ps.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
            ps.setNull(9, Types.INTEGER); // Exhibition_ID handled separately if needed
            if (artwork.getArtist() != null && artwork.getArtist().getId() != null) {
                ps.setLong(10, artwork.getArtist().getId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Artwork artwork) {
        String sql = "UPDATE Artwork SET title = ?, creationYear = ?, type = ?, medium = ?, dimension = ?, "
                   + "description = ?, price = ?, statut = ? WHERE Artwork_ID = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artwork.getTitle());
            if (artwork.getCreationYear() != null) {
                ps.setInt(2, artwork.getCreationYear());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, artwork.getType());
            ps.setString(4, artwork.getMedium());
            ps.setString(5, artwork.getDimensions());
            ps.setString(6, artwork.getDescription());
            ps.setDouble(7, artwork.getPrice());
            ps.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
            ps.setLong(9, artwork.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Artwork WHERE Artwork_ID = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.*, a.Artist_ID AS artist_id, a.name AS artist_name, a.bio AS artist_bio, "
                   + "a.contact_email AS artist_email, a.city AS artist_city, a.birthyear AS artist_birthyear "
                   + "FROM Artwork aw "
                   + "LEFT JOIN Artist a ON aw.Artist_ID = a.Artist_ID "
                   + "WHERE a.name = ? ORDER BY aw.Artwork_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    artworks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching artworks by artist: " + e.getMessage());
            e.printStackTrace();
        }
        return artworks;
    }

    @Override
    public List<Artwork> findById(Long id) {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.*, a.Artist_ID AS artist_id, a.name AS artist_name, a.bio AS artist_bio, "
                   + "a.contact_email AS artist_email, a.city AS artist_city, a.birthyear AS artist_birthyear "
                   + "FROM Artwork aw "
                   + "LEFT JOIN Artist a ON aw.Artist_ID = a.Artist_ID "
                   + "WHERE aw.Artwork_ID = ? ORDER BY aw.Artwork_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    artworks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching artwork by id: " + e.getMessage());
            e.printStackTrace();
        }
        return artworks;
    }

    /**
     * Maps a ResultSet row to an Artwork object, including the linked Artist.
     */
    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artwork aw = new Artwork();
        aw.setId(rs.getLong("artwork_id"));
        aw.setTitle(rs.getString("title"));
        int year = rs.getInt("creationyear");
        aw.setCreationYear(rs.wasNull() ? null : year);
        aw.setType(rs.getString("type"));
        aw.setMedium(rs.getString("medium"));
        aw.setDimensions(rs.getString("dimension"));
        aw.setDescription(rs.getString("description"));
        aw.setPrice(rs.getDouble("price"));

        String statut = rs.getString("statut");
        if (statut != null) {
            try {
                aw.setStatus(Artwork.Status.valueOf(statut));
            } catch (IllegalArgumentException e) {
                aw.setStatus(Artwork.Status.FOR_SALE);
            }
        }

        // Map the joined Artist
        String artistName = rs.getString("artist_name");
        if (artistName != null) {
            Artist artist = new Artist();
            artist.setId(rs.getLong("artist_id"));
            artist.setName(artistName);
            artist.setBio(rs.getString("artist_bio"));
            artist.setContactEmail(rs.getString("artist_email"));
            artist.setCity(rs.getString("artist_city"));
            int aby = rs.getInt("artist_birthyear");
            artist.setBirthYear(rs.wasNull() ? null : aby);
            aw.setArtist(artist);
        }

        return aw;
    }
}