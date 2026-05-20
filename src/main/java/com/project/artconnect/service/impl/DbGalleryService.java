package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DbGalleryService implements GalleryService {

    private final GalleryDao galleryDao;
    private final ExhibitionDao exhibitionDao;

    public DbGalleryService(GalleryDao galleryDao, ExhibitionDao exhibitionDao) {
        this.galleryDao = galleryDao;
        this.exhibitionDao = exhibitionDao;
    }

    @Override
    public List<Gallery> getAllGalleries() {
        List<Gallery> galleries = galleryDao.findAll();
        List<Exhibition> allExhibitions = exhibitionDao.findAll();

        java.util.Map<Long, Gallery> galleryMap = new java.util.HashMap<>();
        for (Gallery g : galleries) {
            if (g.getId() != null) {
                galleryMap.put(g.getId(), g);
            }
        }

        // Associate exhibitions with their respective galleries
        for (Exhibition e : allExhibitions) {
            if (e.getGallery() != null && e.getGallery().getId() != null) {
                Gallery g = galleryMap.get(e.getGallery().getId());
                if (g != null) {
                    e.setGallery(g);
                    g.getExhibitions().add(e);
                }
            }
        }
        return galleries;
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return getAllGalleries().stream()
                .filter(g -> g.getName() != null && g.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) return Collections.emptyList();
        if (gallery.getExhibitions() != null && !gallery.getExhibitions().isEmpty()) {
            return gallery.getExhibitions();
        }
        return getAllGalleries().stream()
                .filter(g -> g.getName() != null && g.getName().equals(gallery.getName()))
                .findFirst()
                .map(Gallery::getExhibitions)
                .orElse(Collections.emptyList());
    }

    @Override
    public Gallery createGallery(Gallery gallery) {
        galleryDao.save(gallery);
        return gallery;
    }

    @Override
    public Gallery updateGallery(Gallery gallery) {
        galleryDao.update(gallery);
        return gallery;
    }

    @Override
    public void deleteGallery(Long id) {
        galleryDao.delete(id);
    }

    @Override
    public void addExhibitionToGallery(Exhibition exhibition) {
        exhibitionDao.save(exhibition);
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        exhibitionDao.update(exhibition);
    }

    @Override
    public void deleteExhibition(Long id) {
        exhibitionDao.delete(id);
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        return exhibitionDao.findAll();
    }
}