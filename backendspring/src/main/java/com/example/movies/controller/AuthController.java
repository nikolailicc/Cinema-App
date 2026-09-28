package com.example.movies.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movies.model.User;
import com.example.movies.repository.UserRepository;
import com.example.movies.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Korisničko ime i lozinka su obavezni.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Korisničko ime već postoji.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        userService.create(user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("message", "Registracija uspešna."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "role", u.getRole()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}