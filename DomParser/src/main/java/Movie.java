import java.util.ArrayList;
import java.util.List;

public class Movie {
	private final String title;

	private final String director;

	private final String fid; 

	private final int year;

	private final List<String> categories;

	private final String id;

	public Movie(String title, String director, String fid, int year, List<String> categories, String id)
	{
		this.title = title;
		this.director = director;
		this.fid = fid;
		this.year = year;
		this.categories = categories;
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public String getDirector() {
		return director;
	}

	public String getFid() {
		return fid;
	}

	public int getYear() {
		return year;
	}

	public List<String> getCategories() {
		return this.categories;
	}

	public String getMovieId() {
		return id;
	}

	public String toString() {
		String result = "Title: " + getTitle() + ", " +
				"Director: " + getDirector() + ", " +
				"Fid: " + getFid() + ", " +
				"Year: " + getYear() + ", " + 
				"movieId: " + getMovieId() + ", " + 
				"Categories ";
		for (String category : categories) {
			result += category + " ";
		}
		return result;
	}
}