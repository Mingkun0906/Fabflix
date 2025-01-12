import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DbService {
    private static DataSource dataSource;

    static {
        try {
            System.out.println("Initializing DbService...");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            System.out.println("DbService initialized successfully.");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database connections", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
