package com.project.artconnect.service.impl;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of WorkshopService.
 * Delegates to JdbcWorkshopDao and queries bookings from the Books table.
 */
public class DbWorkshopService implements WorkshopService {

    private final WorkshopDao workshopDao;

    public DbWorkshopService(WorkshopDao workshopDao) {
        this.workshopDao = workshopDao;
    }

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        return workshopDao.findAll().stream()
                .filter(w -> w.getTitle() != null && w.getTitle().equals(title))
                .findFirst();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) return;

        String sql = "INSERT INTO Books (CommunityMember_ID, Workshop_ID, Payment_Status) "
                   + "VALUES ((SELECT CommunityMember_ID FROM Community_Member WHERE name = ?), "
                   + "(SELECT Workshop_ID FROM Workshop WHERE title = ?), 'Pending')";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            ps.setString(2, workshop.getTitle());
            ps.executeUpdate();

            // Also update the in-memory object
            Booking b = new Booking(workshop, member);
            member.addBooking(b);
        } catch (SQLException e) {
            System.err.println("Error booking workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) return Collections.emptyList();

        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.Booking_date, b.Payment_Status, w.title AS workshop_title "
                   + "FROM Books b "
                   + "JOIN Workshop w ON b.Workshop_ID = w.Workshop_ID "
                   + "JOIN Community_Member cm ON b.CommunityMember_ID = cm.CommunityMember_ID "
                   + "WHERE cm.name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workshop w = new Workshop();
                    w.setTitle(rs.getString("workshop_title"));

                    Booking b = new Booking(w, member);
                    Timestamp ts = rs.getTimestamp("booking_date");
                    if (ts != null) b.setBookingDate(ts.toLocalDateTime());
                    b.setPaymentStatus(rs.getString("payment_status"));
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }
}
