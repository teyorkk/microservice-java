package com.moises.movieservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moises.movieservice.model.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

}
