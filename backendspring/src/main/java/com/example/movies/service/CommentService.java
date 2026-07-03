package com.example.movies.service;

import com.example.movies.model.*;
import com.example.movies.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, MovieRepository movieRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public Comment addComment(String username, Long movieId, String content) {
        if (content == null || content.isBlank()) throw new RuntimeException("Komentar ne može biti prazan");
        User user = userRepository.findByUsername(username).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setMovie(movie);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public List<Map<String, Object>> getCommentsForMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        List<Comment> comments = commentRepository.findByMovieOrderByCreatedAtDesc(movie);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : comments) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("username", c.getUser().getUsername());
            m.put("content", c.getContent());
            m.put("createdAt", c.getCreatedAt().toString());
            result.add(m);
        }
        return result;
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        // Može brisati autor ili admin
        if (!comment.getUser().getUsername().equals(username) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Nemate dozvolu");
        }
        commentRepository.delete(comment);
    }
}
