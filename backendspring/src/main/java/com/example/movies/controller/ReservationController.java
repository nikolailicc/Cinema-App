package com.example.movies.controller;

import com.example.movies.model.Reservation;
import com.example.movies.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation create(@RequestBody Map<String, Integer> body) {
        return reservationService.create(
                body.get("userId").longValue(),
                body.get("movieId").longValue(),
                body.get("numberOfTickets")
        );
    }

    @GetMapping("/my")
    public List<Reservation> getMine(Authentication auth) {
        return reservationService.getMyReservations(auth.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        reservationService.deleteReservation(id, auth.getName());
        return ResponseEntity.ok().build();
    }
}