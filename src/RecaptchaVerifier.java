import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class RecaptchaVerifier {
    public static boolean verify(String responseToken, String secretKey) {
        try {
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postParams = "secret=" + secretKey + "&response=" + responseToken;
            OutputStream os = conn.getOutputStream();
            os.write(postParams.getBytes());
            os.flush();
            os.close();

            InputStream is = conn.getInputStream();
            Scanner scanner = new Scanner(is, "UTF-8");
            String jsonResponse = scanner.useDelimiter("\\A").next();
            scanner.close();

            return jsonResponse.contains("\"success\": true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
