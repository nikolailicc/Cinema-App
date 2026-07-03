package com.example.movies.service;

import com.example.movies.model.*;
import com.example.movies.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

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

        WatchlistItem item = watchlistRepository.findByUserAndMovie(user, movie).orElse(new WatchlistItem());
        item.setUser(user);
        item.setMovie(movie);
        item.setStatus(status);
        item.setAddedAt(LocalDateTime.now());
        return watchlistRepository.save(item);
    }

    public void remove(String username, Long movieId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        watchlistRepository.findByUserAndMovie(user, movie).ifPresent(watchlistRepository::delete);
    }

    public List<Map<String, Object>> getWatchlist(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<WatchlistItem> items = watchlistRepository.findByUser(user);
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
            movieMap.put("genre", movie.getGenre());
            movieMap.put("duration", movie.getDuration());
            m.put("movie", movieMap);
            result.add(m);
        }
        return result;
    }

    // Za admin statistiku — broj watchlist po statusu
    public Map<String, Long> getWatchlistStats() {
        List<WatchlistItem> all = watchlistRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        for (WatchlistItem item : all) {
            stats.merge(item.getStatus(), 1L, Long::sum);
        }
        return stats;
    }
}
