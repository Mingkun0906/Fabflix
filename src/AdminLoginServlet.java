import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "AdminLoginServlet", urlPatterns = "/admin_login")
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String recaptchaResponse = request.getParameter("g-recaptcha-response");
        String secretKey = "6LeLunIqAAAAALrN77rqthLy2JmkUPeD1DTxEGGh";

        boolean isRecaptchaVerified = RecaptchaVerifier.verify(recaptchaResponse, secretKey);
        if (!isRecaptchaVerified) {
            response.sendRedirect("admin-login.html?error=recaptcha");
            return;
        }

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.sendRedirect("admin-login.html?error=empty_fields");
            return;
        }

        try (Connection connection = DbService.getRandomConnection()) {
            // First, retrieve the stored encrypted password and employee info
            String query = "SELECT password, fullname FROM employees WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String storedEncryptedPassword = rs.getString("password");
                String fullname = rs.getString("fullname");

                // Use the same encryption method as in customer login
                StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

                // Verify the password
                if (encryptor.checkPassword(password, storedEncryptedPassword)) {
                    // Password matches - create session
                    HttpSession session = request.getSession();
                    session.setAttribute("user", email);
                    session.setAttribute("role", "admin");
                    session.setAttribute("fullname", fullname);

                    response.sendRedirect("dashboard.html");
                } else {
                    // Password doesn't match
                    response.sendRedirect("admin-login.html?error=invalid");
                }
            } else {
                // No user found with that email
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