import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;



// Declaring a WebServlet called MovieServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns="/api/movies")
public class MoviesServlet extends HttpServlet {
	private static final long serialVersionUID = 3L;

	// Create a dataSource which registered in web.xml
	private DataSource dataSource;

	public void init(ServletConfig config) {
		try {
			dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json"); 

		PrintWriter out = response.getWriter();
		try {
			// get connection from data source
			Connection dbcon = dataSource.getConnection();
			String N; 
			String orderFirst = "DESC"; 
			String orderSecond="ASC"; 
			String sort1 = "r.rating"; 
			String sort2 = "m.title";
			Integer offset;
			ArrayList<String> Options = new ArrayList<String>();
			Options.add("10"); Options.add("20"); Options.add("25"); Options.add("50"); Options.add("100");
			// Setting default N="25" or getting parameter for N
			if (request.getParameter("N") == null || !Options.contains(request.getParameter("N"))) {
				N = "25";
			}
			else { 
				N = request.getParameter("N"); 
			}
			// Setting default page=1 or getting param for page
			if (request.getParameter("page") == null) { 
				offset = 0; 
			}
			else { 
				offset = Integer.parseInt(N) * (Integer.parseInt(request.getParameter("page"))-1); 
			}
			// Setting default orderFirst = rating / getting param for orderFirst
			if (request.getParameter("orderFirst") == null || request.getParameter("orderFirst").equals("descending")) { 
				orderFirst = "DESC";
			}
			else if (request.getParameter("orderFirst").equals("ascending")) { 
				orderFirst = "ASC";
			}
			if (request.getParameter("orderSecond") == null || request.getParameter("orderSecond").equals("ascending")) { 
				orderSecond = "ASC"; 
			}
			else if (request.getParameter("orderSecond").equals("descending")) { 
				orderSecond = "DESC";
			}


			// Setting default for sort = rating or
			if (request.getParameter("sort") == null || request.getParameter("sort").equals("rating")) { 
				sort1="r.rating"; sort2="m.title"; 
			}
			else if (request.getParameter("sort").equals("title")) { 
				sort1="m.title"; sort2="r.rating"; 
			}


			// Gets the top 20 rated movies
			String ordering = "ORDER BY " + sort1 + " " + orderFirst + ", " + sort2 +  " " + orderSecond + " LIMIT "
								+ offset.toString() + ", " + N;
			String query = "SELECT * FROM ratings r, movies m WHERE m.Id = r.movieId " + ordering;
			// Handles Search
			/*
			PreparedStatement stmt = dbcon.prepareStatement("SELECT * FROM ratings r, movies m WHERE m.Id = r.movieId ORDER BY ? ?, ? ? LIMIT ?, ?");
			stmt.setString(1, sort1);
			stmt.setString(2, orderFirst);
			stmt.setString(3, sort2);
			stmt.setString(4, orderSecond);
			stmt.setInt(5, offset);
			stmt.setInt(6, Integer.parseInt(N));
			 */

			if (request.getParameter("search") != null) {
				if (request.getParameter("category").equals("title")) {
					// TODO: Check if fulltext or not and change query
					String searchString = request.getParameter("search").replaceAll(" ", "* ") + "*";
					String preQuery = "SELECT * from movies WHERE MATCH (title) AGAINST ('" + searchString + "' in boolean mode) ORDER BY title ASC ";
					query = "SELECT * FROM ratings r, (" + preQuery + ") as m where r.movieId = m.id " + ordering;

				}
				else if (request.getParameter("category").equals("year")) { 
					query = "SELECT * FROM ratings r, movies m WHERE m.year = " + request.getParameter("search").replace("+", " ") +
							" AND m.Id = r.movieId " + ordering;
				}
				else if (request.getParameter("category").equals("director")){ 
					query = "SELECT * FROM ratings r, movies m WHERE m.director LIKE \"%" + request.getParameter("search").replace("+", " ") + "%\" "
						+ "AND m.Id = r.movieId " + ordering;
				}
				else if (request.getParameter("category").equals("star")) { 
					query = "SELECT m.Id, m.title, m.director, m.year, r.rating, r.movieId " +
						"FROM movies m, ratings r, stars st, stars_in_movies sm " +
						"WHERE sm.starId = st.id AND st.name LIKE \"%" + request.getParameter("search").replace("+", " ")
						+ "%\" AND m.Id = sm.movieId AND m.Id = r.movieId LIMIT 10";
				}
			}
			// Handles Browse
			if (request.getParameter("genreBrowse") != null) {
				query = "SELECT m.Id, m.title, m.director, m.year, r.rating, r.movieId FROM ratings r, movies m, genres ge, genres_in_movies gi WHERE "
						+ "m.Id = r.movieId AND ge.name =  \"" + request.getParameter("genreBrowse") + "\" AND gi.movieId = m.Id AND ge.Id = gi.genreId " + ordering;
			}

			// Gets genres of search movies or top 20
//			"select name, sim2.starId, count(*) " +
//                    "from stars_in_movies as sim2 inner join (Select name, starId from stars_in_movies sim left join stars s on sim.starId = s.id " +
//                    "where movieId = '" + id + "')" + " as sr on sr.starId = sim2.starId group by sim2.starId order by count(*) desc, name asc ";
			String genreQuery = "SELECT mov.id, g.name FROM genres AS g, (" + query + ") AS rm, "
								+ "movies AS mov, genres_in_movies AS gim "
								+ "WHERE rm.movieId = mov.id AND gim.genreId = g.id AND mov.id = gim.movieId ";
			// Gets the stars search movies or top 20
			String starQuery = "SELECT s.id, s.name, sr.movieId FROM stars AS s "
								+ "INNER JOIN (SELECT sim.movieId, sim.starId, rm.rating FROM stars_in_movies AS sim "
								+ "INNER JOIN (" + query + ") AS rm "
								+ "ON sim.movieId = rm.movieId) AS sr ON sr.starId = s.id";

			PreparedStatement statement = dbcon.prepareStatement(query);
			PreparedStatement statement2 = dbcon.prepareStatement(genreQuery);
			PreparedStatement statement3 = dbcon.prepareStatement(starQuery);


			ResultSet rs = statement.executeQuery();
			// genre Query
			ResultSet rs2 = statement2.executeQuery();
			// star Query
			ResultSet rs3 = statement3.executeQuery();

			JsonArray jsonArray = new JsonArray();
			// Creating a hashmap of movie id with three genres
			HashMap<String, ArrayList<String>> genres_table = new HashMap<String, ArrayList<String>>();
			String currId = new String();
			while (rs2.next())
			{
				if (currId.equals(rs2.getString("id")))
				{
					if (genres_table.get(currId) == null)
					{
						currId = rs2.getString("id");
						genres_table.put(currId, new ArrayList<String>());
						genres_table.get(currId).add(rs2.getString("name"));
					}
					else if (genres_table.get(currId).size() < 3)
					{
						genres_table.get(currId).add(rs2.getString("name"));
					}
				}
				else if (!currId.equals(rs2.getString("id")))
				{
					currId = rs2.getString("id");
					genres_table.put(currId, new ArrayList<String>());
					genres_table.get(currId).add(rs2.getString("name"));
				}
			}
			// Creating a hashmap of movie id with three stars
			// Movie id : [star, star, star]
			// Star id:
			HashMap<String, ArrayList<String>> stars_table = new HashMap<String, ArrayList<String>>();
			HashMap<String, ArrayList<String>> stars_id_table = new HashMap<String, ArrayList<String>>();
			while (rs3.next())
			{
				if (currId.equals(rs3.getString("movieId")))
				{
					if (stars_table.get(currId) == null)
					{
						stars_table.put(currId, new ArrayList<String>());
						stars_table.get(currId).add(rs3.getString("name"));
						stars_id_table.put(currId, new ArrayList<String>());
						stars_id_table.get(currId).add(rs3.getString("id"));
					}
					else if (stars_table.get(currId).size() < 3)
					{
						stars_table.get(currId).add(rs3.getString("name"));
						stars_id_table.get(currId).add(rs3.getString("id"));
					}
				}
				else if (!currId.equals(rs3.getString("movieId")))
				{
					currId = rs3.getString("movieId");
					stars_table.put(currId, new ArrayList<String>());
					stars_table.get(currId).add(rs3.getString("name"));
					stars_id_table.put(currId, new ArrayList<String>());
					stars_id_table.get(currId).add(rs3.getString("id"));
				}
			}
			while (rs.next()) {
				String movieId = rs.getString("id");
				String movieTitle = rs.getString("title");
				String movieYear = rs.getString("year");
				String movieDirector = rs.getString("director");
				String rating = rs.getString("rating");
				JsonArray genres = new JsonArray();
				JsonArray stars = new JsonArray();
				JsonArray stars_id = new JsonArray();

				// Adding 3 genres to genre list
				if (genres_table.get(rs.getString("id")) != null)
				{
					for (int i = 0; i < genres_table.get(rs.getString("id")).size(); i++)
					{
						genres.add(genres_table.get(rs.getString("id")).get(i));
					}
				}
				// Adding 3 stars to star list

				if (stars_table.get(rs.getString("id")) != null)
				{
					for (int i = 0; i < stars_table.get(rs.getString("id")).size(); i++)
					{
						stars.add(stars_table.get(rs.getString("id")).get(i));
						stars_id.add(stars_id_table.get(rs.getString("id")).get(i));
					}
				}
				// Create a JsonObject based on the data we retrieve from rs
				// title, year, director, 3 genres, 3 stars, rating
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("movie_id", movieId);
				jsonObject.addProperty("movie_title", movieTitle);
				jsonObject.addProperty("movie_year", movieYear);
				jsonObject.addProperty("movie_director", movieDirector);
				jsonObject.addProperty("rating", rating);
				jsonObject.add("genres", genres);
				jsonObject.add("stars", stars);
				jsonObject.add("stars_id", stars_id);
				jsonArray.add(jsonObject);
			}
			JsonArray resultJsonArray = new JsonArray();
			resultJsonArray.add(jsonArray);
			JsonArray params = new JsonArray();
			JsonObject parameters = new JsonObject();
			if (request.getParameter("page") == null)
			{ parameters.addProperty("page", 1); }
			else
			{ parameters.addProperty("page", request.getParameter("page")); }
			if (request.getParameter("sort") == null || request.getParameter("sort").equals("rating"))
			{ parameters.addProperty("sort", "rating"); }
			else
			{ parameters.addProperty("sort", "title"); }
			if (request.getParameter("orderFirst") == null || request.getParameter("orderFirst").equals("descending"))
			{ parameters.addProperty("orderFirst", "descending"); }
			else {parameters.addProperty("orderFirst", request.getParameter("orderFirst"));}
			if (request.getParameter("orderSecond") == null || request.getParameter("orderSecond").equals("ascending"))
			{ parameters.addProperty("orderSecond", "ascending"); }
			else {parameters.addProperty("orderSecond", request.getParameter("orderSecond"));}
			if (request.getParameter("search") != null)
			{
				if (request.getParameter("category") != null)
				{
					parameters.addProperty("category", request.getParameter("category"));
					parameters.addProperty("search", request.getParameter("search"));
				}
			}
			else
			{
				parameters.addProperty("category", "title");
				parameters.addProperty("search", "");
			}
			Statement getGenres = dbcon.createStatement();
			ResultSet rsGenre = getGenres.executeQuery("SELECT * FROM genres");
			// Getting genres for browse
			JsonArray sendGenres = new JsonArray();
			while (rsGenre.next())
			{
				sendGenres.add(rsGenre.getString("name"));
			}
			parameters.add("browseGenres", sendGenres);
			params.add(parameters);
			resultJsonArray.add(params);
			out.write(resultJsonArray.toString());
			response.setStatus(200);
			rs.close(); rs2.close(); rs3.close(); rsGenre.close();
			statement.close(); statement2.close(); statement3.close(); getGenres.close();
			dbcon.close();
		} catch (Exception e) {
			// error message
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			// (Internal server error)
			response.setStatus(500);
		}
		out.close();
	}
}
