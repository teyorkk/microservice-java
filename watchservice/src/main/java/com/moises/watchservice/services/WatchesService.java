package com.moises.watchservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.moises.watchservice.dto.MovieDTO;
import com.moises.watchservice.dto.WatchRequest;
import com.moises.watchservice.dto.WatchResponse;
import com.moises.watchservice.exceptions.ResourceNotFoundException;
import com.moises.watchservice.model.Watches;
import com.moises.watchservice.repository.WatchesRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchesService {

    private final WatchesRepository repo;
    private RestClient restClient;

    @Value("${application.movie-service.url}")
    private String movieServiceUrl;

    @PostConstruct
    public void init() {
        restClient = RestClient.builder()
                .baseUrl(movieServiceUrl)
                .build();
    }

    public WatchResponse addMovie(WatchRequest req, String authHeader) {
        String email = getCurrentUserEmail();
        MovieDTO movieDTO = fetchMovie(req.getMovieId(), authHeader);

        Watches saved = repo.save(Watches.builder()
                .userEmail(email)
                .movieId(movieDTO.getId())
                .WatchStatus(req.getWatchStatus())
                .build());

        return mapToWatchResponse(saved, movieDTO.getTitle());

    }

    public List<WatchResponse> getMyWatches(String authHeader) {
        return repo.findByUserEmail(getCurrentUserEmail()).stream()
                .map(watch -> mapToWatchResponse(watch, fetchMovieTitle(watch.getMovieId(), authHeader)))
                .toList();
    }

    public WatchResponse getMyWatchById(Long id, String authHeader) {
        Watches watch = repo.findByIdAndUserEmail(id, getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found"));
        return mapToWatchResponse(watch, fetchMovieTitle(watch.getMovieId(), authHeader));
    }

    public WatchResponse updateMyWatch(Long id, WatchRequest req, String authHeader) {
        Watches watch = repo.findByIdAndUserEmail(id, getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found"));

        MovieDTO movieDTO = fetchMovie(req.getMovieId(), authHeader);
        watch.setMovieId(movieDTO.getId());
        watch.setWatchStatus(req.getWatchStatus());

        Watches saved = repo.save(watch);
        return mapToWatchResponse(saved, movieDTO.getTitle());
    }

    public void deleteMyWatch(Long id) {
        Watches watch = repo.findByIdAndUserEmail(id, getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found"));
        repo.delete(watch);
    }

    private MovieDTO fetchMovie(Long movieId, String authHeader) {
        try {
            if (!StringUtils.hasText(authHeader)) {
                throw new IllegalArgumentException("Authorization header is required");
            }
            return restClient.get()
                    .uri("/api/movies/{id}", movieId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException("Movie not found");
                    }).body(MovieDTO.class);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to verify movie", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify movie", e);
        }
    }

    private String fetchMovieTitle(Long movieId, String authHeader) {
        return fetchMovie(movieId, authHeader).getTitle();
    }

    private String getCurrentUserEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private WatchResponse mapToWatchResponse(Watches watches, String title) {
        return WatchResponse.builder()
                .id(watches.getId())
                .movieId(watches.getMovieId())
                .movieTitle(title)
                .userEmail(watches.getUserEmail())
                .watchStatus(watches.getWatchStatus())
                .build();
    }

}
