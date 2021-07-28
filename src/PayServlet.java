import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import static java.time.LocalDate.now;

@WebServlet(name = "PayServlet", urlPatterns = "/api/pay")
public class PayServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("getting total price");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        JsonArray cart = currentUser.getItems();

        int total = 0;
        for(JsonElement item : cart){
            System.out.println("Item: " + item.getAsJsonObject().get("name").getAsString());
            total += item.getAsJsonObject().get("quantity").getAsInt() * 10;
        }

        System.out.println("total " + total );
        response.getWriter().write(String.valueOf(total));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String number = request.getParameter("creditCard");
        String expiration = request.getParameter("expiration");
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        try {
//            Date date = simpleDateFormat.parse(expiration);
//            expiration = simpleDateFormat.format(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        System.out.println("expiration after" + expiration);
        String query;
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {

            query = "SELECT cc.firstName, cc.lastName, expiration, ccId, c.id FROM creditcards cc, customers c where cc.id = ccId and  ccId = '" + number + "' ";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonObject responseJsonObject = new JsonObject();
            if(!rs.isBeforeFirst()){
                responseJsonObject.addProperty("status", "failed");
                responseJsonObject.addProperty("message", "Invalid Credit Card Number. Please Try Again.");
            }

            while(rs.next()) {
                String dbFirstName = rs.getString("firstName");
                String dbLastName = rs.getString("lastName");
                String dbExpiration = rs.getString("expiration");
                String customerID = rs.getString("id");

                if(firstName.equals(dbFirstName) && lastName.equals(dbLastName) && expiration.equals(dbExpiration)){
                HttpSession session = request.getSession();
                User currentUser = (User) session.getAttribute("user");
                    try{
                    JsonArray items = currentUser.getItems();
                    String insertSalesQuery = "INSERT INTO sales(customerId, movieID, saleDate)" +
                            "VALUES (?, ?, Date(now())) ";
                    PreparedStatement insertSales = conn.prepareStatement(insertSalesQuery);

                        for (int i= 0; i < items.size(); i++){
                            for(int j= 0; j < items.get(i).getAsJsonObject().get("quantity").getAsInt(); j++){
                                insertSales.setString(1, customerID);
                                insertSales.setString(2, items.get(i).getAsJsonObject().get("id").getAsString());
                                insertSales.executeUpdate(query);
                            }
                        }
                    }
                    catch(Exception e){
                        // write error message to output
                        System.out.println("errpr: " + e.getMessage());
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("errorMessage", e.getMessage());
                        out.write(jsonObject.toString());
                        // set response status to 500 (Internal Server Error)
                        response.setStatus(500);
                    }

                    synchronized (currentUser){
                        currentUser.addOrder(simpleDateFormat.format(new Date()));
                        currentUser.clearCart();
                    }
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
                else{
                    System.out.println("not the same");
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("message", "Input is wrong. Try again.");
                }
            }

            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            System.out.println("error outside: " + e.getMessage());
            // write error message to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}