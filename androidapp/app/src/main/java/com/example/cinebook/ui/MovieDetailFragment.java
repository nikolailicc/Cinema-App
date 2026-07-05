package com.example.cinebook.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinebook.R;
import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.Movie;
import com.example.cinebook.model.Reservation;
import com.example.cinebook.notification.ReminderScheduler;
import com.example.cinebook.provider.WatchlistLocalStore;
import com.example.cinebook.ui.adapter.CommentAdapter;
import com.example.cinebook.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "arg_movie";

    private Movie movie;
    private SessionManager session;
    private WatchlistLocalStore localStore;
    private CommentAdapter commentAdapter;

    private ImageView imagePoster;
    private TextView textTitle, textMeta, textDescription;
    private RatingBar ratingBar;
    private Button btnWatchlist, btnReserve, btnEdit, btnDelete, btnAddComment;
    private LinearLayout adminActions;
    private EditText editComment;
    private RecyclerView recyclerComments;

    public static MovieDetailFragment newInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movie = (Movie) getArguments().getSerializable(ARG_MOVIE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        localStore = new WatchlistLocalStore(requireContext());

        imagePoster = view.findViewById(R.id.imagePoster);
        textTitle = view.findViewById(R.id.textTitle);
        textMeta = view.findViewById(R.id.textMeta);
        textDescription = view.findViewById(R.id.textDescription);
        ratingBar = view.findViewById(R.id.ratingBar);
        btnWatchlist = view.findViewById(R.id.btnWatchlist);
        btnReserve = view.findViewById(R.id.btnReserve);
        adminActions = view.findViewById(R.id.adminActions);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        editComment = view.findViewById(R.id.editComment);
        btnAddComment = view.findViewById(R.id.btnAddComment);
        recyclerComments = view.findViewById(R.id.recyclerComments);

        commentAdapter = new CommentAdapter();
        recyclerComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerComments.setAdapter(commentAdapter);

        bindMovie();
        loadRating();
        loadComments();

        adminActions.setVisibility(session.isAdmin() ? View.VISIBLE : View.GONE);

        btnWatchlist.setOnClickListener(v -> toggleWatchlist());
        btnReserve.setOnClickListener(v -> showReservationDialog());
        btnAddComment.setOnClickListener(v -> addComment());
        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            if (fromUser) rateMovie((int) rating);
        });

        btnEdit.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), AddEditMovieActivity.class);
            intent.putExtra(AddEditMovieActivity.EXTRA_MOVIE, movie);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void bindMovie() {
        textTitle.setText(movie.getTitle());
        textMeta.setText(movie.getGenre() + " • " + movie.getDuration() + " min • " + movie.getScreeningDate());
        textDescription.setText(movie.getDescription());

        boolean inWatchlist = localStore.isInWatchlist(movie.getId());
        btnWatchlist.setText(inWatchlist ? R.string.remove_from_watchlist : R.string.add_to_watchlist);

        if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
            Glide.with(this).load(movie.getImageUrl()).centerCrop().into(imagePoster);
        }
    }

    private void loadRating() {
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getRating(movie.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object avg = response.body().containsKey("average")
                            ? response.body().get("average") : response.body().get("rating");
                    if (avg instanceof Number) {
                        ratingBar.setRating(((Number) avg).floatValue());
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Tiho ignorisemo - ocena nije kriticna za prikaz filma
            }
        });
    }

    private void loadComments() {
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getComments(movie.getId()).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    commentAdapter.setComments(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Greška pri učitavanju komentara", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment() {
        String text = editComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        Map<String, String> body = new HashMap<>();
        body.put("content", text);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.addComment(movie.getId(), body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                editComment.setText("");
                loadComments();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rateMovie(int stars) {
        Map<String, Integer> body = new HashMap<>();
        body.put("stars", stars);
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.rateMovie(movie.getId(), body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ocena sačuvana", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Greška pri ocenjivanju", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWatchlist() {
        boolean inWatchlist = localStore.isInWatchlist(movie.getId());
        ApiService api = RetrofitClient.getApiService(requireContext());

        if (inWatchlist) {
            api.removeFromWatchlist(movie.getId()).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    localStore.remove(movie.getId());
                    btnWatchlist.setText(R.string.add_to_watchlist);
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
                    btnWatchlist.setText(R.string.remove_from_watchlist);
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showReservationDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.number_of_tickets);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reserve)
                .setView(input)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    int tickets = TextUtils.isEmpty(value) ? 1 : Integer.parseInt(value);
                    createReservation(tickets);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void createReservation(int tickets) {
        Map<String, Integer> body = new HashMap<>();
        body.put("userId", (int) session.getUserId());
        body.put("movieId", movie.getId().intValue());
        body.put("numberOfTickets", tickets);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.createReservation(body).enqueue(new Callback<Reservation>() {
            @Override
            public void onResponse(Call<Reservation> call, Response<Reservation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Reservation reservation = response.body();
                    Toast.makeText(requireContext(), R.string.reservation_created, Toast.LENGTH_SHORT).show();

                    // Zakazivanje notifikacije - podsetnik na dan prikazivanja filma
                    if (reservation.getId() != null) {
                        ReminderScheduler.scheduleReminder(requireContext(),
                                reservation.getId(), movie.getTitle(), movie.getScreeningDate());
                    }
                } else {
                    Toast.makeText(requireContext(), "Rezervacija nije uspela", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Reservation> call, Throwable t) {
                Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(movie.getTitle())
                .setPositiveButton(R.string.confirm, (dialog, which) -> deleteMovie())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteMovie() {
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.deleteMovie(movie.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(requireContext(), "Film obrisan", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().onBackPressed();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
