package com.moises.watchservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moises.watchservice.model.Watches;

@Repository
public interface WatchesRepository extends JpaRepository<Watches, Long> {

    List<Watches> findByUserEmail(String userEmail);

    Optional<Watches> findByIdAndUserEmail(Long id, String userEmail);

}
