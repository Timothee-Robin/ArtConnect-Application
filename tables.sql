CREATE TABLE Artist (
    Artist_ID SERIAL PRIMARY KEY,
    name VARCHAR(100),
    bio TEXT,
    contact_email VARCHAR(50),
    phone VARCHAR(20),
    city VARCHAR(50),
    website VARCHAR(50),
    socialMedia VARCHAR(50),
    isActive BOOLEAN DEFAULT TRUE,
    birthYear INT
);

CREATE TABLE Gallery (
    Gallery_ID SERIAL PRIMARY KEY,
    name VARCHAR(50),
    adress VARCHAR(255),
    ownerName VARCHAR(50),
    openingHours VARCHAR(50),
    contactPhone VARCHAR(50),
    rating DECIMAL(15,2),
    website VARCHAR(50)
);

CREATE TABLE Exhibition (
    Exhibition_ID SERIAL PRIMARY KEY,
    title VARCHAR(100),
    startDate DATE,
    endDate DATE,
    Description TEXT,
    curatorName VARCHAR(50),
    theme VARCHAR(50),
    Gallery_ID INT REFERENCES Gallery(Gallery_ID) ON DELETE CASCADE
);

CREATE TABLE Community_Member (
    CommunityMember_ID SERIAL PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    birthYear INT,
    phone VARCHAR(50),
    city VARCHAR(50),
    membershipType VARCHAR(50)
);

CREATE TABLE Workshop (
    Workshop_ID SERIAL PRIMARY KEY,
    title VARCHAR(100),
    workshopDate TIMESTAMP,
    duration INT,
    maxParticipant INT,
    Price DECIMAL(15,2),
    location VARCHAR(100),
    description TEXT,
    level VARCHAR(50),
    Artist_ID INT REFERENCES Artist(Artist_ID) ON DELETE SET NULL
);

CREATE TABLE Discipline (
    Discipline_ID SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

CREATE TABLE Artwork_Tag (
    ArtworkTag_ID SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

CREATE TABLE Artwork (
    Artwork_ID SERIAL PRIMARY KEY,
    title VARCHAR(100),
    creationYear INT,
    type VARCHAR(50),
    medium VARCHAR(50),
    dimension VARCHAR(50),
    description TEXT,
    price DECIMAL(15,2),
    statut VARCHAR(50),
    Exhibition_ID INT REFERENCES Exhibition(Exhibition_ID) ON DELETE SET NULL,
    Artist_ID INT REFERENCES Artist(Artist_ID) ON DELETE CASCADE
);



CREATE TABLE Rate (
    Artwork_ID INT REFERENCES Artwork(Artwork_ID) ON DELETE CASCADE,
    CommunityMember_ID INT REFERENCES Community_Member(CommunityMember_ID) ON DELETE CASCADE,
    Rating SMALLINT CHECK (Rating >= 1 AND Rating <= 5),
    Comment TEXT,
    Review_Date DATE DEFAULT CURRENT_DATE,
    PRIMARY KEY (Artwork_ID, CommunityMember_ID)
);

CREATE TABLE Practice (
    Artist_ID INT REFERENCES Artist(Artist_ID) ON DELETE CASCADE,
    Discipline_ID INT REFERENCES Discipline(Discipline_ID) ON DELETE CASCADE,
    PRIMARY KEY (Artist_ID, Discipline_ID)
);

CREATE TABLE Books (
    CommunityMember_ID INT REFERENCES Community_Member(CommunityMember_ID) ON DELETE CASCADE,
    Workshop_ID INT REFERENCES Workshop(Workshop_ID) ON DELETE CASCADE,
    Booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Payment_Status VARCHAR(50),
    PRIMARY KEY (CommunityMember_ID, Workshop_ID)
);

CREATE TABLE Tag (
    Artwork_ID INT REFERENCES Artwork(Artwork_ID) ON DELETE CASCADE,
    ArtworkTag_ID INT REFERENCES Artwork_Tag(ArtworkTag_ID) ON DELETE CASCADE,
    PRIMARY KEY (Artwork_ID, ArtworkTag_ID)
);

CREATE TABLE favored (
    CommunityMember_ID INT REFERENCES Community_Member(CommunityMember_ID) ON DELETE CASCADE,
    Discipline_ID INT REFERENCES Discipline(Discipline_ID) ON DELETE CASCADE,
    PRIMARY KEY (CommunityMember_ID, Discipline_ID)
);