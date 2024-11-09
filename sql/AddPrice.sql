ALTER TABLE movies ADD COLUMN price INT;
UPDATE movies
SET price = FLOOR(RAND() * (20 - 5 + 1)) + 5
WHERE price IS NULL;

DELIMITER //

CREATE TRIGGER set_price_before_insert
    BEFORE INSERT ON movies
    FOR EACH ROW
BEGIN
    -- Set the price to a random integer between 5 and 20 if it is not provided
    IF NEW.price IS NULL THEN
        SET NEW.price = FLOOR(RAND() * (20 - 5 + 1)) + 5;
    END IF;
END//

DELIMITER ;