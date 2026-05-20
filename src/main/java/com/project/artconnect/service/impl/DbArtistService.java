package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database-backed implementation of ArtistService.
 * Delegates CRUD operations to JdbcArtistDao and adds service-level logic.
 */
public class DbArtistService implements ArtistService {

    private final ArtistDao artistDao;

    public DbArtistService(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    @Override
    public List<Artist> getAllArtists() {
        List<Artist> artists = artistDao.findAll();
        // Load disciplines for each artist
        for (Artist artist : artists) {
            artist.setDisciplines(loadDisciplinesForArtist(artist.getName()));
        }
        return artists;
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        return artistDao.findAll().stream()
                .filter(a -> a.getName() != null && a.getName().equals(name))
                .findFirst()
                .map(a -> {
                    a.setDisciplines(loadDisciplinesForArtist(a.getName()));
                    return a;
                });
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(Long id) {
        artistDao.delete(id);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        List<Discipline> disciplines = new ArrayList<>();
        String sql = "SELECT name FROM Discipline ORDER BY Discipline_ID";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                disciplines.add(new Discipline(rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching disciplines: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplines;
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        List<Artist> all = getAllArtists();
        return all.stream()
                .filter(a -> query == null || query.isEmpty()
                        || a.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> city == null || city.isEmpty()
                        || (a.getCity() != null && a.getCity().equalsIgnoreCase(city)))
                .filter(a -> disciplineName == null || disciplineName.isEmpty()
                        || a.getDisciplines().stream().anyMatch(d -> d.getName().equals(disciplineName)))
                .collect(Collectors.toList());
    }

    /**
     * Loads the list of disciplines for a given artist from the Practice join table.
     */
    private List<Discipline> loadDisciplinesForArtist(String artistName) {
        List<Discipline> disciplines = new ArrayList<>();
        String sql = "SELECT d.name FROM Discipline d "
                   + "JOIN Practice p ON d.Discipline_ID = p.Discipline_ID "
                   + "JOIN Artist a ON a.Artist_ID = p.Artist_ID "
                   + "WHERE a.name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disciplines.add(new Discipline(rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading disciplines for artist: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplines;
    }
}
