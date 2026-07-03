package com.example.movies.controller;

import com.example.movies.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<?> rate(@PathVariable Long movieId,
                                   @RequestBody Map<String, Integer> body,
                                   Authentication auth) {
        return ResponseEntity.ok(ratingService.rateMovie(auth.getName(), movieId, body.get("stars")));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<?> getRating(@PathVariable Long movieId, Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(ratingService.getMovieRating(username, movieId));
    }
}
