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

/**
 * Glavna Activity. Ako layout ima "detail_container" (sw600dp / tablet), radi se
 * o master-detail rasporedu i detalji filma se prikazuju u drugom panelu iste Activity.
 * Na telefonu detail_container ne postoji, pa se detalji otvaraju u posebnoj Activity-ju.
 */
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
        // Osvezi listu (npr. nakon dodavanja/izmene filma ili promene watchlist-e)
        MovieListFragment fragment = (MovieListFragment) getSupportFragmentManager().findFragmentByTag("list");
        if (fragment != null) {
            fragment.loadMovies();
        }
    }
}
