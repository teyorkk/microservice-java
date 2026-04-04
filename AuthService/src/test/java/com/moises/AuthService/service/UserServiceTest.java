package com.moises.AuthService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.moises.AuthService.dto.AuthRequest;
import com.moises.AuthService.enums.Role;
import com.moises.AuthService.exceptions.DuplicateResourceException;
import com.moises.AuthService.exceptions.ResourceNotFoundException;
import com.moises.AuthService.model.User;
import com.moises.AuthService.model.VerificationToken;
import com.moises.AuthService.repository.UserRepository;
import com.moises.AuthService.repository.VerificationTokenRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ThrowsWhenEmailAlreadyExists() {
        AuthRequest req = AuthRequest.builder()
                .email("taken@example.com")
                .password("password123")
                .build();

        when(repo.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(req));
    }

    @Test
    void verifyAccount_ThrowsWhenTokenMissing() {
        when(verificationTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.verifyAccount("missing-token"));
    }

    @Test
    void login_ThrowsWhenUserMissing() {
        AuthRequest req = AuthRequest.builder()
                .email("none@example.com")
                .password("password123")
                .build();

        when(repo.findByEmail("none@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.login(req));
    }

    @Test
    void getAllUser_ReturnsRepositoryUsers() {
        User user = User.builder()
                .email("user@example.com")
                .password("encoded")
                .role(Role.USER)
                .isVerified(true)
                .build();

        when(repo.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUser();

        assertEquals(1, result.size());
        assertEquals("user@example.com", result.getFirst().getEmail());
        verify(repo).findAll();
    }
}
