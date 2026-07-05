package com.example.movies.service;

import com.example.movies.model.Movie;
import com.example.movies.repository.CommentRepository;
import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.RatingRepository;
import com.example.movies.repository.ReservationRepository;
import com.example.movies.repository.WatchlistRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class MovieService {
	private final MovieRepository movieRepository;
	private final RatingRepository ratingRepository;
	private final CommentRepository commentRepository;
	private final ReservationRepository reservationRepository;
	private final WatchlistRepository watchlistRepository;
	private static final String UPLOAD_DIR = "uploads/movies/";

	public MovieService(MovieRepository movieRepository, RatingRepository ratingRepository,
			CommentRepository commentRepository, ReservationRepository reservationRepository,
			WatchlistRepository watchlistRepository) {
		this.movieRepository = movieRepository;
		this.ratingRepository = ratingRepository;
		this.commentRepository = commentRepository;
		this.reservationRepository = reservationRepository;
		this.watchlistRepository = watchlistRepository;
	}

	public List<Movie> getAll() {
		return movieRepository.findAll();
	}

	public Movie getById(Long id) {
		return movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Film nije pronađen: " + id));
	}

	public Movie create(Movie movie) {
		return movieRepository.save(movie);
	}

	public Movie update(Long id, Movie updated) {
		Movie existing = getById(id);
		existing.setTitle(updated.getTitle());
		existing.setDescription(updated.getDescription());
		existing.setDuration(updated.getDuration());
		existing.setGenre(updated.getGenre());
		existing.setScreeningDate(updated.getScreeningDate());
		return movieRepository.save(existing);
	}

	@Transactional
	public void delete(Long id) {
		Movie movie = getById(id);

		// Prvo obrisi sve zavisne zapise (foreign key constraint)
		ratingRepository.deleteByMovie(movie);
		commentRepository.deleteByMovie(movie);
		reservationRepository.deleteByMovie(movie);
		watchlistRepository.deleteByMovie(movie);

		if (movie.getImageUrl() != null) {
			try {
				Path imagePath = Paths.get(movie.getImageUrl().substring(1));
				Files.deleteIfExists(imagePath);
			} catch (IOException ignored) {
			}
		}
		movieRepository.deleteById(id);
	}

	public Movie saveImage(Long id, MultipartFile file) throws IOException {
		Movie movie = getById(id);

		// Napravi folder ako ne postoji
		Path uploadPath = Paths.get(UPLOAD_DIR);
		Files.createDirectories(uploadPath);

		// Obrisi staru sliku ako postoji
		if (movie.getImageUrl() != null) {
			try {
				Path oldImage = Paths.get(movie.getImageUrl().substring(1));
				Files.deleteIfExists(oldImage);
			} catch (IOException ignored) {
			}
		}

		// Sacuvaj novu sliku: movie_ID.ext
		String originalName = file.getOriginalFilename();
		String extension = "";
		if (originalName != null && originalName.contains(".")) {
			extension = originalName.substring(originalName.lastIndexOf("."));
		}
		String filename = "movie_" + id + extension;

		Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

		movie.setImageUrl("/uploads/movies/" + filename);
		return movieRepository.save(movie);
	}
}
