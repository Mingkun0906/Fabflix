import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AdminLoginServlet", urlPatterns = "/admin_login")
public class AdminLoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String recaptchaResponse = request.getParameter("g-recaptcha-response");
        String secretKey = "6LeLunIqAAAAALrN77rqthLy2JmkUPeD1DTxEGGh";

        // Verify reCAPTCHA
        boolean isRecaptchaVerified = RecaptchaVerifier.verify(recaptchaResponse, secretKey);
        if (!isRecaptchaVerified) {
            response.sendRedirect("admin-login.html?error=recaptcha");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT fullname FROM employees WHERE email = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String fullname = rs.getString("fullname");

                // Create a session and set admin role
                HttpSession session = request.getSession();
                session.setAttribute("user", email);
                session.setAttribute("role", "admin");
                session.setAttribute("fullname", fullname);

                // Redirect to dashboard
                response.sendRedirect("dashboard.html");
            } else {
                response.sendRedirect("admin-login.html?error=invalid");
            }

            rs.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("admin-login.html?error=server");
        }
    }

}
