package com.moises.watchservice.dto;

import com.moises.watchservice.model.WatchStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchRequest {

    @NotNull(message = "Movie Id should not be empty")
    private Long movieId;
    @NotNull(message = "Watch status should not be empty")
    private WatchStatus watchStatus;
}
