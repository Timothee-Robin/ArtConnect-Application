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
        for (Gallery g : galleries) {
            for (Exhibition e : allExhibitions) {
                if (e.getGallery() != null && e.getGallery().getName() != null
                        && e.getGallery().getName().equals(g.getName())) {
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