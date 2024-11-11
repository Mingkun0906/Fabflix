//import org.xml.sax.Attributes;
//import org.xml.sax.helpers.DefaultHandler;
//
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//import java.sql.*;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CastSAXParser {
//    private static Map<String, String> movieMap = new HashMap<>();
//    private static Map<String, String> starMap = new HashMap<>();
//    private static Map<String, String> stageNameToRealNameMap = new HashMap<>();
//
//    public static class ActorHandler extends DefaultHandler {
//        private StringBuilder currentValue = new StringBuilder();
//        private String stageName;
//        private String firstName;
//        private String lastName;
//
//        @Override
//        public void startElement(String uri, String localName, String qName, Attributes attributes) {
//            currentValue.setLength(0);
//        }
//
//        @Override
//        public void characters(char[] ch, int start, int length) {
//            currentValue.append(ch, start, length);
//        }
//
//        @Override
//        public void endElement(String uri, String localName, String qName) {
//            String value = currentValue.toString().trim();
//
//            switch (qName.toLowerCase()) {
//                case "stagename":
//                    stageName = value.toLowerCase();
//                    break;
//                case "firstname":
//                    firstName = value;
//                    break;
//                case "familyname":
//                    lastName = value;
//                    break;
//                case "actor":
//                    if (stageName != null && firstName != null && lastName != null) {
//                        String realName = (lastName + " " + firstName).trim();
//                        stageNameToRealNameMap.put(stageName, realName);
//                        stageName = null;
//                        firstName = null;
//                        lastName = null;
//                    }
//                    break;
//            }
//        }
//    }
//
//    public static class CastHandler extends DefaultHandler {
//        private StringBuilder currentValue = new StringBuilder();
//        private String movieId;
//        private String stageName;
//
//        @Override
//        public void startElement(String uri, String localName, String qName, Attributes attributes) {
//            currentValue.setLength(0);
//        }
//
//        @Override
//        public void characters(char[] ch, int start, int length) {
//            currentValue.append(ch, start, length);
//        }
//
//        @Override
//        public void endElement(String uri, String localName, String qName) {
//            String value = currentValue.toString().trim();
//
//            switch (qName.toLowerCase()) {
//                case "f":
//                    movieId = value;
//                    break;
//                case "a":
//                    stageName = value.toLowerCase();
//                    break;
//                case "m":
//                    if (movieId != null && stageName != null) {
//                        processStarInMovie(movieId, stageName);
//                        movieId = null;
//                        stageName = null;
//                    }
//                    break;
//            }
//        }
//    }
//
//    private static void processStarInMovie(String movieId, String stageName) {
//        String realName = stageNameToRealNameMap.get(stageName);
//        if (realName == null) {
//            return;
//        }
//
//        String movieDbId = movieMap.get(movieId.toLowerCase());
//        String starDbId = starMap.get(normalizeString(realName));
//
//        if (movieDbId == null || starDbId == null) {
//            return;
//        }
//
//        insertStarInMovie(starDbId, movieDbId);
//    }
//
//    private static void insertStarInMovie(String starId, String movieId) {
//        String url = "jdbc:mysql://localhost:3306/moviedb";
//        String user = "mytestuser";
//        String password = "My6$Password";
//
//        String checkSQL = "SELECT 1 FROM stars_in_movies_backup WHERE starId = ? AND movieId = ?";
//        String insertSQL = "INSERT INTO stars_in_movies_backup (starId, movieId) VALUES (?, ?)";
//
//        try (Connection conn = DriverManager.getConnection(url, user, password)) {
//            conn.setAutoCommit(false);
//
//            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
//                checkStmt.setString(1, starId);
//                checkStmt.setString(2, movieId);
//                ResultSet rs = checkStmt.executeQuery();
//
//                if (!rs.next()) {
//                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
//                        insertStmt.setString(1, starId);
//                        insertStmt.setString(2, movieId);
//                        insertStmt.executeUpdate();
//                    }
//                }
//
//                conn.commit();
//
//            } catch (SQLException e) {
//                conn.rollback();
//                e.printStackTrace();
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void loadExistingData() {
//        String url = "jdbc:mysql://localhost:3306/moviedb";
//        String user = "mytestuser";
//        String password = "My6$Password";
//
//        try (Connection conn = DriverManager.getConnection(url, user, password)) {
//            try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM movies");
//                 ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    String id = rs.getString("id");
//                    movieMap.put(id.toLowerCase(), id);
//                }
//            }
//
//            try (PreparedStatement pstmt = conn.prepareStatement("SELECT id, name FROM stars");
//                 ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    String name = normalizeString(rs.getString("name"));
//                    starMap.put(name, rs.getString("id"));
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String normalizeString(String input) {
//        if (input == null) return "";
//        return input.replaceAll("[^a-zA-Z0-9\\s]", "")
//                .toLowerCase()
//                .trim()
//                .replaceAll("\\s+", " ");
//    }
//
//    public static void main(String[] args) {
//        try {
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            SAXParser parser = factory.newSAXParser();
//            parser.parse("data/actors63.xml", new ActorHandler());
//            loadExistingData();
//            parser.parse("data/casts124.xml", new CastHandler());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CastSAXParser {
    private static Map<String, String> movieMap = new HashMap<>();
    private static Map<String, String> starMap = new HashMap<>();
    private static Map<String, String> stageNameToRealNameMap = new HashMap<>();
    private static final int BATCH_SIZE = 500;
    private static int totalProcessed = 0;
    private static int successfulMatches = 0;
    private static int failedMatches = 0;

    public static class ActorHandler extends DefaultHandler {
        private StringBuilder currentValue = new StringBuilder();
        private String stageName;
        private String firstName;
        private String lastName;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentValue.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String value = currentValue.toString().trim();

            switch (qName.toLowerCase()) {
                case "stagename":
                    stageName = value.toLowerCase();
                    break;
                case "firstname":
                    firstName = value;
                    break;
                case "familyname":
                    lastName = value;
                    break;
                case "actor":
                    if (stageName != null && firstName != null && lastName != null) {
                        String realName = (lastName + " " + firstName).trim();
                        stageNameToRealNameMap.put(stageName, realName);
                        stageName = null;
                        firstName = null;
                        lastName = null;
                    }
                    break;
            }
        }
    }

    public static class CastHandler extends DefaultHandler {
        private StringBuilder currentValue = new StringBuilder();
        private String movieId;
        private String stageName;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentValue.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String value = currentValue.toString().trim();

            switch (qName.toLowerCase()) {
                case "f":
                    movieId = value;
                    break;
                case "a":
                    stageName = value.toLowerCase();
                    break;
                case "m":
                    if (movieId != null && stageName != null) {
                        processStarInMovie(movieId, stageName);
                        totalProcessed++;
                        movieId = null;
                        stageName = null;
                    }
                    break;
            }
        }
    }

    private static void processStarInMovie(String movieId, String stageName) {
        String realName = stageNameToRealNameMap.get(stageName);
        if (realName == null) {
            failedMatches++;
            System.out.println("No real name found for stage name: " + stageName);
            return;
        }

        String movieDbId = movieMap.get(movieId.toLowerCase());
        String starDbId = starMap.get(normalizeString(realName));

        if (movieDbId == null || starDbId == null) {
            failedMatches++;
            if (movieDbId == null) System.out.println("Movie not found: " + movieId);
            if (starDbId == null) System.out.println("Star not found: " + realName + " (stage name: " + stageName + ")");
            return;
        }

        addBatchInsert(starDbId, movieDbId);
    }

    private static Connection connection;
    private static PreparedStatement insertStmt;
    private static int batchCount = 0;

    private static void initializeDatabaseConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/moviedb";
        String user = "mytestuser";
        String password = "My6$Password";

        connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        String insertSQL = "INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        insertStmt = connection.prepareStatement(insertSQL);
    }

    private static void addBatchInsert(String starId, String movieId) {
        try {
            insertStmt.setString(1, starId);
            insertStmt.setString(2, movieId);
            insertStmt.addBatch();
            batchCount++;

            if (batchCount % BATCH_SIZE == 0) {
                executeBatch();
            }
        } catch (SQLException e) {
            failedMatches++;
            e.printStackTrace();
        }
    }

    private static void executeBatch() {
        try {
            insertStmt.executeBatch();
            connection.commit();
            successfulMatches += batchCount;
            batchCount = 0;
        } catch (SQLException e) {
            failedMatches += batchCount;
            System.err.println("Batch execution failed. Rolling back.");
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    private static void closeDatabaseConnection() {
        try {
            if (batchCount > 0) {
                executeBatch(); // Execute remaining batch
            }
            insertStmt.close();
            connection.commit(); // Final commit to ensure all data is saved
            connection.close();
            System.out.println("Database connection closed.");
            System.out.println("Total processed: " + totalProcessed);
            System.out.println("Successful matches: " + successfulMatches);
            System.out.println("Failed matches: " + failedMatches);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadExistingData() {
        String url = "jdbc:mysql://localhost:3306/moviedb";
        String user = "mytestuser";
        String password = "My6$Password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM movies");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    movieMap.put(id.toLowerCase(), id);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT id, name FROM stars");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = normalizeString(rs.getString("name"));
                    starMap.put(name, rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String normalizeString(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9\\s]", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");
    }

    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            // Step 1: Parse actors.xml to build stage name to real name mapping
            parser.parse("data/actors63.xml", new ActorHandler());

            // Step 2: Load existing movie and star data from database
            loadExistingData();

            // Step 3: Initialize database connection for batch inserts
            initializeDatabaseConnection();

            // Step 4: Parse casts.xml and process relationships
            parser.parse("data/casts124.xml", new CastHandler());

            // Close database connection
            closeDatabaseConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

