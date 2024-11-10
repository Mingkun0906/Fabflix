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
    private int processedCount;
    private int invalidCount;

    public MovieParser() {
        movies = new ArrayList<>();
        tempVal = new StringBuilder();
        processedCount = 0;
        invalidCount = 0;
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

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal.setLength(0);

        switch(qName.toLowerCase()) {
            case "film":
                currentMovie = new Movie();
                inFilm = true;
                processedCount++;
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

    public void insertMovies(Connection conn) throws SQLException {
        String insertSQL = "INSERT IGNORE INTO movies_cpy (id, title, year, director) VALUES (?, ?, ?, ?)";

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
                        System.out.println("Processed " + totalProcessed + " records");
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
            conn.setAutoCommit(true);
        }

        System.out.println("\nImport completed:");
        System.out.println("Total records processed: " + totalProcessed);
    }

    private void printStatistics(int successCount, int errorCount) {
        System.out.println("\nParsing and Insertion Statistics:");
        System.out.println("--------------------------------");
        System.out.println("Total records processed: " + processedCount);
        System.out.println("Valid records found: " + movies.size());
        System.out.println("Invalid records found: " + invalidCount);
        System.out.println("Successfully inserted: " + successCount);
        System.out.println("Failed to insert: " + errorCount);
    }

    public List<Movie> getMovies() {
        return movies;
    }
}