# Project 1

This is a movie web application that allows users to view a list of movies and click on individual movies to see more 
detailed information, including the top stars and genres for each movie.

## Video url
- https://youtu.be/ahCbn9ZfatM (proj1)
- https://youtu.be/XPMR9KZYtgU (proj2)
- https://youtu.be/sQ0nNFSsyv4 (proj3)

## Contributions

### Feiyang Jin:
project1:
- Set up the environment and handled the majority of the backend implementation.
- Developed the **MoviesServlet** to query and return a list of top 20 movies along with stars and genres.
- Developed and enhanced custom CSS styles to make the website visually appealing, improving layout and responsiveness.

project2:
- Implemented exend movielist, search feature, payment, confirmation parts.

### Mingkun Liu:
project1:
- Assisted with the backend development and helped optimize the database queries.
- Implemented the **SingleMovieServlet** to fetch details of a specific movie based on its ID.
- Worked on the frontend by designing the **single-movie.js** and **single-movie.html** script to populate the movie details page.

Project2:
- Implemented login, browsing, shoppping cart parts.

# Project3 Notice:
- Implemented Prepared Statement: ActorSAXParser.java, AdminLoginServlet.java, CastSAXParser.java, ConfirmationServlet.java, DashboardServlet.java, GenresServlet.java, LoginServlet.java, MovieParser.java, MoviesServlet.java, PaymentServlet.java,  ShoppingCartServlet.java, SingleMovieServlet.java, SingleStarServlet.java, StarsServlet.java, UpdatePasswordEmployee.java, UpdateSecurePassword.java


- Final data count:
stars count: 64895, movies count: 20826

- We decide that data with missing information, typo,  or abnormal information (like it's not in the provided genre list) are considered invalid data and discard them.


Parsing Time Optimization Strategies:
- Batch Processing:
We optimized the data insertion phase by using batch processing during XML parsing. Instead of executing a database insert operation for each individual record, we grouped records into batches of a fixed size and inserted them together.


- In-Memory Caching for Lookup Tables:
We loaded essential reference data into memory such as existing movie and star IDs before processing the XML files. This caching approach avoids repeated database lookups for each parsed record, allowing the parser to quickly check if an entity already exists in the database. By maintaining a map of relevant IDs and names in memory, we reduced database access time and improved the speed of data validation and insertion.


