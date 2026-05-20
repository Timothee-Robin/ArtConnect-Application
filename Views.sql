DROP VIEW IF EXISTS GetArts;
DROP VIEW IF EXISTS GetArtsCommunityRates;

CREATE VIEW GetArts as
SELECT Artwork_ID, title, creationYear, type, medium, dimension, price, statut,
      name as Name_Artist, bio as Bio_Artits, contact_email, phone, city, website, socialmedia, birthyear
FROM Artwork
INNER JOIN Artist USING (Artist_ID);



CREATE VIEW GetArtsCommunityRates as
SELECT Rating, Comment, Review_Date, 
         title, creationYear, type, medium, dimension, price, statut,
         name, email, birthyear, phone, city, membershiptype
FROM Rate
INNER JOIN Community_Member USING (CommunityMember_ID)
INNER JOIN Artwork USING (Artwork_ID);








SELECT * FROM GetArts;
SELECT * FROM GetArtsCommunityRates;