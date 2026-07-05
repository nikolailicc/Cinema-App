package com.example.movies.repository;

import com.example.movies.model.Movie;
import com.example.movies.model.Reservation;
import com.example.movies.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);

    @Query("SELECT CAST(r.reservationDate AS date), COUNT(r) FROM Reservation r GROUP BY CAST(r.reservationDate AS date) ORDER BY CAST(r.reservationDate AS date) DESC")
    List<Object[]> countReservationsByDay();
    
    void deleteByMovie(Movie movie);
}