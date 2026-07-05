package com.example.movies.repository;

import com.example.movies.model.Movie;
import com.example.movies.model.User;
import com.example.movies.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUser(User user);
    Optional<WatchlistItem> findByUserAndMovie(User user, Movie movie);
    boolean existsByUserAndMovie(User user, Movie movie);
    void deleteByMovie(Movie movie);
}