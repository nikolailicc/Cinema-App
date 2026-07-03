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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Prikazuje listu svih filmova sa backenda. Koristi se i na telefonu (jedini panel)
 * i na tabletu (levi panel u master-detail rasporedu).
 */
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

        adapter = new MovieAdapter(this, movieId -> localStore.isInWatchlist(movieId));
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
    public void onWatchlistToggle(Movie movie, boolean currentlyInWatchlist) {
        ApiService api = RetrofitClient.getApiService(requireContext());
        if (currentlyInWatchlist) {
            api.removeFromWatchlist(movie.getId()).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    localStore.remove(movie.getId());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Map<String, String> body = new HashMap<>();
            body.put("status", "PLANNED");
            api.addToWatchlist(movie.getId(), body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    localStore.add(movie);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
