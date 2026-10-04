package com.example.movies.repository;

import com.example.movies.model.CinemaSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaSettingsRepository extends JpaRepository<CinemaSettings, Long> {
}
