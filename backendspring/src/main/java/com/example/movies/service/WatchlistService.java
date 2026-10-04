package com.example.movies.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.movies.model.Movie;
import com.example.movies.model.User;
import com.example.movies.model.WatchlistItem;
import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.UserRepository;
import com.example.movies.repository.WatchlistRepository;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public WatchlistService(WatchlistRepository watchlistRepository, UserRepository userRepository, MovieRepository movieRepository) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public WatchlistItem addOrUpdate(String username, Long movieId, String status) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        WatchlistItem item = watchlistRepository.findByUserAndMovieAndListType(user, movie, "WATCHLIST")
                .or(() -> watchlistRepository.findByUserAndMovie(user, movie)
                        .filter(existing -> existing.getListType() == null))
                .orElseGet(WatchlistItem::new);
        item.setUser(user);
        item.setMovie(movie);
        item.setStatus(status);
        item.setListType("WATCHLIST");
        item.setAddedAt(LocalDateTime.now());
        return watchlistRepository.save(item);
    }

    public void remove(String username, Long movieId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        watchlistRepository.findByUserAndMovieAndListType(user, movie, "WATCHLIST")
                .or(() -> watchlistRepository.findByUserAndMovie(user, movie)
                        .filter(item -> item.getListType() == null))
                .ifPresent(watchlistRepository::delete);
    }

    public List<Map<String, Object>> getWatchlist(String username) {
        return getCollection(username, "WATCHLIST");
    }

    public WatchlistItem addFavorite(String username, Long movieId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        WatchlistItem item = watchlistRepository.findByUserAndMovieAndListType(user, movie, "FAVORITE")
                .orElse(new WatchlistItem());
        item.setUser(user);
        item.setMovie(movie);
        item.setStatus("FAVORITE");
        item.setListType("FAVORITE");
        item.setAddedAt(LocalDateTime.now());
        return watchlistRepository.save(item);
    }

    public void removeFavorite(String username, Long movieId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        watchlistRepository.findByUserAndMovieAndListType(user, movie, "FAVORITE")
                .ifPresent(watchlistRepository::delete);
    }

    public List<Map<String, Object>> getFavorites(String username) {
        return getCollection(username, "FAVORITE");
    }

    private List<Map<String, Object>> getCollection(String username, String listType) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<WatchlistItem> items = new ArrayList<>(watchlistRepository.findByUserAndListType(user, listType));
        if ("WATCHLIST".equals(listType)) {
            items.addAll(watchlistRepository.findByUser(user).stream()
                    .filter(item -> item.getListType() == null).toList());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (WatchlistItem item : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("status", item.getStatus());
            m.put("addedAt", item.getAddedAt().toString());
            Movie movie = item.getMovie();
            Map<String, Object> movieMap = new HashMap<>();
            movieMap.put("id", movie.getId());
            movieMap.put("title", movie.getTitle());
            movieMap.put("description", movie.getDescription());
            movieMap.put("genre", movie.getGenre());
            movieMap.put("duration", movie.getDuration());
            movieMap.put("screeningDate", movie.getScreeningDate());
            movieMap.put("imageUrl", movie.getImageUrl());
            m.put("movie", movieMap);
            result.add(m);
        }
        return result;
    }

    public Map<String, Long> getWatchlistStats() {
        List<WatchlistItem> all = watchlistRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        for (WatchlistItem item : all) {
            stats.merge(item.getStatus(), 1L, Long::sum);
        }
        return stats;
    }
}
