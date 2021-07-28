package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.EditText;

import java.lang.reflect.Array;
import java.util.ArrayList;


import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

public class Search extends Activity {
    private EditText searchTitle;
    private Button searchButton;
    private final String host = "10.0.2.2";
    private final String port = "8443";
    private final String domain = "cs122b-spring21-team-52";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        searchTitle = findViewById(R.id.title);
        searchButton = findViewById(R.id.enter);

        // Handles when user clicks search
        searchTitle.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                keyCode == KeyEvent.KEYCODE_ENTER) {
                    enter();
                    return true;
                }
                return false;
            }
        });
        searchButton.setOnClickListener(view -> enter());
    }

    public void enter() {
        String requestURL = baseURL + "/api/movies?&category=title&N=20&search=" + searchTitle.getText().toString();
        if (!searchTitle.getText().toString().isEmpty()) {
            Intent listPage = new Intent(Search.this, ListViewActivity.class);
            listPage.putExtra("requestURL", requestURL);
            startActivity(listPage);
        }

    }
}
