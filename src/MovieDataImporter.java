import java.sql.Connection;
import java.sql.DriverManager;
import java.io.File;

public class MovieDataImporter {
    // Database connection parameters
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";

    private Connection getConnection() throws Exception {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void importMovies(String xmlFilePath) {
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists() || !xmlFile.canRead()) {
            System.err.println("XML file not found or not readable: " + xmlFilePath);
            return;
        }

        MovieParser parser = new MovieParser();

        try {
            // Step 1: Parse XML
            System.out.println("Starting import process...");
            System.out.println("Parsing XML file: " + xmlFilePath);
            parser.parseDocument(xmlFilePath);


            try (Connection conn = getConnection()) {
                System.out.println("Database connected, starting insertion...");
                parser.insertMovies(conn);
                System.out.println("Import process completed successfully!");
            }

        } catch (Exception e) {
            System.err.println("Error during import process:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String xmlFilePath = "data/mains243.xml";
        System.out.println("XML File: " + xmlFilePath);


        MovieDataImporter importer = new MovieDataImporter();
        importer.importMovies(xmlFilePath);
    }
}