import java.sql.*;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class Movie {
    String id;
    String title;
    Integer year;
    String director;
    Set<String> genres;

    public Movie() {
        genres = new HashSet<>();
    }

    public boolean isValid() {
        return id != null && !id.isEmpty() &&
                title != null && !title.isEmpty() &&
                year != null && year > 0 &&
                director != null && !director.isEmpty();
    }
}

public class MovieParser extends DefaultHandler {
    private List<Movie> movies;
    private StringBuilder tempVal;
    private Movie currentMovie;
    private String currentDirector;
    private boolean inFilm;
    private boolean inCats;
    private int processedCount;
    private int invalidCount;
    private Map<String, Integer> genreMap; // Maps genre names to IDs

    private static final Map<String, String> GENRE_MAPPING = new HashMap<>();
    static {
        GENRE_MAPPING.put("Susp", "Thriller");
        GENRE_MAPPING.put("CnR", "Crime");
        GENRE_MAPPING.put("CnRb", "Crime");
        GENRE_MAPPING.put("Dram", "Drama");
        GENRE_MAPPING.put("West", "Western");
        GENRE_MAPPING.put("Myst", "Mystery");
        GENRE_MAPPING.put("S.F.", "Sci-Fi");
        GENRE_MAPPING.put("ScFi", "Sci-Fi");
        GENRE_MAPPING.put("Actn", "Action");
        GENRE_MAPPING.put("Advt", "Adventure");
        GENRE_MAPPING.put("Horr", "Horror");
        GENRE_MAPPING.put("Romt", "Romance");
        GENRE_MAPPING.put("Comd", "Comedy");
        GENRE_MAPPING.put("Musc", "Musical");
        GENRE_MAPPING.put("Muscl", "Musical");
        GENRE_MAPPING.put("Docu", "Documentary");
        GENRE_MAPPING.put("Porn", "Adult");
        GENRE_MAPPING.put("Noir", "Black");
        GENRE_MAPPING.put("BioP", "Biography");
        GENRE_MAPPING.put("TV", "TV Show");
        GENRE_MAPPING.put("TVs", "TV Series");
        GENRE_MAPPING.put("TVm", "TV Miniseries");
        GENRE_MAPPING.put("Actn", "Action");
        GENRE_MAPPING.put("Cart", "Cartoon");
        GENRE_MAPPING.put("Hist", "History");
        GENRE_MAPPING.put("Fant", "Fantasy");
        GENRE_MAPPING.put("Faml", "Family");
        GENRE_MAPPING.put("Romt Comd", "Comedy");
        GENRE_MAPPING.put("Epic", "Epic");
    }

    public MovieParser() {
        movies = new ArrayList<>();
        tempVal = new StringBuilder();
        processedCount = 0;
        invalidCount = 0;
        genreMap = new HashMap<>();
    }

    public void initializeGenres(Connection conn) throws SQLException {
        String selectSQL = "SELECT id, name FROM genres";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                genreMap.put(rs.getString("name"), rs.getInt("id"));
            }
        }

        // Get the next available ID
        int nextId = 1;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) + 1 FROM genres")) {
            if (rs.next()) {
                nextId = Math.max(rs.getInt(1), 1); // Ensure we start at least at 1
            }
        }

        String insertSQL = "INSERT INTO genres (id, name) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            conn.setAutoCommit(false);

            for (String xmlCode : GENRE_MAPPING.keySet()) {
                String genreName = GENRE_MAPPING.get(xmlCode);
                if (!genreMap.containsKey(genreName)) {
                    pstmt.setInt(1, nextId);
                    pstmt.setString(2, genreName);
                    pstmt.executeUpdate();

                    genreMap.put(genreName, nextId);
                    System.out.println("Added new genre: " + genreName + " with ID: " + nextId);
                    nextId++;
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            //conn.setAutoCommit(true);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal.setLength(0);

        switch(qName.toLowerCase()) {
            case "film":
                currentMovie = new Movie();
                inFilm = true;
                processedCount++;
                break;
            case "cats":
                inCats = true;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String value = tempVal.toString().trim();

        switch(qName.toLowerCase()) {
            case "dirname":
                if (value == null ||
                        value.isEmpty() ||
                        value.toLowerCase().startsWith("unknown") ||
                        value.toLowerCase().startsWith("unyear")) {
                    currentDirector = "";
                } else {
                    currentDirector = value;
                }
                break;

            case "fid":
                if (inFilm && currentMovie != null) {
                    currentMovie.id = value;
                }
                break;

            case "t":
                if (inFilm && currentMovie != null) {
                    currentMovie.title = "NKT".equals(value) ? "Unknown Title" : value;
                }
                break;

            case "year":
                if (inFilm && currentMovie != null) {
                    try {
                        currentMovie.year = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid year format for movie ID " + currentMovie.id + ": " + value);
                        currentMovie.year = 0;
                    }
                }
                break;

            case "cat":
                if (inCats && currentMovie != null && !value.isEmpty()) {
                    String mappedGenre = GENRE_MAPPING.get(value);
                    if (mappedGenre != null) {
                        currentMovie.genres.add(mappedGenre);
                    } else {
                        System.out.println("Warning: Unrecognized genre code in XML: " + value);
                    }
                }
                break;

            case "cats":
                inCats = false;
                break;

            case "film":
                if (currentMovie != null) {
                    currentMovie.director = currentDirector;
                    if (currentMovie.isValid()) {
                        movies.add(currentMovie);
                    } else {
                        invalidCount++;
                        System.err.println("Invalid movie data: " +
                                "ID=" + currentMovie.id +
                                ", Title=" + currentMovie.title +
                                ", Year=" + currentMovie.year +
                                ", Director=" + currentMovie.director);
                    }
                }
                inFilm = false;
                break;
        }
    }

    public void insertMoviesAndGenres(Connection conn, List<Movie> movies) throws SQLException {
        initializeGenres(conn);

        insertMovies(conn, movies);

        String insertGenreSQL = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

        int batchSize = 1000;
        int recordCount = 0;
        int totalProcessed = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(insertGenreSQL)) {
            conn.setAutoCommit(false);

            for (Movie movie : movies) {
                for (String genreName : movie.genres) {
                    Integer genreId = genreMap.get(genreName);
                    if (genreId != null) {
                        pstmt.setInt(1, genreId);
                        pstmt.setString(2, movie.id);
                        pstmt.addBatch();
                        recordCount++;

                        if (recordCount % batchSize == 0) {
                            try {
                                pstmt.executeBatch();
                                conn.commit();
                                totalProcessed += recordCount;
                                System.out.println("Processed " + totalProcessed + " genre relationships");
                                recordCount = 0;
                            } catch (BatchUpdateException e) {
                                conn.rollback();
                                System.err.println("Batch insert failed, continuing with next batch");
                                recordCount = 0;
                            }
                        }
                    }
                }
            }

            // Process remaining records
            if (recordCount > 0) {
                try {
                    pstmt.executeBatch();
                    conn.commit();
                    totalProcessed += recordCount;
                } catch (BatchUpdateException e) {
                    conn.rollback();
                    System.err.println("Final batch insert failed");
                }
            }

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            //conn.setAutoCommit(true);
        }

        System.out.println("\nGenre relationships import completed:");
        System.out.println("Total relationships processed: " + totalProcessed);
    }

    public void insertMovies(Connection conn, List<Movie> movies) throws SQLException {
        String insertSQL = "INSERT IGNORE INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";

        int batchSize = 1000;
        int recordCount = 0;
        int totalProcessed = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            conn.setAutoCommit(false);

            for (Movie movie : movies) {
                pstmt.setString(1, movie.id);
                pstmt.setString(2, movie.title);
                pstmt.setInt(3, movie.year);
                pstmt.setString(4, movie.director);
                pstmt.addBatch();
                recordCount++;

                if (recordCount % batchSize == 0) {
                    try {
                        pstmt.executeBatch();
                        conn.commit();
                        totalProcessed += recordCount;
                        System.out.println("Processed " + totalProcessed + " movies");
                        recordCount = 0;
                    } catch (BatchUpdateException e) {
                        conn.rollback();
                        System.err.println("Batch insert failed, continuing with next batch");
                        recordCount = 0;
                    }
                }
            }

            // Process remaining records
            if (recordCount > 0) {
                try {
                    pstmt.executeBatch();
                    conn.commit();
                    totalProcessed += recordCount;
                } catch (BatchUpdateException e) {
                    conn.rollback();
                    System.err.println("Final batch insert failed");
                }
            }

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            //conn.setAutoCommit(true);
        }

        System.out.println("\nMovie import completed:");
        System.out.println("Total movies processed: " + totalProcessed);
    }

    public void parseDocument(String filename) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(filename, this);
        } catch (Exception e) {
            System.err.println("Error parsing file: " + filename);
            e.printStackTrace();
        }
    }

    public List<Movie> getMovies() {
        List<Movie> moviesCopy = new ArrayList<>(movies);
        movies.clear();
        return moviesCopy;
    }
}