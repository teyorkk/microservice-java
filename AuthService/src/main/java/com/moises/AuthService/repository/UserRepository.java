package com.moises.AuthService.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moises.AuthService.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existByEmail(String email);
}
