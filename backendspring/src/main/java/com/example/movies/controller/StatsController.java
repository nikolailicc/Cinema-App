package com.example.movies.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.ReservationRepository;
import com.example.movies.service.RatingService;
import com.example.movies.service.WatchlistService;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final MovieRepository movieRepository;
    private final ReservationRepository reservationRepository;
    private final RatingService ratingService;
    private final WatchlistService watchlistService;

    public StatsController(MovieRepository movieRepository, ReservationRepository reservationRepository,
                            RatingService ratingService, WatchlistService watchlistService) {
        this.movieRepository = movieRepository;
        this.reservationRepository = reservationRepository;
        this.ratingService = ratingService;
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalMovies", movieRepository.count());
        stats.put("totalReservations", reservationRepository.count());

        List<Object[]> resByDay = reservationRepository.countReservationsByDay();
        List<Map<String, Object>> reservationsByDay = new ArrayList<>();
        for (Object[] row : resByDay) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", row[0].toString());
            entry.put("count", row[1]);
            reservationsByDay.add(entry);
        }
        stats.put("reservationsByDay", reservationsByDay);

        List<Object[]> ratingsByGenre = ratingService.getAverageRatingByGenre();
        List<Map<String, Object>> ratingsData = new ArrayList<>();
        for (Object[] row : ratingsByGenre) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("genre", row[0] != null ? row[0].toString() : "Nepoznato");
            entry.put("avgRating", row[1] != null ? Math.round(((Number) row[1]).doubleValue() * 10.0) / 10.0 : 0);
            ratingsData.add(entry);
        }
        stats.put("ratingsByGenre", ratingsData);

        stats.put("watchlistStats", watchlistService.getWatchlistStats());

        return ResponseEntity.ok(stats);
    }
}
