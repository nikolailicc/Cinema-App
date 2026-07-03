package com.example.cinebook.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cinebook.R;
import com.example.cinebook.model.Movie;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Movie movie = (Movie) getIntent().getSerializableExtra(EXTRA_MOVIE);
        if (getSupportActionBar() != null && movie != null) {
            getSupportActionBar().setTitle(movie.getTitle());
        }

        if (savedInstanceState == null && movie != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, MovieDetailFragment.newInstance(movie))
                    .commit();
        }
    }
}
