import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class DbService {
    private static DataSource masterDataSource;

    static {
        try {
            masterDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_master");
        } catch (NamingException e) {
            throw new RuntimeException("Failed to initialize database connections", e);
        }
    }

    public static Connection getMasterConnection() throws SQLException {
        return masterDataSource.getConnection();
    }

    public static Connection getRandomConnection() throws SQLException {
        return masterDataSource.getConnection();
    }
}