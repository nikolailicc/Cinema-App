package com.example.movies.repository;

import com.example.movies.model.Movie;
import com.example.movies.model.Rating;
import com.example.movies.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndMovie(User user, Movie movie);
    List<Rating> findByMovie(Movie movie);

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.movie = :movie")
    Double findAverageStarsByMovie(Movie movie);

    @Query("SELECT r.movie.genre, AVG(r.stars) FROM Rating r GROUP BY r.movie.genre")
    List<Object[]> findAverageRatingByGenre();
}