import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

// -----------------------------------------------------------------------
// To do:
// Parse movies
// Add inconsistency report
// Write stars to text file
// 


public class DomParser {
	List<Movie> movies = new ArrayList<>();
	List<Actor> actors = new ArrayList<>();
	HashMap<String, HashSet<String>> cast = new HashMap<String, HashSet<String>>();
	HashSet<String> seenActors = new HashSet<String>();
	HashSet<String> seenFid = new HashSet<String>();
	HashSet<String> seenTitles = new HashSet<String>();
	HashMap<String, String> actorMap = new HashMap<String, String>();
	HashMap<String, Integer> genreMap = new HashMap<String, Integer>();
	HashMap<String, String> movieMap = new HashMap<String, String>();

	Document domMain;
	Document domActors;
	Document domCast;
	int inconsistency = 0;
	int movieInconsistency = 0;
	int castInconsistency = 0;
	int maxMovieId = 0;
	int maxStarId = 0;
	int m_id = 0;
	int s_id = 0;
	HashMap<String, String> categoryMap = new HashMap<String, String>();
	FileWriter inconsistencyFile = new FileWriter("inconsistencyData.txt");

	private DataSource dataSource;

	public DomParser() throws IOException {
	}

	public void run() throws Exception{
		String loginUser = "mytestuser";
		String loginPasswd = "My6$Password";
		String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
		Statement statement = connection.createStatement();

		int inc = 0;

		String query = "SELECT * FROM movies";
		ResultSet rs = statement.executeQuery((query));
		while (rs.next()) {
			inc += 1;
			seenTitles.add(rs.getString("title"));
			movieMap.put(rs.getString("title"), rs.getString("id"));
		}
		query = "SELECT * FROM stars";
		rs = statement.executeQuery(query);
		while (rs.next()) {
			seenActors.add(rs.getString("name"));
			actorMap.put(rs.getString("name"), rs.getString("id"));
		}

		query = "SELECT MAX(id) FROM movies";
		rs = statement.executeQuery(query);
		while (rs.next())
		{
			maxMovieId = Integer.parseInt(rs.getString("MAX(id)").substring(2));
		}
		// create a genremap;

		query = "select * from genres";
		rs = statement.executeQuery(query);
		while (rs.next())
		{
			genreMap.put(rs.getString("name"), Integer.parseInt(rs.getString("id")));
		}
		int genreStart = genreMap.size();

		query = "SELECT MAX(id) FROM stars";
		rs = statement.executeQuery(query);
		while (rs.next()) {
			maxStarId = Integer.parseInt(rs.getString("MAX(id)").substring(2));
		}
		// Actors
		parseXMLActors();

		parseActorsDocument();

		domActors = null;
		//printData();

		// Movies/Main
		parseXMLMain();

		parseMainDocument();

		domMain = null;
		//printMovies();

		// Casts
		parseXMLCast();

		parseCastsDocument();

		domCast = null;

		try {
			FileWriter movieFile = new FileWriter("movieData.txt", false);
			FileWriter genreFile = new FileWriter("genreData.txt", false);

			for (String genreName : genreMap.keySet())
			{
				if (genreMap.get(genreName) > genreStart) {
					genreFile.write(String.valueOf(genreMap.get(genreName)) + "\t" + genreName + "\n");
				}
			}
			genreFile.close();

			FileWriter ratingFile = new FileWriter("ratingData.txt", false);
			FileWriter genresInMoviesFile = new FileWriter("genresInMoviesData.txt", false);
			for (Movie movie : movies)
			{
				movieFile.write(movie.getMovieId() + "\t" + movie.getTitle() + "\t" +
								String.valueOf(movie.getYear()) + "\t" + movie.getDirector() +
								"\n");
				ratingFile.write(movie.getMovieId() + "\t" + "NULL" + "\t" + "NULL\n");
				for (String genreName : movie.getCategories()) {
					if (genreMap.get(genreName) != null) {
						genresInMoviesFile.write(String.valueOf(genreMap.get(genreName)) + "\t" + movie.getMovieId() + "\n");
					}
				}
			}
			ratingFile.close();
			genresInMoviesFile.close();
			movieFile.close();

			FileWriter actorFile = new FileWriter("actorData.txt", false);
			for (Actor actor : actors)
			{
				actorFile.write(actor.getStarId() + "\t" + actor.getName() + "\t");
				if  (actor.getDob() == 0) {
					actorFile.write("NULL\n");
				}
				else {
					actorFile.write(String.valueOf(actor.getDob()) + "\n");
				}
			}
			actorFile.close();

			FileWriter starsInMoviesFile = new FileWriter("starsInMoviesData.txt", false);
			for (String movieName: cast.keySet()) {
				for (String castName: cast.get(movieName)) {
					starsInMoviesFile.write(actorMap.get(castName) + "\t" + movieMap.get(movieName) + "\n");
				}
			}
			starsInMoviesFile.close();
		} catch (IOException e) {
			System.out.println("Error while creating file.");
			e.printStackTrace();
		}


		//printCasts();
		inconsistencyFile.close();
	}

	private void parseXMLMain() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			domMain = documentBuilder.parse("mains243.xml");
		} catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
	}

	private void parseXMLCast() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			domCast = documentBuilder.parse("casts124.xml");
		} catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
	}

	private void parseXMLActors() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			domActors = documentBuilder.parse("actors63.xml");
		
		} catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
	}

	private void parseMainDocument() throws Exception{
		Element documentElement = domMain.getDocumentElement();
		categoryMap.put("romt", "Romance");
		categoryMap.put("dram", "Drama");
		categoryMap.put("advt", "Adventure");
		categoryMap.put("actn", "Action");
		categoryMap.put("comd", "Comedy");
		categoryMap.put("scfi", "Sci-Fi");
		categoryMap.put("musc", "Music");
		categoryMap.put("docu", "Documentary");
		categoryMap.put("susp", "Thriller");
		categoryMap.put("fant", "Fantasy");
		categoryMap.put("porn", "Adult");
		categoryMap.put("horr", "Horror");
		categoryMap.put("cart", "Cartoon");
		categoryMap.put("biop", "Biography");
		categoryMap.put("hist", "History");


		NodeList nodeList = documentElement.getElementsByTagName("film");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);

				Movie movie = parseMovie(element);
				if (movie != null) {
					// handle duplicates
					if (!seenTitles.contains(movie.getTitle()) || !seenFid.contains(movie.getFid())) {
						seenTitles.add(movie.getTitle());
						seenFid.add(movie.getFid());
						movies.add(movie);
						movieMap.put(movie.getTitle(), movie.getMovieId());					
					}
					else {
						inconsistencyFile.write(movie.getTitle() + "\t" + "item: " + i + "\tmovie is duplicate\n");
						movieInconsistency += 1;
						// movie is a duplicate
					}
				}

			}
		}
	}

	private void parseCastsDocument() throws IOException {
		Element documentElement = domCast.getDocumentElement();
		NodeList nodeList = documentElement.getElementsByTagName("filmc");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				parseCasts(element);
			}
		}		
	}

	private void parseActorsDocument() throws IOException {
		Element documentElement = domActors.getDocumentElement();
		NodeList nodeList = documentElement.getElementsByTagName("actor");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				// get the actor element
				Element element = (Element) nodeList.item(i);

				// get actor object
				Actor actor = parseActor(element);
				if (actor != null) {
					if (!seenActors.contains(actor.getName())) {
						seenActors.add(actor.getName());
						actors.add(actor);
						actorMap.put(actor.getName(), actor.getStarId());
					}
					else
					{
						inconsistencyFile.write(actor.getName() + "\t" + "item: " + i + "\tactor is duplicate\n");
						inconsistency += 1;
					}
				}
			}
		}
	}

	private int parseCasts(Element element) throws IOException {
		NodeList castList = element.getElementsByTagName("a");
		String castTitle = getTextValue(element, "t");
		HashSet<String> castSet = new HashSet<String>();
		if (castList != null && castList.getLength() > 0) {
			for (int i = 0; i < castList.getLength(); i++) {
				if (castList.item(i).getFirstChild() == null) {
					castInconsistency += 1;
					break;
				}
				String castName = castList.item(i).getFirstChild().getNodeValue();
				if (castName == null) {
					inconsistencyFile.write(castName + "\tcast name is NULL\n");
					castInconsistency += 1;
					break;
				}
				else {
					if (!seenActors.contains(castName.trim()) || !seenTitles.contains(castTitle))
					{
						inconsistencyFile.write(castName + "\tcast is not in actors\n");
						castInconsistency += 1;
						break;
					}
					else {
						castSet.add(castName.trim());
					}
				}
			}
		}
		cast.put(castTitle, castSet);
		return 0;
	}

	private Movie parseMovie(Element element) throws Exception{
		String title = getTextValue(element, "t");
		String fid = getTextValue(element, "fid");
		String director = getTextValue(element, "dirn");
		int year = getIntValue(element, "year");

		if (title == null) {
			// no title, inconsistency
			inconsistencyFile.write(title + "\t" + element + "\tNo title\n");
			movieInconsistency += 1;
			return null;
		}
		if (director == null) {
			// inconsistency
			movieInconsistency += 1;
			return null;
		}

		int count = 0;
		List<String> cats = new ArrayList<String>();
		NodeList nodeList = element.getElementsByTagName("cat");
		if (nodeList != null && nodeList.getLength() > 0) {
			if (nodeList.item(count).getFirstChild() != null) {
				String itemCategory = nodeList.item(count).getFirstChild().getNodeValue();
				if (itemCategory != null) {
					itemCategory = itemCategory.toLowerCase();
					if (categoryMap.containsKey(itemCategory)) {
						cats.add(categoryMap.get(itemCategory));
					}
					else {
						cats.add(itemCategory);
						if (!genreMap.containsKey((itemCategory)))
						{
							genreMap.put(itemCategory, genreMap.size() + 1);
						}

					}
					count++;
				}
			}
			else {
				// Handle this inconsistency too
				inconsistencyFile.write(nodeList.item(count).getFirstChild() + "\t" + "child is null\n");
				movieInconsistency += 1;
				return null;
			}
		}
		title = title.trim();
		m_id += 1;
		String movieId = genMovieId(m_id);
		return new Movie(title, director, fid, year, cats, movieId);
	}

	private Actor parseActor(Element element) throws IOException {
		String first = getTextValue(element, "firstname");
		String last = getTextValue(element, "familyname");
		String name;
		if (first == null || last == null) {
			name = getTextValue(element, "stagename");
			if (name == null)
			{
				inconsistencyFile.write(first);
				inconsistencyFile.write("t");
				inconsistencyFile.write(last);
				inconsistencyFile.write("\tfirst or last name is null");
				inconsistency += 1;
				return null;
			}
		}
		else
		{
			name = first.trim() + " " + last.trim();
		}
		int dob = getIntValue(element, "dob");
		s_id += 1;
		String starId = genStarId(s_id);
		return new Actor(name, dob, starId);
	}

	private String getTextValue(Element element, String tagName) {
		String textVal = null;
		NodeList nodeList = element.getElementsByTagName(tagName);
		if (nodeList != null && nodeList.getLength() > 0) {
			if (nodeList.item(0).getFirstChild() != null) {
				textVal = nodeList.item(0).getFirstChild().getNodeValue();
			}
		}
		return textVal;
	}

	private int getIntValue(Element ele, String tagName) {
		String result = getTextValue(ele, tagName);
		if (result == null) {
			return 0;
		}
		try
		{
			return Integer.parseInt(getTextValue(ele, tagName));
		} catch (NumberFormatException error) {
			//error.printStackTrace();
			return 0;
		}
	}

	private void printData() {
		for (Actor actor : actors) {
			System.out.println("\t" + actor.toString());
		}
		System.out.println("Total parsed: " + actors.size() + " actors");
		System.out.print("Inconsistencies: ");
		System.out.println(inconsistency);
	}

	private void printMovies() {
		for (Movie movie : movies) {
			System.out.println("\t" + movie.toString());
		}
		System.out.println("Total parsed: " + movies.size() + " movies");
		System.out.print("Movie Inconsistencies: ");
		System.out.println(movieInconsistency);
	}

	private void printCasts() {
		for (String title : cast.keySet()) {
			String result = "";
			result += title + " cast: "; 
			for (String castName : cast.get(title)) {
				result += castName + ", ";
			}
			System.out.println(result);
		}
		System.out.println("cast inconsistency: " + castInconsistency);
	}

	private String genMovieId(Integer id) {
		int newIdInt = maxMovieId + id;
		String newId = String.valueOf(newIdInt);
       	String zeroes = "";
        for (int i = newId.length(); i < 7; i++) {
            zeroes += "0";
        }
        String final_id = "tt" + zeroes + newId;
        return final_id;
	}

	private String genStarId(Integer id) {
		int newIdInt = maxStarId + id;
		String newId = String.valueOf(newIdInt);
       	String zeroes = "";
        for (int i = newId.length(); i < 7; i++) {
            zeroes += "0";
        }
        String final_id = "nm" + zeroes + newId;
        return final_id;
	}

	public static void main(String[] args) throws Exception {
		DomParser domParser = new DomParser();
		domParser.run();
	}
}







