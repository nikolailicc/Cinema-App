package com.example.movies.controller;

import com.example.movies.model.CinemaSettings;
import com.example.movies.repository.CinemaSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cinema")
public class CinemaSettingsController {

    private static final long SETTINGS_ID = 1L;
    private final CinemaSettingsRepository repository;

    public CinemaSettingsController(CinemaSettingsRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/settings")
    public CinemaSettings get() {
        return repository.findById(SETTINGS_ID).orElseGet(this::defaults);
    }

    @PutMapping("/settings")
    public ResponseEntity<CinemaSettings> update(@RequestBody CinemaSettings settings,
                                                  Authentication authentication) {
        CinemaSettings current = repository.findById(SETTINGS_ID).orElseGet(this::defaults);
        if (settings.getName() == null || settings.getName().isBlank()
                || settings.getAddress() == null || settings.getAddress().isBlank()
                || settings.getLatitude() < -90 || settings.getLatitude() > 90
                || settings.getLongitude() < -180 || settings.getLongitude() > 180) {
            return ResponseEntity.badRequest().build();
        }
        current.setId(SETTINGS_ID);
        current.setName(settings.getName().trim());
        current.setAddress(settings.getAddress().trim());
        current.setLatitude(settings.getLatitude());
        current.setLongitude(settings.getLongitude());
        return ResponseEntity.ok(repository.save(current));
    }

    private CinemaSettings defaults() {
        CinemaSettings settings = new CinemaSettings();
        settings.setId(SETTINGS_ID);
        settings.setName("Bioskop \"Kolosej\" Beograd");
        settings.setAddress("Karađorđeva 3, Beograd");
        settings.setLatitude(44.8206);
        settings.setLongitude(20.4587);
        return settings;
    }
}
