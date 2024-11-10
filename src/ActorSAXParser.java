import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

public class ActorSAXParser {

    public static class Actor {
        private String familyName;
        private String givenName;
        private String dob;

        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }

        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getDob() { return dob; }
        public void setDob(String dob) { this.dob = dob; }

        @Override
        public String toString() {
            return "Actor{" +
                    "familyName='" + familyName + '\'' +
                    ", givenName='" + givenName + '\'' +
                    ", dob='" + dob + '\'' +
                    '}';
        }
    }

    public static class ActorSAXHandler extends DefaultHandler {
        private List<Actor> actors = new ArrayList<>();
        private Actor currentActor;
        private StringBuilder tempValue = new StringBuilder();

        public List<Actor> getActors() {
            return actors;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            tempValue.setLength(0);
            if ("actor".equalsIgnoreCase(qName)) {
                currentActor = new Actor();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            tempValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (currentActor != null) {
                switch (qName.toLowerCase()) {
                    case "familyname":
                        currentActor.setFamilyName(tempValue.toString().trim());
                        break;
                    case "firstname":
                        currentActor.setGivenName(tempValue.toString().trim());
                        break;
                    case "dob":
                        currentActor.setDob(tempValue.toString().trim());
                        break;
                    case "actor":
                        if (currentActor.getFamilyName() != null && !currentActor.getFamilyName().isEmpty() &&
                                currentActor.getGivenName() != null && !currentActor.getGivenName().isEmpty()) {
                            actors.add(currentActor);
                        }
                        currentActor = null;
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            ActorSAXHandler handler = new ActorSAXHandler();
            saxParser.parse("/Users/mingkunliu/Downloads/2024-fall-cs-122b-team-beef/data/actors63.xml", handler);

            List<Actor> actors = handler.getActors();
            insertActorsIntoDatabase(actors);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertActorsIntoDatabase(List<Actor> actors) {
        String url = "jdbc:mysql://localhost:3306/moviedb";
        String user = "mytestuser";
        String password = "My6$Password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            // Step 1: Retrieve last ID
            String getLastIdQuery = "SELECT id FROM stars ORDER BY id DESC LIMIT 1";
            int lastId = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(getLastIdQuery)) {
                if (rs.next()) {
                    String lastIdString = rs.getString("id").substring(2); // Remove "nm" prefix
                    lastId = Integer.parseInt(lastIdString);
                }
            }

            // Step 2: Retrieve all existing star names into a HashSet
            String getAllNamesQuery = "SELECT name FROM stars";
            HashSet<String> existingNames = new HashSet<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(getAllNamesQuery)) {
                while (rs.next()) {
                    existingNames.add(rs.getString("name"));
                }
            }

            // Step 3: Prepare the batch insert
            String insertSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (Actor actor : actors) {
                    String fullName = actor.getFamilyName() + " " + actor.getGivenName();

                    // Check if the star with the same name already exists in the HashSet
                    if (existingNames.contains(fullName)) {
                        continue;
                    }

                    String newId = "nm" + String.format("%07d", ++lastId);
                    pstmt.setString(1, newId);
                    pstmt.setString(2, fullName);

                    if (isNumeric(actor.getDob())) {
                        pstmt.setInt(3, Integer.parseInt(actor.getDob()));
                    } else {
                        pstmt.setNull(3, Types.INTEGER);
                    }

                    existingNames.add(fullName);

                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static boolean isNumeric(String str) {
        if (str == null) return false;
        return str.matches("\\d+");
    }
}
