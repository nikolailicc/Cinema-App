package com.example.movies.controller;

import com.example.movies.model.Movie;
import com.example.movies.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getAll() {
        return movieService.getAll();
    }

    @GetMapping("/{id}")
    public Movie getById(@PathVariable Long id) {
        return movieService.getById(id);
    }

    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        return movieService.create(movie);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable Long id, @RequestBody Movie movie) {
        return movieService.update(id, movie);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Upload slike za film
    @PostMapping("/{id}/image")
    public Movie uploadImage(@PathVariable Long id,
                             @RequestParam("file") MultipartFile file) throws IOException {
        return movieService.saveImage(id, file);
    }
}
