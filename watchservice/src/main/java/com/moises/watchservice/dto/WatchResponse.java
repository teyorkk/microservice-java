package com.moises.watchservice.dto;

import com.moises.watchservice.model.WatchStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchResponse {
    private Long id;
    private String userEmail;
    private Long movieId;
    private String movieTitle;
    private WatchStatus watchStatus;
}
