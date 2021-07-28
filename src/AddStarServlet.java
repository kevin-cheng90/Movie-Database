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
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.Collectors;


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private String genStarId(String maxStarId) {
        int newIdInt = Integer.parseInt(maxStarId.substring(2)) + 1;
        String newId = String.valueOf(newIdInt);
        String zeroes = "";
        for (int i = newId.length(); i < 7; i++) {
            zeroes += "0";
        }
        String final_id = "nm" + zeroes + newId;
        return final_id;
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();
        String name = request.getParameter("starName");
        String birthDay = request.getParameter("birth");
        try (Connection conn = dataSource.getConnection()) {
            String query = "select max(id) from stars ";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery();

            String id = "";
            while(rs.next()){
                id = rs.getString("max(id)");
            }

            String newID = genStarId(id);
            if(birthDay != null){
                query = "INSERT INTO stars (id, name, birthYear) VALUES ('"+ newID + "', '" + name + "', "+ birthDay +" ) ";
            }
            else{
                query = "INSERT INTO stars (id, name) VALUES ('"+ newID + "', '" + name + "' ) ";
            }

            statement = conn.prepareStatement(query);
            int changed = statement.executeUpdate();

            if(changed == 1){
                // write error message to output
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", "Successfully added " + name + " with id: " + newID);
                out.write(jsonObject.toString());
                // set response status to 500 (Internal Server Error)
                response.setStatus(200);
            }
            else{
                // write error message to output
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", "Unable to add " + name + " to database");
                out.write(jsonObject.toString());
                // set response status to 500 (Internal Server Error)
                response.setStatus(200);
            }



        }catch(Exception e){
            System.out.println("ERROR: " + e);
            // write error message to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Unable to add star.");
            out.write(jsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

    }
}
