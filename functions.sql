-- ==========================================================
-- 2. TRIGGERS (FOR AUTOMATION) [cite: 22, 91]
-- ==========================================================

-- A. Date Consistency Check
CREATE OR REPLACE FUNCTION fn_check_exhibition_dates() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.endDate < NEW.startDate THEN
        RAISE EXCEPTION 'End date cannot be before start date';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_exhibition_dates 
BEFORE INSERT OR UPDATE ON Exhibition 
FOR EACH ROW EXECUTE FUNCTION fn_check_exhibition_dates();

-- B. Workshop Capacity Guard [cite: 91]
CREATE OR REPLACE FUNCTION fn_check_workshop_capacity() RETURNS TRIGGER AS $$
DECLARE
    current_count INT;
    max_cap INT;
BEGIN
    SELECT COUNT(*) INTO current_count FROM Books WHERE Workshop_ID = NEW.Workshop_ID;
    SELECT maxParticipant INTO max_cap FROM Workshop WHERE Workshop_ID = NEW.Workshop_ID;
    IF current_count >= max_cap THEN
        RAISE EXCEPTION 'Workshop is full';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_workshop_capacity 
BEFORE INSERT ON Books 
FOR EACH ROW EXECUTE FUNCTION fn_check_workshop_capacity();

-- C. Protect Active Artists
CREATE OR REPLACE FUNCTION fn_prevent_active_artist_deletion() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.isActive = TRUE THEN
        RAISE EXCEPTION 'Cannot delete active artist';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_protect_artists 
BEFORE DELETE ON Artist 
FOR EACH ROW EXECUTE FUNCTION fn_prevent_active_artist_deletion();


-- ==========================================================
-- 3. STORED PROCEDURES & FUNCTIONS [cite: 22, 92]
-- ==========================================================

-- A. Procedure to Register Member [cite: 94]
CREATE OR REPLACE PROCEDURE RegisterMember(p_member_id INT, p_workshop_id INT) AS $$
BEGIN
    INSERT INTO Books (CommunityMember_ID, Workshop_ID, Payment_Status)
    VALUES (p_member_id, p_workshop_id, 'Pending');
END;
$$ LANGUAGE plpgsql;

-- B. Function to Count Participants [cite: 92]
CREATE OR REPLACE FUNCTION GetParticipantCount(workshop_id_param INT) RETURNS INT AS $$
BEGIN
    RETURN (SELECT COUNT(*) FROM Books WHERE Workshop_ID = workshop_id_param);
END;
$$ LANGUAGE plpgsql;

-- C. Procedure to Create Workshop [cite: 92]
CREATE OR REPLACE PROCEDURE CreateWorkshop(p_title VARCHAR, p_artist_id INT, p_max INT) AS $$
BEGIN
    INSERT INTO Workshop (title, Artist_ID, maxParticipant, Price)
    VALUES (p_title, p_artist_id, p_max, 0.00);
END;
$$ LANGUAGE plpgsql;