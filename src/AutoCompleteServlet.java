import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    String query;
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        System.out.println("IN SERVLEY" );
        String input = request.getParameter("query");
        String searchString = "+" + input.replaceAll(" ","* +") + "*";

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            // Construct a query with parameter represented by "?"
            query = "SELECT id, title from movies WHERE MATCH (title) AGAINST ('" + searchString + "' in boolean mode) ORDER BY title ASC LIMIT 10 " ;

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            ResultSet autoResults = statement.executeQuery();

            JsonArray suggestions = new JsonArray();
            while(autoResults.next()){
                JsonObject suggestionItem = new JsonObject();
                suggestionItem.addProperty("query", input);
                suggestionItem.addProperty("title", autoResults.getString("title"));
                suggestionItem.addProperty("id", autoResults.getString("id"));
                suggestions.add(suggestionItem);
            }


            // write JSON array to output
            out.write(suggestions.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
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
