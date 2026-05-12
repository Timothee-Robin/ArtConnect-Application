package com.project.artconnect.service.impl;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of CommunityService.
 * Delegates to JdbcCommunityMemberDao and queries reviews from the Rate table.
 */
public class DbCommunityService implements CommunityService {

    private final CommunityMemberDao memberDao;

    public DbCommunityService(CommunityMemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public Optional<CommunityMember> authenticate(String username, String password) {
        Optional<CommunityMember> optMem = memberDao.findByName(username);
        if (optMem.isPresent()) {
            CommunityMember m = optMem.get();
            if (m.getPassword() != null && m.getPassword().equals(password)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean register(CommunityMember member) {
        if (memberDao.findByName(member.getName()).isPresent()) {
            return false; // username (name) already exists
        }
        memberDao.save(member);
        return true;
    }

    @Override
    public List<CommunityMember> getAllMembers() {
        return memberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        return memberDao.findAll().stream()
                .filter(m -> m.getName() != null && m.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        if (member == null) return Collections.emptyList();

        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.Rating, r.Comment, r.Review_Date, aw.title AS artwork_title "
                   + "FROM Rate r "
                   + "JOIN Artwork aw ON r.Artwork_ID = aw.Artwork_ID "
                   + "JOIN Community_Member cm ON r.CommunityMember_ID = cm.CommunityMember_ID "
                   + "WHERE cm.name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork artwork = new Artwork();
                    artwork.setTitle(rs.getString("artwork_title"));

                    Review r = new Review();
                    r.setReviewer(member);
                    r.setArtwork(artwork);
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));

                    Date reviewDate = rs.getDate("review_date");
                    if (reviewDate != null) r.setReviewDate(reviewDate.toLocalDate());

                    reviews.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
            e.printStackTrace();
        }
        return reviews;
    }
}
