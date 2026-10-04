package com.example.cinebook.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinebook.R;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.Movie;
import com.example.cinebook.model.WatchlistEntry;
import com.example.cinebook.ui.adapter.MovieAdapter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionActivity extends AppCompatActivity implements MovieAdapter.OnMovieClickListener {
    private static final String FAVORITES = "favorites";
    public static Intent intent(Context context, boolean favorites) {
        return new Intent(context, CollectionActivity.class).putExtra(FAVORITES, favorites);
    }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_collection);
        Toolbar toolbar = findViewById(R.id.toolbar); setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        boolean favorites = getIntent().getBooleanExtra(FAVORITES, false);
        toolbar.setTitle(favorites ? R.string.favorites : R.string.watchlist);
        RecyclerView recycler = findViewById(R.id.recyclerCollection);
        TextView empty = findViewById(R.id.textCollectionEmpty);
        MovieAdapter adapter = new MovieAdapter(this, id -> favorites);
        recycler.setLayoutManager(new LinearLayoutManager(this)); recycler.setAdapter(adapter);
        Call<List<WatchlistEntry>> call = favorites ? RetrofitClient.getApiService(this).getMyFavorites()
                : RetrofitClient.getApiService(this).getMyWatchlist();
        call.enqueue(new Callback<List<WatchlistEntry>>() {
            @Override public void onResponse(Call<List<WatchlistEntry>> c, Response<List<WatchlistEntry>> r) {
                List<Movie> movies = new ArrayList<>();
                if (r.isSuccessful() && r.body() != null) for (WatchlistEntry e : r.body()) if (e.getMovie() != null) movies.add(e.getMovie());
                adapter.setMovies(movies); empty.setVisibility(movies.isEmpty() ? TextView.VISIBLE : TextView.GONE);
            }
            @Override public void onFailure(Call<List<WatchlistEntry>> c, Throwable t) {
                Toast.makeText(CollectionActivity.this, "Greška pri učitavanju liste", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override public void onMovieClick(Movie movie) {
        startActivity(new Intent(this, MovieDetailActivity.class).putExtra(MovieDetailActivity.EXTRA_MOVIE, movie));
    }
    @Override public void onFavoriteToggle(Movie movie, boolean currentlyFavorite) { }
}
