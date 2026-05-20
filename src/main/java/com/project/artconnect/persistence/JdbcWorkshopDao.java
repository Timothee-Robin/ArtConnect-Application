package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for WorkshopDao.
 * Uses PreparedStatements and properly closes resources.
 */
public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = "SELECT w.*, a.name AS artist_name "
                   + "FROM Workshop w "
                   + "LEFT JOIN Artist a ON w.Artist_ID = a.Artist_ID "
                   + "WHERE w.Workshop_ID = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching workshop by id: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> workshops = new ArrayList<>();
        String sql = "SELECT w.*, a.name AS artist_name "
                   + "FROM Workshop w "
                   + "LEFT JOIN Artist a ON w.Artist_ID = a.Artist_ID "
                   + "ORDER BY w.Workshop_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                workshops.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all workshops: " + e.getMessage());
            e.printStackTrace();
        }
        return workshops;
    }

    /**
     * Maps a ResultSet row to a Workshop object, including the linked Artist (instructor).
     */
    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setTitle(rs.getString("title"));

        Timestamp ts = rs.getTimestamp("workshopdate");
        if (ts != null) {
            w.setDate(ts.toLocalDateTime());
        }

        w.setDurationMinutes(rs.getInt("duration"));
        w.setMaxParticipants(rs.getInt("maxparticipant"));
        w.setPrice(rs.getDouble("price"));
        w.setLocation(rs.getString("location"));
        w.setDescription(rs.getString("description"));
        w.setLevel(rs.getString("level"));

        // Map the joined Artist as instructor
        String artistName = rs.getString("artist_name");
        if (artistName != null) {
            Artist instructor = new Artist();
            instructor.setName(artistName);
            w.setInstructor(instructor);
        }

        return w;
    }

    @Override
    public void save(Workshop workshop) {
        String sql = "INSERT INTO Workshop (title, workshopdate, duration, maxparticipant, price, location, description, level, Artist_ID) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT Artist_ID FROM Artist WHERE name = ?))";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, workshop.getTitle());
            if (workshop.getDate() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(workshop.getDate()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, workshop.getDurationMinutes());
            ps.setInt(4, workshop.getMaxParticipants());
            ps.setDouble(5, workshop.getPrice());
            ps.setString(6, workshop.getLocation());
            ps.setString(7, workshop.getDescription());
            ps.setString(8, workshop.getLevel());
            if (workshop.getInstructor() != null && workshop.getInstructor().getName() != null) {
                ps.setString(9, workshop.getInstructor().getName());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Workshop workshop) {
        String sql = "UPDATE Workshop SET workshopdate=?, duration=?, maxparticipant=?, price=?, location=?, description=?, level=?, Artist_ID=(SELECT Artist_ID FROM Artist WHERE name=?) WHERE title=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            if (workshop.getDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(workshop.getDate()));
            } else {
                ps.setNull(1, Types.TIMESTAMP);
            }
            ps.setInt(2, workshop.getDurationMinutes());
            ps.setInt(3, workshop.getMaxParticipants());
            ps.setDouble(4, workshop.getPrice());
            ps.setString(5, workshop.getLocation());
            ps.setString(6, workshop.getDescription());
            ps.setString(7, workshop.getLevel());
            if (workshop.getInstructor() != null && workshop.getInstructor().getName() != null) {
                ps.setString(8, workshop.getInstructor().getName());
            } else {
                ps.setNull(8, Types.VARCHAR);
            }
            ps.setString(9, workshop.getTitle());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM Workshop WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
