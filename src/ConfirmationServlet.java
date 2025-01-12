import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Date;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {

            Map<String, JsonObject> cart = (Map<String, JsonObject>) session.getAttribute("cart");
            Integer userId = (Integer) session.getAttribute("user_id");
            String userEmail = (String) session.getAttribute("user");

            if (cart == null || userId == null || userEmail == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing cart or user information");
                return;
            }

            Connection conn = DbService.getConnection();

            try {
                conn.setAutoCommit(false);
                java.sql.Date saleDate = new java.sql.Date(new Date().getTime());

                JsonObject responseObj = new JsonObject();
                responseObj.addProperty("orderDate", saleDate.toString());
                responseObj.addProperty("customerEmail", userEmail);
                JsonArray items = new JsonArray();

                String insertFirstSql = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement firstPs = conn.prepareStatement(insertFirstSql, Statement.RETURN_GENERATED_KEYS);

                JsonObject firstItem = cart.values().iterator().next();
                String firstMovieId = firstItem.get("movie_id").getAsString();
                int firstQuantity = firstItem.get("quantity").getAsInt();

                firstPs.setInt(1, userId);
                firstPs.setString(2, firstMovieId);
                firstPs.setDate(3, saleDate);
                firstPs.setInt(4, firstQuantity);
                firstPs.executeUpdate();

                Integer saleId = null;
                ResultSet rs = firstPs.getGeneratedKeys();
                if (rs.next()) {
                    saleId = rs.getInt(1);
                    responseObj.addProperty("orderId", saleId.toString());
                }
                items.add(firstItem);

                String insertRestSql = "INSERT INTO sales (id, customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement restPs = conn.prepareStatement(insertRestSql);

                boolean isFirst = true;
                for (JsonObject item : cart.values()) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }

                    String movieId = item.get("movie_id").getAsString();
                    int quantity = item.get("quantity").getAsInt();

                    restPs.setInt(1, saleId);
                    restPs.setInt(2, userId);
                    restPs.setString(3, movieId);
                    restPs.setDate(4, saleDate);
                    restPs.setInt(5, quantity);
                    restPs.executeUpdate();

                    items.add(item);
                }

                responseObj.add("items", items);

                conn.commit();

                session.removeAttribute("cart");

                out.write(responseObj.toString());

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }

        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", e.getMessage());
            out.write(errorResponse.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}