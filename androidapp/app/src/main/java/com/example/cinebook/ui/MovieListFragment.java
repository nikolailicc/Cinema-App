package com.example.cinebook.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cinebook.R;
import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.Movie;
import com.example.cinebook.provider.WatchlistLocalStore;
import com.example.cinebook.ui.adapter.MovieAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieListFragment extends Fragment implements MovieAdapter.OnMovieClickListener {

    public interface MovieSelectionListener {
        void onMovieSelected(Movie movie);
    }

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private MovieAdapter adapter;
    private WatchlistLocalStore localStore;
    private MovieSelectionListener listener;
    private final Set<Long> favoriteIds = new HashSet<>();

    public void setListener(MovieSelectionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerMovies);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        textEmpty = view.findViewById(R.id.textEmpty);

        localStore = new WatchlistLocalStore(requireContext());

        adapter = new MovieAdapter(this, movieId -> favoriteIds.contains(movieId));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadMovies);

        loadMovies();
    }

    public void loadMovies() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getAllMovies().enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body();
                    adapter.setMovies(movies);
                    loadFavorites();
                    textEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(requireContext(), "Greška pri učitavanju filmova", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMovieClick(Movie movie) {
        if (listener != null) listener.onMovieSelected(movie);
    }

    @Override
    public void onFavoriteToggle(Movie movie, boolean currentlyFavorite) {
        ApiService api = RetrofitClient.getApiService(requireContext());
        if (currentlyFavorite) {
            api.removeFromFavorites(movie.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        favoriteIds.remove(movie.getId());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), "Greška pri uklanjanju iz omiljenih", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            api.addToFavorites(movie.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        favoriteIds.add(movie.getId());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), favoriteError(response), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String favoriteError(Response<Void> response) {
        String message = "Greška pri dodavanju u omiljene (HTTP " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                String body = response.errorBody().string().trim();
                if (!body.isEmpty()) message += ": " + body;
            } catch (IOException ignored) {
            }
        }
        return message;
    }

    private void loadFavorites() {
        RetrofitClient.getApiService(requireContext()).getMyFavorites().enqueue(new Callback<List<com.example.cinebook.model.WatchlistEntry>>() {
            @Override public void onResponse(Call<List<com.example.cinebook.model.WatchlistEntry>> call,
                                             Response<List<com.example.cinebook.model.WatchlistEntry>> response) {
                favoriteIds.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (com.example.cinebook.model.WatchlistEntry entry : response.body()) {
                        if (entry.getMovie() != null && entry.getMovie().getId() != null) {
                            favoriteIds.add(entry.getMovie().getId());
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<com.example.cinebook.model.WatchlistEntry>> call, Throwable t) {
                Toast.makeText(requireContext(), "Greške pri učitavanju omiljenih filmova", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
