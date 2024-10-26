import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        Map<String, JsonObject> cart = (Map<String, JsonObject>) session.getAttribute("cart");

        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        String movie_id = request.getParameter("movie_id");
        String movie_title = request.getParameter("movie_title");
        int price = Integer.parseInt(request.getParameter("price"));
        int quantityChange = Integer.parseInt(request.getParameter("quantity"));

        // Adjust the cart item
        if (cart.containsKey(movie_id)) {
            JsonObject movie = cart.get(movie_id);
            int currentQuantity = movie.get("quantity").getAsInt();
            int newQuantity = currentQuantity + quantityChange;

            if (newQuantity > 0) {
                movie.addProperty("quantity", newQuantity);
            } else {
                cart.remove(movie_id); // Remove item if quantity drops to zero or below
            }
        } else if (quantityChange > 0) {
            // Add a new item to the cart
            JsonObject movie = new JsonObject();
            movie.addProperty("movie_id", movie_id);
            movie.addProperty("movie_title", movie_title);
            movie.addProperty("price", price);
            movie.addProperty("quantity", quantityChange);
            cart.put(movie_id, movie);
        }

        JsonArray cartArray = new JsonArray();
        for (JsonObject item : cart.values()) {
            cartArray.add(item);
        }

        out.write(cartArray.toString());
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Retrieve session
        HttpSession session = request.getSession(false);
        Map<String, JsonObject> cart = (Map<String, JsonObject>) session.getAttribute("cart");

        // Send response
        JsonArray cartArray = new JsonArray();
        if (cart != null) {
            for (JsonObject item : cart.values()) {
                cartArray.add(item);
            }
        }
        out.write(cartArray.toString());
        out.close();
    }
}

