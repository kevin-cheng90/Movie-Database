import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.stream.Collectors;


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            query = "SELECT title, year, director, rating " +
                    "FROM movies m  LEFT JOIN ratings r on m.id = r.movieId "+
                    "WHERE m.id = '" + id + "' " ;


            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Perform the query
            ResultSet general = statement.executeQuery();
            general.next();

            // Display title, year, director, all genres, all stars, rating
            // Get results

            String movieTitle = general.getString("title");
            String movieYear = general.getString("year");
            String directorName = general.getString("director");
            String rating = general.getString("rating");

            // Create a JsonObject based on the data we retrieve from rs

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movie_title", movieTitle);
            jsonObject.addProperty("movie_year", movieYear);
            jsonObject.addProperty("movie_director", directorName);
            jsonObject.addProperty("movie_rating", rating);


            query = "SELECT name " +
                    "FROM genres_in_movies gim LEFT JOIN genres g ON gim.genreId = g.id "+
                    "where movieId = '" + id + "' order by name asc " ;

            statement = conn.prepareStatement(query);

            ResultSet genreResult = statement.executeQuery();

            ArrayList<String> genres= new ArrayList<String>();
            while(genreResult.next()){
                genres.add(genreResult.getString("name"));
            }

            jsonObject.addProperty("genres", genres.toString());

            query = "select name, sim2.starId, count(*) " +
                    "from stars_in_movies as sim2 inner join (Select name, starId from stars_in_movies sim left join stars s on sim.starId = s.id " +
                    "where movieId = '" + id + "')" + " as sr on sr.starId = sim2.starId group by sim2.starId order by count(*) desc, name asc ";

            statement = conn.prepareStatement(query);

            ResultSet starResult = statement.executeQuery();

            JsonArray starsArray = new JsonArray();
            while(starResult.next()){
                JsonObject stars = new JsonObject();
                stars.addProperty("name", starResult.getString("name"));
                stars.addProperty("starId", starResult.getString("starId"));
                starsArray.add(stars);
            }

            jsonObject.addProperty("stars", String.valueOf(starsArray));

            genreResult.close();
            starResult.close();
            general.close();
            statement.close();

            // write JSON string to output
            out.write(jsonObject.toString());
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
