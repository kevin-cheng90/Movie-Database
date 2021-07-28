package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ListViewActivity extends Activity {
    private Button prevButton;
    private Button nextButton;

    private final String host = "10.0.2.2";
    private final String port = "8443";
    private final String domain = "cs122b-spring21-team-52";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    private ArrayList<Movie> movies = new ArrayList<Movie>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        // TODO: this should be retrieved from the backend server
        ArrayList<Movie> movies = new ArrayList<Movie>();
        String movieURL;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                movieURL = null;
            }
            else {
                movieURL = extras.getString("requestURL");
            }
        }
        else {
            movieURL = (String) savedInstanceState.getSerializable("requestURL");
        }
        Log.d("requestURL", movieURL);
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        queue.add(createMovieRequest(movieURL));
    }


    public StringRequest createMovieRequest (String url) {
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    // Clear movies if we get a new, successful request
                    movies = new ArrayList<Movie>();
                    Log.d("movies", response);
                    try {
                        nextButton = findViewById(R.id.next);
                        prevButton = findViewById(R.id.prev);
                        JSONArray objArr = new JSONArray(response);
                        // obj[0] is jsonArray of the movies [ {movie}, {movie} ]
                        // obj[1] is parameters [ {page:1, sort:rating, orderFirst:desc} ]

                        for (int j = 0; j < objArr.getJSONArray(0).length(); j++) {
                            JSONObject obj = objArr.getJSONArray(0).getJSONObject(j);
                            // (String name, short year, String director, ArrayList<String> stars,
                            // ArrayList<String> genres, String movieId)
                            ArrayList<String> starsArray = new ArrayList<String>();
                            ArrayList<String> genresArray = new ArrayList<String>();
                            int i = 0;
                            if (obj.getJSONArray("stars") != null) {
                                for (i = 0; i < obj.getJSONArray("stars").length(); i++) {
                                    starsArray.add(obj.getJSONArray("stars").getString(i));
                                }
                            }
                            if (obj.getJSONArray("genres") != null) {
                                for (i = 0; i < obj.getJSONArray("genres").length(); i++) {
                                    genresArray.add(obj.getJSONArray("genres").getString(i));
                                }
                            }
                            Movie individualMovie = new Movie(obj.getString("movie_title"),
                                    Short.valueOf(obj.getString("movie_year")),
                                    obj.getString("movie_director"),
                                    starsArray,
                                    genresArray,
                                    obj.getString("movie_id"));
                            movies.add(individualMovie);
                        }
                        for (int i = 0; i < movies.size(); i++) {
                            Log.d(Integer.valueOf(i) + "title", movies.get(i).getName());
                        }
                        int page = objArr.getJSONArray(1).getJSONObject(0).getInt("page");

                        if (movies.size() == 20)
                        {
                            nextButton.setOnClickListener(view -> onNextClick(url, page));
                        }
                        if (page > 1)
                        {
                            prevButton.setOnClickListener(view -> onPrevClick(url, page));
                        }
                        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);
                        ListView listView = findViewById(R.id.list);

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            // Link to single movie page here
                            Movie movie = movies.get(position);
                            Intent singlePage = new Intent(ListViewActivity.this, singleMovie.class);
                            String singleMovieUrl = baseURL + "/api/single-movie?id=" + movie.getMovieId();
                            singlePage.putExtra("singleMovie", singleMovieUrl);
                            startActivity(singlePage);

                            String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("search error", error.toString());
                }
        );
        return searchRequest;
    }

    public void onNextClick(String nextURL, int currPage) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        String newUrl;
        if (nextURL.contains("page=" + Integer.valueOf(currPage))) {
            newUrl = nextURL.replace("page=" + Integer.valueOf(currPage), "");
            newUrl += "&page=" + Integer.valueOf(currPage+1);
        }
        else
        {
            newUrl = nextURL;
            newUrl += "&page=" + Integer.valueOf(currPage+1);
        }
        Log.d("next url:", newUrl);
        queue.add(createMovieRequest(newUrl));
    }

    public void onPrevClick(String nextURL, int currPage) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        String newUrl;
        if (nextURL.contains("page=" + Integer.valueOf(currPage))) {
            newUrl = nextURL.replace("page=" + Integer.valueOf(currPage), "");
            newUrl += "&page=" + Integer.valueOf(currPage-1);
        }
        else {
            newUrl = nextURL;
            newUrl += "&page=" + Integer.valueOf(currPage-1);
        }
        Log.d("prev url", newUrl);
        queue.add(createMovieRequest(newUrl));
    }

    // need to get individual movie
    // pass url
    // parse json
}