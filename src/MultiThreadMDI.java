import java.sql.Connection;
import java.sql.DriverManager;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

class MovieBatch {
    List<Movie> movies;
    boolean isLast;

    public MovieBatch(List<Movie> movies, boolean isLast) {
        this.movies = movies;
        this.isLast = isLast;
    }
}

public class MultiThreadMDI {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";

    private static final int BATCH_SIZE = 1000;
    private static final int QUEUE_CAPACITY = 10;
    private static final int NUM_DB_THREADS = 4;

    private final BlockingQueue<MovieBatch> movieQueue;
    private final ExecutorService dbExecutor;
    private final AtomicInteger activeWorkers;
    private volatile boolean parsingComplete = false;

    public MultiThreadMDI() {
        this.movieQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.dbExecutor = Executors.newFixedThreadPool(NUM_DB_THREADS);
        this.activeWorkers = new AtomicInteger(0);
    }

    private Connection getConnection() throws Exception {
        Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        conn.setAutoCommit(false);
        return conn;
    }

    private class XMLParserTask implements Runnable {
        private final String xmlFilePath;

        public XMLParserTask(String xmlFilePath) {
            this.xmlFilePath = xmlFilePath;
        }

        @Override
        public void run() {
            try {
                MovieParser parser = new MovieParser();
                parser.parseDocument(xmlFilePath);

                List<Movie> batch = new ArrayList<>();
                for (Movie movie : parser.getMovies()) {
                    batch.add(movie);

                    if (batch.size() >= BATCH_SIZE) {
                        movieQueue.put(new MovieBatch(new ArrayList<>(batch), false));
                        batch.clear();
                    }
                }

                // Handle remaining movies
                if (!batch.isEmpty()) {
                    movieQueue.put(new MovieBatch(batch, true));
                } else {
                    movieQueue.put(new MovieBatch(new ArrayList<>(), true));
                }

                parsingComplete = true;
            } catch (Exception e) {
                System.err.println("Error in parser thread:");
                e.printStackTrace();
            }
        }
    }

    private class DatabaseWorker implements Runnable {
        @Override
        public void run() {
            activeWorkers.incrementAndGet();
            Connection conn = null;
            try {
                conn = getConnection();
                conn.setAutoCommit(false);
                if (conn.getAutoCommit()) {
                    throw new SQLException("Failed to disable auto-commit mode");
                }

                while (!parsingComplete || !movieQueue.isEmpty()) {
                    MovieBatch batch = movieQueue.poll(1, TimeUnit.SECONDS);
                    if (batch != null) {
                        try {
                            insertMovieBatch(conn, batch.movies);
                            conn.commit();
                        } catch (Exception e) {
                            System.err.println("Error processing batch:");
                            e.printStackTrace();
                            try {
                                conn.rollback();
                            } catch (SQLException re) {
                                System.err.println("Error during rollback:");
                                re.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in database worker:");
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.err.println("Error closing connection:");
                        e.printStackTrace();
                    }
                }
                activeWorkers.decrementAndGet();
            }
        }


    private void insertMovieBatch(Connection conn, List<Movie> movies) throws Exception {
            MovieParser parser = new MovieParser();
            parser.insertMoviesAndGenres(conn, movies);
        }
    }

    public void importMovies(String xmlFilePath) {
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists() || !xmlFile.canRead()) {
            System.err.println("XML file not found or not readable: " + xmlFilePath);
            return;
        }

        try {
            System.out.println("Starting multi-threaded import process...");
            System.out.println("Initializing genres...");
            try (Connection conn = getConnection()) {
                MovieParser initParser = new MovieParser();
                initParser.initializeGenres(conn);
            }
            // Start parser thread
            Thread parserThread = new Thread(new XMLParserTask(xmlFilePath));
            parserThread.start();

            // Start database workers
            for (int i = 0; i < NUM_DB_THREADS; i++) {
                dbExecutor.submit(new DatabaseWorker());
            }

            // Wait for parser to complete
            parserThread.join();

            // Wait for all workers to complete
            while (activeWorkers.get() > 0) {
                Thread.sleep(100);
            }

            // Shutdown executor
            dbExecutor.shutdown();
            dbExecutor.awaitTermination(5, TimeUnit.MINUTES);

            System.out.println("Import process completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during import process:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String xmlFilePath = args.length > 0 ? args[0] : "data/mains243.xml";
        System.out.println("Processing XML file: " + xmlFilePath);
        MultiThreadMDI importer = new MultiThreadMDI();
        importer.importMovies(xmlFilePath);
    }
}
