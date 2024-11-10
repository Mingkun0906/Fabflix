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
import java.sql.PreparedStatement;
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
            List<Object> parameters = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                conditions.add("m.title LIKE ?");
                parameters.add("%" + title + "%");
            }
            if (year != null && !year.isEmpty()) {
                conditions.add("m.year = ?");
                parameters.add(Integer.parseInt(year));
            }
            if (director != null && !director.isEmpty()) {
                conditions.add("m.director LIKE ?");
                parameters.add("%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                conditions.add("s.name LIKE ?");
                parameters.add("%" + star + "%");
            }

            if (titleStart != null && !titleStart.isEmpty()) {
                if (titleStart.equals("*")) {
                    conditions.add("m.title REGEXP '^[^a-zA-Z0-9]'");
                } else {
                    conditions.add("m.title LIKE ?");
                    parameters.add(titleStart + "%");
                }
            }

            if (genre != null && !genre.isEmpty()) {
                conditions.add("g.name = ?");
                parameters.add(genre);
            }

            String countQuery = "SELECT COUNT(DISTINCT m.id) as total FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id";

            if (!conditions.isEmpty()) {
                countQuery += " WHERE " + String.join(" AND ", conditions);
            }

            PreparedStatement countStatement = conn.prepareStatement(countQuery);
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String) {
                    countStatement.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    countStatement.setInt(i + 1, (Integer) param);
                }
            }

            ResultSet countRs = countStatement.executeQuery();
            int totalResults = 0;
            if (countRs.next()) {
                totalResults = countRs.getInt("total");
            }
            countRs.close();
            countStatement.close();

            String baseQuery = "SELECT DISTINCT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0.0) as rating, m.price, " +
                    "COALESCE(" +
                    "  (SELECT GROUP_CONCAT(CONCAT(star_info.id, '::', star_info.name, '::', star_info.movie_count) " +
                    "   ORDER BY star_info.movie_count DESC, star_info.name ASC " +
                    "   SEPARATOR ', ') " +
                    "   FROM (SELECT DISTINCT s.id, s.name, " +
                    "                (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) as movie_count " +
                    "         FROM stars s " +
                    "         LEFT JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "         WHERE sim.movieId = m.id " +
                    "         ORDER BY movie_count DESC, s.name ASC " +
                    "         LIMIT 3) star_info), " +
                    "  '') AS stars_info, " +
                    "COALESCE(" +
                    "  (SELECT GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ', ') " +
                    "   FROM genres g " +
                    "   JOIN genres_in_movies gim ON g.id = gim.genreId " +
                    "   WHERE gim.movieId = m.id), " +
                    "  '') AS genre_names " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id";

            if (!conditions.isEmpty()) {
                baseQuery += " WHERE " + String.join(" AND ", conditions);
            }

            String sortParam = request.getParameter("sort");
            if (sortParam == null) {
                sortParam = "rating,desc,title,asc";
            }
            String[] sortParts = sortParam.split(",");
            String field1 = sortParts[0];
            String order1 = sortParts[1].toUpperCase();
            String field2 = sortParts[2];
            String order2 = sortParts[3].toUpperCase();

            if (!order1.equals("ASC") && !order1.equals("DESC")) order1 = "DESC";
            if (!order2.equals("ASC") && !order2.equals("DESC")) order2 = "ASC";
            if (!field1.equals("rating") && !field1.equals("title")) field1 = "rating";
            if (!field2.equals("rating") && !field2.equals("title")) field2 = "title";

            baseQuery += " ORDER BY " +
                    (field1.equals("rating") ? "rating" : "m.title") + " " + order1 + ", " +
                    (field2.equals("rating") ? "rating" : "m.title") + " " + order2;

            int page = 1;
            int limit = 10;
            try {
                page = Integer.parseInt(request.getParameter("page"));
                limit = Integer.parseInt(request.getParameter("limit"));
            } catch (NumberFormatException e) {}
            int offset = (page - 1) * limit;

            baseQuery += " LIMIT ? OFFSET ?";

            PreparedStatement statement = conn.prepareStatement(baseQuery);

            int paramIndex = 1;
            for (Object param : parameters) {
                if (param instanceof String) {
                    statement.setString(paramIndex++, (String) param);
                } else if (param instanceof Integer) {
                    statement.setInt(paramIndex++, (Integer) param);
                }
            }

            statement.setInt(paramIndex++, limit);
            statement.setInt(paramIndex, offset);

            ResultSet rs = statement.executeQuery();

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
                String movie_price = rs.getString("price");
                String stars_info = rs.getString("stars_info");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("price", movie_price);
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
    }
}