package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtworkService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of ArtworkService.
 * Delegates CRUD operations to JdbcArtworkDao.
 */
public class DbArtworkService implements ArtworkService {

    private final ArtworkDao artworkDao;

    public DbArtworkService(ArtworkDao artworkDao) {
        this.artworkDao = artworkDao;
    }

    @Override
    public List<Artwork> getAllArtworks() {
        return artworkDao.findAll();
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        return artworkDao.findAll().stream()
                .filter(a -> a.getTitle() != null && a.getTitle().equals(title))
                .findFirst();
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null || artist.getName() == null) return Collections.emptyList();
        return artworkDao.findByArtistName(artist.getName());
    }

    @Override
    public void createArtwork(Artwork artwork) {
        artworkDao.save(artwork);
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        artworkDao.update(artwork);
    }

    @Override
    public void deleteArtwork(Long id) {
        artworkDao.delete(id);
    }
}
