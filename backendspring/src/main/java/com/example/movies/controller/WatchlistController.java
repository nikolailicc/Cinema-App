package com.example.movies.controller;

import com.example.movies.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<?> getMyWatchlist(Authentication auth) {
        return ResponseEntity.ok(watchlistService.getWatchlist(auth.getName()));
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<?> addOrUpdate(@PathVariable Long movieId,
                                          @RequestBody Map<String, String> body,
                                          Authentication auth) {
        return ResponseEntity.ok(watchlistService.addOrUpdate(auth.getName(), movieId, body.get("status")));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> remove(@PathVariable Long movieId, Authentication auth) {
        watchlistService.remove(auth.getName(), movieId);
        return ResponseEntity.ok().build();
    }
}
