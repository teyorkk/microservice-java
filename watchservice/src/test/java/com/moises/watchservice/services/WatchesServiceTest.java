package com.moises.watchservice.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.moises.watchservice.dto.WatchRequest;
import com.moises.watchservice.exceptions.ResourceNotFoundException;
import com.moises.watchservice.model.WatchStatus;
import com.moises.watchservice.model.Watches;
import com.moises.watchservice.repository.WatchesRepository;

@ExtendWith(MockitoExtension.class)
class WatchesServiceTest {

    @Mock
    private WatchesRepository repo;

    @InjectMocks
    private WatchesService watchesService;

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "password"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteMyWatch_DeletesWhenWatchExistsForUser() {
        Watches watch = Watches.builder()
                .id(1L)
                .userEmail("user@example.com")
                .movieId(11L)
                .WatchStatus(WatchStatus.WATCHING)
                .build();

        when(repo.findByIdAndUserEmail(1L, "user@example.com")).thenReturn(Optional.of(watch));

        assertDoesNotThrow(() -> watchesService.deleteMyWatch(1L));
        verify(repo).delete(watch);
    }

    @Test
    void deleteMyWatch_ThrowsWhenWatchMissing() {
        when(repo.findByIdAndUserEmail(9L, "user@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> watchesService.deleteMyWatch(9L));
    }

    @Test
    void getMyWatchById_ThrowsWhenWatchMissing() {
        when(repo.findByIdAndUserEmail(3L, "user@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> watchesService.getMyWatchById(3L, "Bearer token"));
    }

    @Test
    void updateMyWatch_ThrowsWhenWatchMissing() {
        WatchRequest req = WatchRequest.builder()
                .movieId(22L)
                .watchStatus(WatchStatus.COMPLETED)
                .build();

        when(repo.findByIdAndUserEmail(7L, "user@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> watchesService.updateMyWatch(7L, req, "Bearer token"));
    }
}
