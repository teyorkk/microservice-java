package com.moises.watchservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moises.watchservice.model.Watches;

@Repository
public interface WatchesRepository extends JpaRepository<Watches, Long> {

}
