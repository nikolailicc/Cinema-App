package com.example.movies.controller;

import com.example.movies.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<?> getComments(@PathVariable Long movieId) {
        return ResponseEntity.ok(commentService.getCommentsForMovie(movieId));
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<?> addComment(@PathVariable Long movieId,
                                         @RequestBody Map<String, String> body,
                                         Authentication auth) {
        return ResponseEntity.ok(commentService.addComment(auth.getName(), movieId, body.get("content")));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication auth) {
        commentService.deleteComment(commentId, auth.getName());
        return ResponseEntity.ok().build();
    }
}
