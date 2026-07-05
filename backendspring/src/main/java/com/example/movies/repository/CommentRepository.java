package com.example.movies.repository;

import com.example.movies.model.Comment;
import com.example.movies.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMovieOrderByCreatedAtDesc(Movie movie);
    void deleteByMovie(Movie movie);
}