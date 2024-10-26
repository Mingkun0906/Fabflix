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

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //HttpSession session = request.getSession();


        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String creditCard = request.getParameter("creditCard");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            int expirationMonth = Integer.parseInt(request.getParameter("expirationMonth"));
            int expirationYear = Integer.parseInt(request.getParameter("expirationYear"));

            String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? " +
                    "AND YEAR(expiration) = ? AND MONTH(expiration) = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, creditCard);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setInt(4, expirationYear);
            statement.setInt(5, expirationMonth);

            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();

            if (rs.next()) {
                // Credit card is valid
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Payment processed successfully");
                response.setStatus(200);
            } else {
                // Credit card validation failed
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid credit card information");
                response.setStatus(400);
            }

            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}