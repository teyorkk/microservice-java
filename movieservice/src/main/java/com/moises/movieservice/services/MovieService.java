package com.moises.movieservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moises.movieservice.dto.MovieRequest;
import com.moises.movieservice.dto.MovieResponse;
import com.moises.movieservice.exceptions.ResourceNotFoundException;
import com.moises.movieservice.repository.MovieRepository;
import com.moises.movieservice.model.Movie;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository repo;

    public MovieResponse create(MovieRequest req) {
        Movie movie = Movie.builder()
                .title(req.getTitle())
                .director(req.getDirector())
                .releaseYear(req.getReleaseYear())
                .synopsis(req.getSynopsis())
                .build();

        Movie savedMovie = repo.save(movie);

        return mapToMovieResponse(savedMovie);
    }

    public List<MovieResponse> getAllMovie() {
        return repo.findAll().stream().map(this::mapToMovieResponse).toList();
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return mapToMovieResponse(movie);
    }

    public List<MovieResponse> getMovieByTitle(String title) {
        return repo.findByTitle(title).stream().map(this::mapToMovieResponse).toList();
    }

    public List<MovieResponse> getMovieByDirector(String director) {
        return repo.findAllByDirector(director).stream().map(this::mapToMovieResponse).toList();
    }

    public List<MovieResponse> getMovieByReleaseYear(Integer releaseYear) {
        return repo.findAllByReleaseYear(releaseYear).stream().map(this::mapToMovieResponse).toList();
    }

    public MovieResponse update(Long id, MovieRequest req) {
        Movie existingMovie = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        existingMovie.setTitle(req.getTitle());
        existingMovie.setDirector(req.getDirector());
        existingMovie.setReleaseYear(req.getReleaseYear());
        existingMovie.setSynopsis(req.getSynopsis());

        Movie updatedMovie = repo.save(existingMovie);
        return mapToMovieResponse(updatedMovie);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        repo.deleteById(id);
    }

    private MovieResponse mapToMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .title(movie.getTitle())
                .director(movie.getDirector())
                .releaseYear(movie.getReleaseYear())
                .synopsis(movie.getSynopsis())
                .id(movie.getId())
                .build();
    }
}
