import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard/*")
public class DashboardServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/metadata")) {
            handleMetadataRequest(response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (pathInfo) {
            case "/add-movie":
                handleAddMovie(request, response);
                break;
            case "/add-star":
                handleAddStar(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleAddMovie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseJsonObject = new JsonObject();

        try {
            String jsonString = request.getReader().readLine();
            JsonObject jsonRequest = JsonParser.parseString(jsonString).getAsJsonObject();

            String movieTitle = jsonRequest.has("title") ? jsonRequest.get("title").getAsString() : null;
            String yearStr = jsonRequest.has("year") ? jsonRequest.get("year").getAsString() : null;
            String movieDirector = jsonRequest.has("director") ? jsonRequest.get("director").getAsString() : null;
            String starName = jsonRequest.has("star_name") ? jsonRequest.get("star_name").getAsString() : null;
            String genreName = jsonRequest.has("genre_name") ? jsonRequest.get("genre_name").getAsString() : null;

            if (movieTitle == null || movieTitle.trim().isEmpty() ||
                    movieDirector == null || movieDirector.trim().isEmpty() ||
                    starName == null || starName.trim().isEmpty() ||
                    genreName == null || genreName.trim().isEmpty()) {
                throw new IllegalArgumentException("All fields are required");
            }

            int movieYear = Integer.parseInt(yearStr);

            try (Connection conn = dataSource.getConnection()) {
                CallableStatement statement = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?)}");

                statement.setString(1, movieTitle);
                statement.setInt(2, movieYear);
                statement.setString(3, movieDirector);
                statement.setString(4, starName);
                statement.setString(5, genreName);

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    String message = rs.getString("message");
                    String movieId = rs.getString("movie_id");
                    String starId = rs.getString("star_id");
                    String genreId = rs.getString("genre_id");
                    responseJsonObject.addProperty("message",
                            message + " movie id:" + movieId + " star id:" + starId + " genre id:" + genreId);
                    responseJsonObject.addProperty("status", "success");
                }

                rs.close();
                statement.close();
            }
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Error adding movie: " + e.getMessage());
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
    private void handleAddStar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseJsonObject = new JsonObject();

        try {
            String jsonString = request.getReader().readLine();
            JsonObject jsonRequest = JsonParser.parseString(jsonString).getAsJsonObject();
            String starName = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            String birthYear = jsonRequest.has("birthYear") && !jsonRequest.get("birthYear").isJsonNull()
                    ? jsonRequest.get("birthYear").getAsString() : null;
            if (starName == null || starName.trim().isEmpty()) {
                throw new IllegalArgumentException("Star name is requiredddddd");
            }

            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) as max_id FROM stars WHERE id LIKE 'nm%'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                int maxId = 0;
                if (rs.next()) {
                    maxId = rs.getInt("max_id");
                }
                String newId = String.format("nm%07d", maxId + 1);

                String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setString(1, newId);
                pstmt.setString(2, starName);
                pstmt.setString(3, birthYear.isEmpty() ? null : birthYear);

                pstmt.executeUpdate();

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Star added successfully, starId:" + newId);
            }
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Error adding star: " + e.getMessage());
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }

    private void handleMetadataRequest(HttpServletResponse response) throws IOException {
        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("tableName", tableName);

                JsonArray columnsArray = new JsonArray();
                ResultSet columns = metaData.getColumns(null, null, tableName, null);

                while (columns.next()) {
                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("name", columns.getString("COLUMN_NAME"));
                    columnObject.addProperty("type", columns.getString("TYPE_NAME"));
                    columnsArray.add(columnObject);
                }

                tableObject.add("columns", columnsArray);
                jsonArray.add(tableObject);
            }

            response.setContentType("application/json");
            response.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("status", "error");
            errorObject.addProperty("message", "Error fetching metadata: " + e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write(errorObject.toString());
            e.printStackTrace();
        }
    }
}