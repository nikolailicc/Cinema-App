package com.example.movies.controller;

import com.example.movies.model.User;
import com.example.movies.service.UserService;
import com.example.movies.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // Javna registracija - uvijek kreira USER rolu
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Korisničko ime i lozinka su obavezni.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Korisničko ime već postoji.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER"); // registracija uvijek daje USER rolu
        userService.create(user);

        return ResponseEntity.ok("Registracija uspješna.");
    }

    // Vraća podatke trenutno ulogovanog korisnika
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