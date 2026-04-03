package com.moises.AuthService.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
import com.moises.AuthService.model.VerificationToken;
import com.moises.AuthService.repository.UserRepository;
import com.moises.AuthService.repository.VerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    public String register(AuthRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("User already exists");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .isVerified(false)
                .build();

        User savedUser = repo.save(user);
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return "Registration successful please check your email to verifiy your account";

    }

    public String verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
        User user = verificationToken.getUser();
        user.setVerified(true);
        repo.save(user);

        verificationTokenRepository.delete(verificationToken);
        return "Verification Successful you can now login";
    }

    public AuthResponse login(AuthRequest req) {

        User user = repo.findByEmail(req.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your account first");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

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
