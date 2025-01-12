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

@WebServlet("/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DbService.getConnection()) {
            String query = request.getParameter("query");
            if (query == null || query.trim().length() < 3) {
                out.write("[]");
                return;
            }

            // Process query terms for MySQL BOOLEAN MODE search
            String[] terms = query.trim().split("\\s+");
            StringBuilder searchQuery = new StringBuilder();
            for (String term : terms) {
                term = term.replaceAll("[+*]", "").trim();
                if (!term.isEmpty()) {
                    searchQuery.append("+" + term + "* ");
                }
            }

            String sql = "SELECT DISTINCT id, title FROM movies " +
                    "WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) " +
                    "UNION " +
                    "SELECT DISTINCT id, title FROM movies " +
                    "WHERE LOWER(title) LIKE LOWER(?) " +
                    "ORDER BY title LIMIT 10";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchQuery.toString().trim());
            stmt.setString(2, query.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", rs.getString("id"));
                jsonObject.addProperty("value", rs.getString("title"));
                jsonArray.add(jsonObject);
            }

            out.write(jsonArray.toString());
            rs.close();
            stmt.close();

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
        out.close();
    }
}