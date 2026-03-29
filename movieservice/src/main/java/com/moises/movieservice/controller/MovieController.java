package com.moises.movieservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moises.movieservice.dto.MovieRequest;
import com.moises.movieservice.dto.MovieResponse;
import com.moises.movieservice.services.MovieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovie() {
        return ResponseEntity.ok(movieService.getAllMovie());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<MovieResponse>> getMovieByTitle(@RequestParam String title) {
        return ResponseEntity.ok(movieService.getMovieByTitle(title));
    }

    @GetMapping("/search/director")
    public ResponseEntity<List<MovieResponse>> getMovieByDirector(@RequestParam String director) {
        return ResponseEntity.ok(movieService.getMovieByDirector(director));
    }

    @GetMapping("/search/release-year")
    public ResponseEntity<List<MovieResponse>> getMovieByReleaseYear(@RequestParam Integer releaseYear) {
        return ResponseEntity.ok(movieService.getMovieByReleaseYear(releaseYear));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> update(@PathVariable Long id, @Valid @RequestBody MovieRequest req) {
        return ResponseEntity.ok(movieService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
