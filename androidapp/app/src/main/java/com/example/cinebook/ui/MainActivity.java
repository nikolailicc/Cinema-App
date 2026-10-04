package com.example.cinebook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cinebook.R;
import com.example.cinebook.model.Movie;
import com.example.cinebook.util.SessionManager;

public class MainActivity extends AppCompatActivity implements MovieListFragment.MovieSelectionListener {

    private boolean isTabletLayout;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.quickWatchlist).setOnClickListener(v ->
                startActivity(CollectionActivity.intent(this, false)));
        findViewById(R.id.quickFavorites).setOnClickListener(v ->
                startActivity(CollectionActivity.intent(this, true)));
        findViewById(R.id.quickMap).setOnClickListener(v ->
                startActivity(new Intent(this, CinemaMapActivity.class)));

        isTabletLayout = findViewById(R.id.detail_container) != null;

        if (savedInstanceState == null) {
            MovieListFragment listFragment = new MovieListFragment();
            listFragment.setListener(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_container, listFragment, "list")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_movie).setVisible(session.isAdmin());
        menu.findItem(R.id.action_cinema_settings).setVisible(session.isAdmin());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@Nullable MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_movie) {
            startActivity(new Intent(this, AddEditMovieActivity.class));
            return true;
        } else if (id == R.id.action_reservations) {
            startActivity(new Intent(this, MyReservationsActivity.class));
            return true;
        } else if (id == R.id.action_map) {
            startActivity(new Intent(this, CinemaMapActivity.class));
            return true;
        } else if (id == R.id.action_watchlist) {
            startActivity(CollectionActivity.intent(this, false));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(CollectionActivity.intent(this, true));
            return true;
        } else if (id == R.id.action_cinema_settings) {
            startActivity(new Intent(this, CinemaSettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            session.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(Movie movie) {
        if (isTabletLayout) {
            MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(movie);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, detailFragment, "detail")
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE, movie);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MovieListFragment fragment = (MovieListFragment) getSupportFragmentManager().findFragmentByTag("list");
        if (fragment != null) {
            fragment.loadMovies();
        }
    }
}
