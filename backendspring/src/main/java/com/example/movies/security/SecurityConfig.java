package com.example.movies.security;

import com.example.movies.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.movies.model.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronadjen: " + username));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        };
    }

    // ── Jedan lanac za cijeli REST API ────────────────────────
    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> {})
            .authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI dokumentacija — javno dostupna
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Statičke uploadovane slike su javno dostupne
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/movies/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/movies").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/movies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/movies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/movies/**").hasRole("ADMIN")
                .requestMatchers("/reservations/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                // ── Watchlist, Ratings, Comments ──────────────────────────────
                .requestMatchers("/watchlist/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/ratings/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/comments/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/reports/**").hasRole("ADMIN")
                .requestMatchers("/stats/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // ── CORS konfiguracija za odvojeni frontend (SPA) ─────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Prilagodi listu origina adresi na kojoj se hostuje frontend aplikacija
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
