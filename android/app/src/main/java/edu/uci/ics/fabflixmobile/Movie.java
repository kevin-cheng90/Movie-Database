package edu.uci.ics.fabflixmobile;

import java.util.ArrayList;

public class Movie {
    private String name;
    private short year;
    private String director;
    private ArrayList<String> stars;
    private ArrayList<String> genres;
    private String movieId;


    public Movie(String name, short year, String director,
                 ArrayList<String> stars, ArrayList<String> genres, String movieId) {
        this.name = name;
        this.year = year;
        this.director = director;
        this.stars = stars;
        this.genres = genres;
        this.movieId = movieId;
    }

    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() { return director; }

    public ArrayList<String> getStars()  { return stars; }

    public ArrayList<String> getGenres() { return genres; }

    public String getMovieId() { return movieId; }
}