import com.google.gson.JsonArray;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.stream.Collectors;


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "AddCartServlet", urlPatterns = "/api/add-cart")
public class AddCartServlet extends HttpServlet {
    String query;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entered POST.");
        PrintWriter out = response.getWriter();
        String requestData = request.getReader().lines().collect(Collectors.joining());

        System.out.println("Got request data: " + requestData);
        JsonParser parser = new JsonParser();
        JsonObject newItem = parser.parse(requestData).getAsJsonObject();


        System.out.println("newItem trinyg to be added: " + newItem.get("name").getAsString());

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        JsonArray cart = currentUser.getItems();
        String item = newItem.get("name").getAsString();

        int inCart = currentUser.findItem(item);
        System.out.println("otuside 1 " + cart.getAsJsonArray());
        if(inCart > -1){
            try{
                System.out.println("qty: " + (cart.get(inCart).getAsJsonObject().get("quantity").getAsInt() + 1));
                int oldQty = cart.get(inCart).getAsJsonObject().get("quantity").getAsInt();
                synchronized (currentUser) {
                    currentUser.replaceItem(item, oldQty + 1, inCart);
                }
                response.setStatus(200);
            } catch (Exception e) {
                // write error message JSON object to output
                System.out.println("error updating: " + e.getMessage());
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());
                // set response status to 500 (Internal Server Error)
                response.setStatus(500);
            }

        }
        else{
            try{

                synchronized (currentUser){
                    currentUser.addItem(newItem.get("name").getAsString(), 1, newItem.get("id").getAsString());
                }

                response.setStatus(200);

            } catch (Exception e) {
                // write error message JSON object to output
                System.out.println("error: " + e.getMessage());
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());
                // set response status to 500 (Internal Server Error)
                response.setStatus(500);
            }
        }

        System.out.println("Current shopping cart @ end of PUT in AddCartServlet: " + currentUser.getItems());
    }
}
