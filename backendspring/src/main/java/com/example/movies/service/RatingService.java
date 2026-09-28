package com.example.movies.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.movies.model.Movie;
import com.example.movies.model.Rating;
import com.example.movies.model.User;
import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.RatingRepository;
import com.example.movies.repository.UserRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository, MovieRepository movieRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public Rating rateMovie(String username, Long movieId, int stars) {
        if (stars < 1 || stars > 5) throw new RuntimeException("Ocena mora biti između 1 i 5");
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        Rating rating = ratingRepository.findByUserAndMovie(user, movie).orElse(new Rating());
        rating.setUser(user);
        rating.setMovie(movie);
        rating.setStars(stars);
        rating.setCreatedAt(LocalDateTime.now());
        return ratingRepository.save(rating);
    }

    public Map<String, Object> getMovieRating(String username, Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        Double avg = ratingRepository.findAverageStarsByMovie(movie);
        long count = ratingRepository.findByMovie(movie).size();

        Map<String, Object> result = new HashMap<>();
        result.put("average", avg != null ? Math.round(avg * 10.0) / 10.0 : null);
        result.put("count", count);

        if (username != null) {
            userRepository.findByUsername(username).ifPresent(user ->
                ratingRepository.findByUserAndMovie(user, movie)
                    .ifPresent(r -> result.put("myRating", r.getStars()))
            );
        }
        return result;
    }

    public List<Object[]> getAverageRatingByGenre() {
        return ratingRepository.findAverageRatingByGenre();
    }
}
