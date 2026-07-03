package com.example.cinebook.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinebook.R;
import com.example.cinebook.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);

        void onWatchlistToggle(Movie movie, boolean currentlyInWatchlist);
    }

    private List<Movie> movies = new ArrayList<>();
    private final OnMovieClickListener listener;
    private final WatchlistChecker watchlistChecker;

    public interface WatchlistChecker {
        boolean isInWatchlist(long movieId);
    }

    public MovieAdapter(OnMovieClickListener listener, WatchlistChecker watchlistChecker) {
        this.listener = listener;
        this.watchlistChecker = watchlistChecker;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies != null ? movies : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.title.setText(movie.getTitle());
        holder.genre.setText(movie.getGenre());
        holder.date.setText(movie.getScreeningDate());

        if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(movie.getImageUrl())
                    .centerCrop()
                    .into(holder.poster);
        } else {
            holder.poster.setImageDrawable(null);
        }

        boolean inWatchlist = watchlistChecker != null && watchlistChecker.isInWatchlist(movie.getId());
        holder.watchlistIcon.setImageResource(inWatchlist
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        holder.itemView.setOnClickListener(v -> listener.onMovieClick(movie));
        holder.watchlistIcon.setOnClickListener(v -> listener.onWatchlistToggle(movie, inWatchlist));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster, watchlistIcon;
        TextView title, genre, date;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.imagePoster);
            title = itemView.findViewById(R.id.textTitle);
            genre = itemView.findViewById(R.id.textGenre);
            date = itemView.findViewById(R.id.textDate);
            watchlistIcon = itemView.findViewById(R.id.imageWatchlist);
        }
    }
}
