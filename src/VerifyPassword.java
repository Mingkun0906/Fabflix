import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class VerifyPassword {

    /*
     * After you update the passwords in customers table,
     *   you can use this program as an example to verify the password.
     *
     * Verify the password is simple:
     * success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
     *
     * Note that you need to use the same StrongPasswordEncryptor when encrypting the passwords
     *
     */
    public static void main(String[] args) throws Exception {

        System.out.println(verifyCredentials("a@email.com", "a2"));
        System.out.println(verifyCredentials("a@email.com", "a3"));

    }

    static boolean verifyCredentials(String email, String password) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        String query = String.format("SELECT * from customers where email='%s'", email);

        ResultSet rs = statement.executeQuery(query);

        boolean success = false;
        if (rs.next()) {
            String encryptedPassword = rs.getString("password");

            success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
        }
        rs.close();
        statement.close();
        connection.close();
        System.out.println("verify " + email + " - " + password);

        return success;
    }

}