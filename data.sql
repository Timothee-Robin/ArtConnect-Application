-- 1. Insert Artists (Independent Entity)
INSERT INTO Artist (Artist_ID, name, bio, contact_email, phone, city, website, socialMedia, isActive, birthYear) VALUES
(1, 'Elena Rossi', 'Contemporary painter focusing on urban landscapes.', 'elena@email.com', '1234567890', 'Rome', 'elenarossi.com', '@elenarossi_art', TRUE, 1985),
(2, 'Marcus Chen', 'Digital artist and photographer blending tech and nature.', 'marcus@email.com', '0987654321', 'New York', 'marcuschen.net', '@marcus_c', TRUE, 1990),
(3, 'Sarah Jenkins', 'Sculptor working exclusively with recycled materials.', 'sarah@email.com', '5551234567', 'London', 'sjenkins.org', '@sarah_sculpts', TRUE, 1978);

-- 2. Insert Galleries (Independent Entity)
INSERT INTO Gallery (Gallery_ID, name, address, ownerName, openingHours, contactPhone, rating, website) VALUES
(1, 'Lumiere Gallery', '15 Art Avenue, Paris', 'Jean Dupont', 'Mon-Sat 10:00-18:00', '33140203040', 4.8, 'lumieregallery.fr'),
(2, 'Modern Hub', '100 Broadway, NY', 'Alice Smith', 'Tue-Sun 11:00-19:00', '12125550199', 4.5, 'modernhub.nyc');

-- 3. Insert Exhibitions (Depends on Gallery)
INSERT INTO Exhibition (Exhibition_ID, title, startDate, endDate, Description, curatorName, theme, Gallery_ID) VALUES
(1, 'Urban Echoes', '2026-05-01', '2026-06-15', 'Exploring city life through mixed media.', 'Claire Vance', 'Urban Life', 1),
(2, 'Digital Futures', '2026-07-10', '2026-08-20', 'The intersection of technology and fine art.', 'Tom Richards', 'Technology', 2);

-- 4. Insert Community Members (Independent Entity)
-- role: 'user' or 'admin' — modify directly in Supabase as needed
INSERT INTO Community_Member (CommunityMember_ID, name, email, birthYear, phone, city, membershipType, password, role) VALUES
(1, 'Alex Johnson', 'alex.j@email.com', 1995, '1112223333', 'Paris', 'Premium', 'password123', 'admin'),
(2, 'Maria Garcia', 'maria.g@email.com', 1988, '4445556666', 'New York', 'Standard', 'password123', 'user'),
(3, 'Liam O''Connor', 'liam.o@email.com', 2000, '7778889999', 'London', 'Student', 'password123', 'user');

-- 5. Insert Workshops (Depends on Artist)
INSERT INTO Workshop (Workshop_ID, title, workshopDate, duration, maxParticipant, Price, location, description, level, Artist_ID) VALUES
(1, 'Intro to Urban Sketching', '2026-05-15 14:00:00', 120, 15, 45.00, 'Lumiere Gallery Studio', 'Learn to capture cityscapes.', 'Beginner', 1),
(2, 'Advanced Photoshop Art', '2026-07-20 10:00:00', 240, 10, 80.00, 'Modern Hub Lab', 'Deep dive into digital compositing.', 'Advanced', 2);

-- 6. Insert Disciplines & Tags (Independent Entities)
INSERT INTO Discipline (Discipline_ID, name) VALUES
(1, 'Painting'), (2, 'Photography'), (3, 'Digital Art'), (4, 'Sculpture');

INSERT INTO Artwork_Tag (ArtworkTag_ID, name) VALUES
(1, 'Abstract'), (2, 'Cityscape'), (3, 'Eco-Art'), (4, 'Cyberpunk');

-- 7. Insert Artworks (Depends on Artist, Exhibition)
INSERT INTO Artwork (Artwork_ID, title, creationYear, type, medium, dimension, description, price, statut, Exhibition_ID, Artist_ID) VALUES
(1, 'Neon Nights', 2025, 'Original', 'Acrylic on Canvas', '100x150cm', 'A vibrant depiction of a city street at night.', 1200.00, 'Available', 1, 1),
(2, 'Data Stream', 2026, 'Print', 'Digital rendering', '1920x1080px', 'Visualized data flows.', 300.00, 'Sold', 2, 2),
(3, 'Iron Forest', 2024, 'Original', 'Recycled Metal', '200x100x100cm', 'A tree made of car parts.', 3500.00, 'On Loan', 1, 3);

-- 8. Insert Rating/Reviews (Depends on Artwork, Community_Member)
INSERT INTO Rate (Artwork_ID, CommunityMember_ID, Rating, Comment, Review_Date) VALUES
(1, 1, 5, 'Absolutely breathtaking use of colors!', '2026-05-02'),
(2, 2, 4, 'Great concept, very thought-provoking.', '2026-07-11');

-- 9. Insert Practices (Depends on Artist, Discipline)
INSERT INTO Practice (Artist_ID, Discipline_ID) VALUES
(1, 1),
(2, 2),
(2, 3),
(3, 4);

-- 10. Insert Workshop Bookings (Depends on Community_Member, Workshop)
INSERT INTO Books (CommunityMember_ID, Workshop_ID, Booking_date, Payment_Status) VALUES
(1, 1, '2026-04-01 10:00:00', 'Paid'),
(1, 2, '2026-04-02 11:30:00', 'Paid'),
(2, 2, '2026-04-05 09:15:00', 'Pending');

-- 11. Insert Artwork Tags (Depends on Artwork, Artwork_Tag)
INSERT INTO Tag (Artwork_ID, ArtworkTag_ID) VALUES
(1, 1),
(1, 2),
(2, 4),
(3, 3);

-- 12. Insert Favored Disciplines (Depends on Community_Member, Discipline)
-- Table renamed from 'favored' to 'FavoriteDiscipline'
INSERT INTO FavoriteDiscipline (CommunityMember_ID, Discipline_ID) VALUES
(1, 1),
(1, 3),
(2, 3),
(3, 4);

-- Synchronize SERIAL sequences (PostgreSQL)
SELECT setval('artist_artist_id_seq', (SELECT MAX(Artist_ID) FROM Artist));
SELECT setval('gallery_gallery_id_seq', (SELECT MAX(Gallery_ID) FROM Gallery));
SELECT setval('exhibition_exhibition_id_seq', (SELECT MAX(Exhibition_ID) FROM Exhibition));
SELECT setval('community_member_communitymember_id_seq', (SELECT MAX(CommunityMember_ID) FROM Community_Member));
SELECT setval('workshop_workshop_id_seq', (SELECT MAX(Workshop_ID) FROM Workshop));
SELECT setval('discipline_discipline_id_seq', (SELECT MAX(Discipline_ID) FROM Discipline));
SELECT setval('artwork_tag_artworktag_id_seq', (SELECT MAX(ArtworkTag_ID) FROM Artwork_Tag));
SELECT setval('artwork_artwork_id_seq', (SELECT MAX(Artwork_ID) FROM Artwork));