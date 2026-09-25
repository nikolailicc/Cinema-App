package com.example.movies.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movies.service.RatingService;

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
        Integer stars = body == null ? null : body.get("stars");
        if (stars == null || stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().body("Ocena mora biti između 1 i 5");
        }
        return ResponseEntity.ok(ratingService.rateMovie(auth.getName(), movieId, stars));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<?> getRating(@PathVariable Long movieId, Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(ratingService.getMovieRating(username, movieId));
    }
}
