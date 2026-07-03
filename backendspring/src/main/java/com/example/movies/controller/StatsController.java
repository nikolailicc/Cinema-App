package com.example.movies.controller;

import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.ReservationRepository;
import com.example.movies.service.RatingService;
import com.example.movies.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

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

        // Ukupan broj filmova i rezervacija
        stats.put("totalMovies", movieRepository.count());
        stats.put("totalReservations", reservationRepository.count());

        // Rezervacije po danu (zadnjih 7 dana)
        List<Object[]> resByDay = reservationRepository.countReservationsByDay();
        List<Map<String, Object>> reservationsByDay = new ArrayList<>();
        for (Object[] row : resByDay) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", row[0].toString());
            entry.put("count", row[1]);
            reservationsByDay.add(entry);
        }
        stats.put("reservationsByDay", reservationsByDay);

        // Prosječna ocjena po žanru
        List<Object[]> ratingsByGenre = ratingService.getAverageRatingByGenre();
        List<Map<String, Object>> ratingsData = new ArrayList<>();
        for (Object[] row : ratingsByGenre) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("genre", row[0] != null ? row[0].toString() : "Nepoznato");
            entry.put("avgRating", row[1] != null ? Math.round(((Number) row[1]).doubleValue() * 10.0) / 10.0 : 0);
            ratingsData.add(entry);
        }
        stats.put("ratingsByGenre", ratingsData);

        // Watchlist statistika
        stats.put("watchlistStats", watchlistService.getWatchlistStats());

        return ResponseEntity.ok(stats);
    }
}
