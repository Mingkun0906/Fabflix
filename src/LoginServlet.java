import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * A servlet that takes input from a HTML <form> and talks to MySQL moviedbexample,
 * generates output as a HTML <table>
 */

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.sendRedirect("login.html?error=empty_fields");
            return;
        }

        try (Connection dbCon = DbService.getConnection()) {
            String query = "SELECT id FROM customers WHERE email = ? AND password = ?";
            PreparedStatement statement = dbCon.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();
            boolean success = rs.next();

            if (success) {
                HttpSession session = request.getSession();
                session.setAttribute("user", email);
                int userId = rs.getInt("id");
                session.setAttribute("user_id", userId);
                rs.close();
                statement.close();
                response.sendRedirect("main.html");
            } else {
                rs.close();
                statement.close();
                response.sendRedirect("login.html?error=invalid_credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.html?error=server_error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("login.html");
    }
}
