package com.example.movies.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private int duration; // u minutima

    private String genre;

    private LocalDate screeningDate; // datum prikazivanja

    @Column(length = 500)
    private String imageUrl; // putanja do slike filma

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public LocalDate getScreeningDate() { return screeningDate; }
    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
