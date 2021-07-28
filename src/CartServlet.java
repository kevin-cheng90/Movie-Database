import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/shopping-cart")
public class CartServlet extends HttpServlet {

    /**
     * handles GET requests to retrieve items from user shopping cart
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        System.out.println("currentUser" + currentUser.getUsername());
        response.getWriter().write(currentUser.getItems().toString());
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestData = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject newItem = parser.parse(requestData).getAsJsonObject();

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        String item = newItem.get("item").getAsString();

        int inCart = currentUser.findItem(item);

        synchronized (currentUser) {
            currentUser.replaceItem(item, newItem.get("quantity").getAsInt(), inCart);
        }

        System.out.println("Current shopping cart @ end of PUT in CartServlet: " + currentUser.getItems());
        response.getWriter().write(currentUser.getItems().toString());
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestData = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject deletedItem = parser.parse(requestData).getAsJsonObject();
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        synchronized (currentUser){
            currentUser.deleteItem(deletedItem.get("item").getAsString());
        }
        response.getWriter().write(currentUser.getItems().toString());

    }

}