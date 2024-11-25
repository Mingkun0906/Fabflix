import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = DbService.getRandomConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT " +
                    "m.id AS movie_id, " +
                    "m.title AS movie_title, " +
                    "m.year AS movie_year, " +
                    "m.director AS movie_director, " +
                    "r.rating AS movie_rating, " +
                    "m.price AS movie_price, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) AS movie_genres, " +
                    "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, '::', s.name) ORDER BY movie_count DESC, s.name ASC SEPARATOR ', ') " +
                    " FROM stars s " +
                    " JOIN stars_in_movies sim ON s.id = sim.starId " +
                    " JOIN (SELECT starId, COUNT(movieId) AS movie_count " +
                    "       FROM stars_in_movies GROUP BY starId) AS star_movies ON s.id = star_movies.starId " +
                    " WHERE sim.movieId = m.id) AS movie_stars " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id " +
                    "WHERE m.id = ?";
            //query = "select * from movies where id = ?";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", rs.getString("movie_id"));
                jsonObject.addProperty("movie_title", rs.getString("movie_title"));
                jsonObject.addProperty("movie_year", rs.getString("movie_year"));
                jsonObject.addProperty("movie_director", rs.getString("movie_director"));
                jsonObject.addProperty("movie_genres", rs.getString("movie_genres"));
                jsonObject.addProperty("movie_stars", rs.getString("movie_stars"));
                jsonObject.addProperty("movie_rating", rs.getString("movie_rating"));
                jsonObject.addProperty("movie_price", rs.getDouble("movie_price"));

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
