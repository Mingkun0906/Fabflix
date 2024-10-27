ALTER TABLE movies ADD COLUMN price INT;
UPDATE movies
SET price = FLOOR(RAND() * (20 - 5 + 1)) + 5
WHERE price IS NULL;