package com.example.cinebook.model;

import java.io.Serializable;

/**
 * Odgovara "Movie" semi iz backend Swagger-a:
 * id, title, description, duration, genre, screeningDate (yyyy-MM-dd), imageUrl
 */
public class Movie implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private String genre;
    private String screeningDate;
    private String imageUrl;

    public Movie() {
    }

    public Movie(String title, String description, Integer duration, String genre, String screeningDate) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.genre = genre;
        this.screeningDate = screeningDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getScreeningDate() {
        return screeningDate;
    }

    public void setScreeningDate(String screeningDate) {
        this.screeningDate = screeningDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
