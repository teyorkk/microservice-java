package com.moises.AuthService.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moises.AuthService.dto.AuthRequest;
import com.moises.AuthService.dto.AuthResponse;
import com.moises.AuthService.enums.Role;
import com.moises.AuthService.exceptions.DuplicateResourceException;
import com.moises.AuthService.exceptions.ResourceNotFoundException;
import com.moises.AuthService.model.User;
import com.moises.AuthService.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("User already exists");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = repo.save(user);
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .email(savedUser.getEmail())
                .token(token)
                .role(savedUser.getRole().toString())
                .build();
    }

    public AuthResponse login(AuthRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = repo.findByEmail(req.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .email(user.getEmail())
                .token(token)
                .role(user.getRole().toString())
                .build();
    }

    public List<User> getAllUser() {
        return repo.findAll();
    }
}
