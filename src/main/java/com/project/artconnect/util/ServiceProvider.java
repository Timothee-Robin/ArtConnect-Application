package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;
import com.project.artconnect.persistence.*;
import com.project.artconnect.ui.MainController;

/**
 * Service Provider to manage singleton instances of services.
 * 
 * Uses DatabaseConfig.USE_DATABASE to switch between:
 *   - true  -> Database-backed services (Supabase PostgreSQL)
 *   - false -> In-memory services (no database needed)
 */
public class ServiceProvider {

    private static final ArtistService artistService;
    private static final ArtworkService artworkService;
    private static final GalleryService galleryService;
    private static final WorkshopService workshopService;
    private static final CommunityService communityService;
    private static MainController mainController;

    static {
        if (DatabaseConfig.USE_DATABASE) {
            // ==========================================
            // DATABASE MODE (Supabase PostgreSQL)
            // ==========================================
            System.out.println("[ServiceProvider] Mode: DATABASE (Supabase)");

            JdbcArtistDao artistDao = new JdbcArtistDao();
            JdbcArtworkDao artworkDao = new JdbcArtworkDao();
            JdbcGalleryDao galleryDao = new JdbcGalleryDao();
            JdbcExhibitionDao exhibitionDao = new JdbcExhibitionDao();
            JdbcWorkshopDao workshopDao = new JdbcWorkshopDao();
            JdbcCommunityMemberDao communityMemberDao = new JdbcCommunityMemberDao();

            artistService = new DbArtistService(artistDao);
            artworkService = new DbArtworkService(artworkDao);
            galleryService = new DbGalleryService(galleryDao, exhibitionDao);
            workshopService = new DbWorkshopService(workshopDao);
            communityService = new DbCommunityService(communityMemberDao);

        } else {
            // ==========================================
            // IN-MEMORY MODE (no database needed)
            // ==========================================
            System.out.println("[ServiceProvider] Mode: IN-MEMORY");

            InMemoryArtistService memArtistService = new InMemoryArtistService();
            InMemoryArtworkService memArtworkService = new InMemoryArtworkService();
            InMemoryGalleryService memGalleryService = new InMemoryGalleryService();
            InMemoryWorkshopService memWorkshopService = new InMemoryWorkshopService();
            InMemoryCommunityService memCommunityService = new InMemoryCommunityService();

            // Initialize in-memory services with their dependencies
            memArtworkService.initData(memArtistService);
            memGalleryService.initData(memArtworkService);
            memWorkshopService.initData(memArtistService);
            memCommunityService.initData(memArtworkService);

            artistService = memArtistService;
            artworkService = memArtworkService;
            galleryService = memGalleryService;
            workshopService = memWorkshopService;
            communityService = memCommunityService;
        }
    }

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }

    /**
     * Returns the current mode as a user-friendly string.
     */
public static String getModeName() {
        return DatabaseConfig.USE_DATABASE ? "Supabase (PostgreSQL)" : "In-Memory";
    }

    public static MainController getMainController() {
        return mainController;
    }

    public static void setMainController(MainController controller) {
        mainController = controller;
    }
}
