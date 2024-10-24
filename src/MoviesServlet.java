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
import java.sql.ResultSet;
import java.sql.Statement;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        }
        catch (NamingException e)
        {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false); // false = don't create session if it doesn't exist

        if (session == null || session.getAttribute("user") == null) {
            // No session, redirect to login page
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");
            String genre = request.getParameter("genre");
            String titleStart = request.getParameter("title_start");

            List<String> conditions = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                conditions.add("m.title LIKE '%" + title + "%'");
            }
            if (year != null && !year.isEmpty()) {
                conditions.add("m.year = " + year);
            }
            if (director != null && !director.isEmpty()) {
                conditions.add("m.director LIKE '%" + director + "%'");
            }
            if (star != null && !star.isEmpty()) {
                conditions.add("s.name LIKE '%" + star + "%'");
            }


            String countQuery = "SELECT COUNT(DISTINCT m.id) as total FROM movies m " +
                    "JOIN ratings r ON m.id = r.movieId";

            if (!conditions.isEmpty()) {
                countQuery += " WHERE " + String.join(" AND ", conditions);
            }

            Statement countStatement = conn.createStatement();
            ResultSet countRs = countStatement.executeQuery(countQuery);
            int totalResults = 0;
            if (countRs.next()) {
                totalResults = countRs.getInt("total");
            }
            countRs.close();
            countStatement.close();

            if (genre != null && !genre.isEmpty()) {
                conditions.add("g.name = '" + genre + "'");
            }

            if (titleStart != null && !titleStart.isEmpty()) {
                if (titleStart.equals("*")) {
                    conditions.add("m.title REGEXP '^[^a-zA-Z0-9]'");
                } else {
                    conditions.add("m.title LIKE '" + titleStart + "%'");
                }
            }



            String baseQuery = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(CONCAT(star_info.id, '::', star_info.name, '::', star_info.movie_count) " +
                    "                    ORDER BY star_info.movie_count DESC, star_info.name ASC " +
                    "                    SEPARATOR ', ') " +
                    "FROM (SELECT DISTINCT s.id, s.name, " +
                    "             (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) as movie_count " +
                    "      FROM stars s " +
                    "      JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "      WHERE sim.movieId = m.id " +
                    "      ORDER BY movie_count DESC, s.name ASC " +
                    "      LIMIT 3) star_info) AS stars_info, " +
                    "(SELECT GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ', ') " +
                    "FROM (SELECT DISTINCT gim.genreId " +
                    "      FROM genres_in_movies gim " +
                    "      WHERE gim.movieId = m.id LIMIT 3) top_genres " +
                    "JOIN genres g ON top_genres.genreId = g.id) AS genre_names " +
                    "FROM movies m " +
                    "JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id";

            if (!conditions.isEmpty()) {
                baseQuery += " WHERE " + String.join(" AND ", conditions);
            }

            String sortParam = request.getParameter("sort");
            if (sortParam == null) {
                sortParam = "rating,desc,title,asc"; // default sort
            }
            String[] sortParts = sortParam.split(",");
            String field1 = sortParts[0];
            String order1 = sortParts[1].toUpperCase();
            String field2 = sortParts[2];
            String order2 = sortParts[3].toUpperCase();

            baseQuery += " ORDER BY " +
                    (field1.equals("rating") ? "r.rating" : "m.title") + " " + order1 + ", " +
                    (field2.equals("rating") ? "r.rating" : "m.title") + " " + order2;

            int page = 1;
            int limit = 10;
            try {
                page = Integer.parseInt(request.getParameter("page"));
                limit = Integer.parseInt(request.getParameter("limit"));
            } catch (NumberFormatException e) {}
            int offset = (page - 1) * limit;

            baseQuery += String.format(" LIMIT %d OFFSET %d", limit, offset);


            Statement statement = conn.createStatement();

            // Perform the query
            ResultSet rs = statement.executeQuery(baseQuery);

            JsonArray jsonArray = new JsonArray();
            JsonObject responseObject = new JsonObject();
            responseObject.addProperty("totalResults", totalResults);
            jsonArray.add(responseObject);

            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genre_names");
                String movie_rating = rs.getString("rating");
                String stars_info = rs.getString("stars_info");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("stars_info", stars_info);
                jsonObject.addProperty("movie_genres", movie_genres);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
