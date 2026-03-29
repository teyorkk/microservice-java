package com.moises.movieservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Synopsis is required")
    private String synopsis;

    @NotBlank(message = "Director is required")
    private String director;

    @NotNull(message = "Release year is required")
    @Min(value = 1888, message = "Release year must be at least 1888")
    @Max(value = 2100, message = "Release year must be at most 2100")
    private Integer releaseYear;
}
