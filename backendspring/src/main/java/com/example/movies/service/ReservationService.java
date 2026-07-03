package com.example.movies.service;

import com.example.movies.model.*;
import com.example.movies.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public ReservationService(ReservationRepository reservationRepository,
                               UserRepository userRepository,
                               MovieRepository movieRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public Reservation create(Long userId, Long movieId, int numberOfTickets) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Film nije pronađen"));

        Reservation r = new Reservation();
        r.setUser(user);
        r.setMovie(movie);
        r.setNumberOfTickets(numberOfTickets);
        r.setReservationDate(LocalDateTime.now());
        return reservationRepository.save(r);
    }

    public List<Reservation> getMyReservations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        return reservationRepository.findByUser(user);
    }

    public void deleteReservation(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervacija nije pronađena"));

        // Korisnik može obrisati samo svoju rezervaciju
        if (!reservation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Nemate dozvolu za brisanje ove rezervacije");
        }

        reservationRepository.delete(reservation);
    }
}