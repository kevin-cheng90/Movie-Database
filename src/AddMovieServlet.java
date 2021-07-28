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
@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();
        String name = request.getParameter("starName");
        String genre = request.getParameter("genreName");
        String movieName = request.getParameter("movieName");
        String movieYear = request.getParameter("movieYear");
        String movieDirector = request.getParameter("movieDirector");
//add_movie(IN movieTitle VARCHAR(100), IN movieYear INTEGER, IN movieDirector VARCHAR(100), IN starName VARCHAR(100), IN genreName VARCHAR(32))
        try (Connection conn = dataSource.getConnection()) {
            String query = "CALL add_movie('"+ movieName  +"', " + movieYear +", '"+ movieDirector +"', '" + name + "', '" + genre + "');";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            if (!rs.isBeforeFirst() ) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", "Error Adding Movie.");
                out.write(jsonObject.toString());
                response.setStatus(200);
            }
            while(rs.next()){
                System.out.println("Message: " + rs.getString(1));
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Successfully added movie.");
            out.write(jsonObject.toString());
            response.setStatus(200);

        }catch(Exception e){
            System.out.println("ERROR: " + e);
            // write error message to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Unable to add movie.");
            out.write(jsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

    }
}
