package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class singleMovie extends Activity {


    private TextView titleView;
    private TextView yearView;
    private TextView directorView;
    private TextView starsView;
    private TextView genresView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovie);
        String singleMovieUrl;

        titleView = findViewById(R.id.singletitle);
        yearView = findViewById(R.id.singleyear);
        directorView = findViewById(R.id.singledirector);
        starsView = findViewById(R.id.singlestars);
        genresView = findViewById(R.id.singlegenres);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                singleMovieUrl = null;
            }
            else {
                singleMovieUrl = extras.getString("singleMovie");
            }
        }
        else {
            singleMovieUrl = (String) savedInstanceState.getSerializable("singleMovie");
        }

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        queue.add(createSingleMovieRequest(singleMovieUrl));

    }

    public StringRequest createSingleMovieRequest (String url) {
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    Log.d("single movie response", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        titleView.setText(obj.getString("movie_title"));
                        yearView.setText(obj.getString("movie_year"));
                        directorView.setText(obj.getString("movie_director"));
                        String allStars = "";
                        JSONArray starsArr = new JSONArray(obj.get("stars").toString());
                        for (int i = 0; i < starsArr.length(); i++)
                        {
                            if (i == starsArr.length()-1) {
                                allStars += starsArr.getJSONObject(i).getString("name");
                            }
                            else
                            {
                                allStars += starsArr.getJSONObject(i).getString("name") + ", ";
                            }
                            Log.d("all stars", allStars);
                        }
                        starsView.setText("Stars: " + allStars);
                        String allGenres = "";
                        allGenres += obj.getString("genres").replace("[", "").replace("]", "");
                        genresView.setText("Genres: " + allGenres);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("single movie error", error.toString());
                }
        );
        return searchRequest;
    }
}
