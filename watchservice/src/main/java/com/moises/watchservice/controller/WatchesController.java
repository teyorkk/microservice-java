package com.moises.watchservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moises.watchservice.dto.WatchRequest;
import com.moises.watchservice.dto.WatchResponse;
import com.moises.watchservice.services.WatchesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/watches")
public class WatchesController {
    private final WatchesService service;

    @PostMapping
    public ResponseEntity<WatchResponse> addMovie(@Valid @RequestBody WatchRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMovie(request, authHeader));
    }

    @GetMapping
    public ResponseEntity<List<WatchResponse>> getMyWatches(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.getMyWatches(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchResponse> getMyWatchById(@PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.getMyWatchById(id, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WatchResponse> updateMyWatch(@PathVariable Long id, @Valid @RequestBody WatchRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.updateMyWatch(id, request, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyWatch(@PathVariable Long id) {
        service.deleteMyWatch(id);
        return ResponseEntity.noContent().build();
    }
}
