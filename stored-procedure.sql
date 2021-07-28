DELIMITER $$
CREATE PROCEDURE add_movie(IN movieTitle VARCHAR(100), IN movieYear INTEGER, IN movieDirector VARCHAR(100), IN starName VARCHAR(100), IN genreName VARCHAR(32))
BEGIN
	IF NOT EXISTS (SELECT * FROM movies where movieTitle = title AND movieYear = year AND movieDirector = director) THEN
		SET @newMovieID = "null";
		SET  @newStarID = "null";
		SET  @newGenreID = "null";
        
		SELECT "Added";
        SET @newMovieID = LPAD(FLOOR(RAND() * 999999), 9, '0');
        SELECT concat("NEW MOVIE ID: ", @newMovieID);
        INSERT INTO movies (id, title, year, director) VALUES (@newMovieID, movieTitle, movieYear, movieDirector);
		
        IF NOT EXISTS (SELECT * FROM stars where starName = name) THEN
			SET @newStarID = LPAD(FLOOR(RAND() * 999999), 9, '0');
			INSERT INTO stars (id, name) VALUES (@newStarID, starName);
			SELECT concat_ws("Inserted star: ", starName, "with id: ", @newStarID);
            SELECT concat("NEW STAR ID: ", @newStarID);
		ELSE
			SET @newStarID = (SELECT id FROM stars where starName = name);
			SELECT "Star is already in database.";
		END IF;
    
		IF NOT EXISTS (SELECT * FROM genres where genreName = name) THEN
			INSERT INTO genres (name) VALUES (genreName);
			SET @newGenreID = last_insert_id();
			SELECT concat_ws("Inserted genre: ", genreName, "with id: ", @newGenreID);
            SELECT concat("NEW genre ID: ", @newGenreID);
		ELSE
			SET @newGenreID = (SELECT id FROM genres where genreName = name);
			SELECT "Genre is already in database.";
			SELECT concat("OLD GENRE ID: ", @newGenreID);
		END IF;
        
        INSERT INTO stars_in_movies (starId, movieId) VALUES (@newStarID, @newMovieID);
        SELECT concat_ws("Linked starID: ", @newStarID, " with movieID: ", @newMovieID );
        
        INSERT INTO genres_in_movies (genreId, movieId)  VALUES (@newGenreID, @newMovieID);
        SELECT concat_ws("Linked genreID: ", @newGenreID, " with movieID: ", @newMovieID );
        
		INSERT INTO ratings (movieID, rating, numVotes)  VALUES (@newMovieID, 0, 0);
	ELSE
		SELECT "Duplicate";
	END IF;

END $$

DELIMITER ;