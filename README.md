# Project 1

This is a movie web application that allows users to view a list of movies and click on individual movies to see more 
detailed information, including the top stars and genres for each movie.

## Project Structure

- **Backend**: Java Servlets
- **Frontend**: HTML with Bootstrap and JavaScript
- **Database**: MySQL with connection pooling via a DataSource


## Contributions

### Feiyang Jin:
- Set up the environment and handled the majority of the backend implementation.
- Developed the **MoviesServlet** to query and return a list of top 20 movies along with stars and genres.
- Developed and enhanced custom CSS styles to make the website visually appealing, improving layout and responsiveness.

### Mingkun Liu:
- Assisted with the backend development and helped optimize the database queries.
- Implemented the **SingleMovieServlet** to fetch details of a specific movie based on its ID.
- Worked on the frontend by designing the **single-movie.js** and **single-movie.html** script to populate the movie details page.


## Running the Application

1. Deploy the application on a Tomcat server.
2. Access the movie list at `/api/movies`.
3. Click on a movie title to view detailed information on the single movie page.
