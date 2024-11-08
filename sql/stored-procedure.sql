CREATE TABLE IF NOT EXISTS employees (
     email VARCHAR(50) PRIMARY KEY,
     password VARCHAR(20) NOT NULL,
     fullname VARCHAR(100)
);

INSERT INTO employees (email, password, fullname)
VALUES ('classta@email.edu', 'classta', 'TA CS122B');


DELIMITER //

CREATE PROCEDURE add_movie(
    IN p_movie_title VARCHAR(100),
    IN p_movie_year INTEGER,
    IN p_movie_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE v_movie_id VARCHAR(10);
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INTEGER;
    DECLARE v_max_movie_id VARCHAR(10);
    DECLARE v_max_star_id VARCHAR(10);

    START TRANSACTION;

    SELECT id INTO v_movie_id
    FROM movies
    WHERE title = p_movie_title;

    IF v_movie_id IS NULL THEN
        SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO v_max_movie_id
        FROM movies
        WHERE id LIKE 'tt%';

        SET v_movie_id = CONCAT('tt', LPAD(COALESCE(v_max_movie_id + 1, 1), 7, '0'));

        INSERT INTO movies (id, title, year, director)
        VALUES (v_movie_id, p_movie_title, p_movie_year, p_movie_director);

        SELECT id INTO v_star_id
        FROM stars
        WHERE name = p_star_name;

        IF v_star_id IS NULL THEN
            SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO v_max_star_id
            FROM stars
            WHERE id LIKE 'nm%';

            SET v_star_id = CONCAT('nm', LPAD(COALESCE(v_max_star_id + 1, 1), 7, '0'));

            INSERT INTO stars (id, name)
            VALUES (v_star_id, p_star_name);
        END IF;

        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (v_star_id, v_movie_id);

        SELECT id INTO v_genre_id
        FROM genres
        WHERE name = p_genre_name;

        IF v_genre_id IS NULL THEN
            INSERT INTO genres (name)
            VALUES (p_genre_name);

            SET v_genre_id = LAST_INSERT_ID();
        END IF;

        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (v_genre_id, v_movie_id);

        COMMIT;
        SELECT 'Success: Movie added successfully' as message;
    ELSE
        ROLLBACK;
        SELECT 'Error: Movie already exists' as message;
    END IF;

END //

DELIMITER ;