package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private final ArrayList<Movie> movies;

    public MovieListViewAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.row, movies);
        this.movies = movies;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.row, parent, false);

        Movie movie = movies.get(position);

        TextView titleView = view.findViewById(R.id.title);
        TextView yearView = view.findViewById(R.id.year);
        TextView directorView = view.findViewById(R.id.director);
        TextView starsView = view.findViewById(R.id.stars);
        TextView genresView = view.findViewById(R.id.genres);

        titleView.setText(movie.getName());
        // need to cast the year to a string to set the label
        yearView.setText(movie.getYear() + "");
        directorView.setText("Director: " + movie.getDirector());
        String starsText = "";
        for (int i = 0; i < movie.getStars().size(); i++) {
            if (i == movie.getStars().size()-1) {
                starsText += movie.getStars().get(i);
            }
            else {
                starsText += movie.getStars().get(i) + ", ";
            }
        }
        starsView.setText("Stars: " + starsText);
        String genresText = "";
        for (int i = 0; i < movie.getGenres().size(); i++) {
            if (i == movie.getGenres().size()-1) {
                genresText += movie.getGenres().get(i);
            }
            else {
                genresText += movie.getGenres().get(i) + ", ";
            }
        }
        genresView.setText("Genres: " + genresText);
        return view;
    }
}