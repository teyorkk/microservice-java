package com.moises.movieservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.moises.movieservice.model.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByReleaseYear(Integer releaseYear);

    List<Movie> findAllByDirector(String director);

    List<Movie> findByTitle(String title);
}
