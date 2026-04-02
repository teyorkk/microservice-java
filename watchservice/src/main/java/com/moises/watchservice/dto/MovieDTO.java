package com.moises.watchservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private Long id;
    private String title;
    private String director;
    private String synopsis;
    private Integer relaseYear;

}
