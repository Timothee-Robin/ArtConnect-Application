CREATE INDEX idx_artwork_artist_id ON Artwork(Artist_ID);
CREATE INDEX idx_artwork_exhibition_id ON Artwork(Exhibition_ID);
CREATE INDEX idx_exhibition_gallery_id ON Exhibition(Gallery_ID);
CREATE INDEX idx_workshop_artist_id ON Workshop(Artist_ID);




CREATE INDEX idx_artwork_statut ON Artwork(statut);

CREATE INDEX idx_artist_city ON Artist(city);

CREATE INDEX idx_artwork_type ON Artwork(type);