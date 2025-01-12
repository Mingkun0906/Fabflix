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

// Map this servlet to the "/api/genres" URL
@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DbService.getConnection()) {
            String query = "SELECT name FROM genres ORDER BY name ASC";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                JsonObject genre = new JsonObject();
                genre.addProperty("name", rs.getString("name"));
                jsonArray.add(genre);
            }

            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
