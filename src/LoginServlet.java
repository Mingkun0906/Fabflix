import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.http.HttpSession;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Get the email and password from the form
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.sendRedirect("login.html?error=empty_fields");
            return;
        }

        try {
            // Establish connection to the database
            Connection dbCon = dataSource.getConnection();

            // Query to verify email and password
            String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
            PreparedStatement statement = dbCon.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                // Successful login, create session
                HttpSession session = request.getSession();
                session.setAttribute("user", email);
                int userId = rs.getInt("id");
                session.setAttribute("user_id", userId);
                // Redirect to the main page after successful login
                response.sendRedirect("main.html");
            } else {
                // Invalid credentials, redirect to login page with error
                response.sendRedirect("login.html?error=invalid_credentials");
            }

            rs.close();
            statement.close();
            dbCon.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.html?error=server_error");
        }
    }

    // Optional: If you want to handle GET requests as well
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Handle GET requests, e.g., redirect to login page if accessed directly
        response.sendRedirect("login.html");
    }
}