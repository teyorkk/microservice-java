package com.moises.movieservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moises.movieservice.dto.MovieRequest;
import com.moises.movieservice.dto.MovieResponse;
import com.moises.movieservice.exceptions.ResourceNotFoundException;
import com.moises.movieservice.model.Movie;
import com.moises.movieservice.repository.MovieRepository;
import com.moises.movieservice.services.MovieService;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository repo;
    @InjectMocks
    private MovieService service;

    @Test
    public void create_shouldSaveMovieAndReturnResponse() {
        MovieRequest request = MovieRequest.builder()
                .title("Inception")
                .director("Christopher Nolan")
                .releaseYear(2010)
                .synopsis("Leonardo and others")
                .build();

        when(repo.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie savedMovie = invocation.getArgument(0);
            savedMovie.setId(1L);
            return savedMovie;
        });

        MovieResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Inception", response.getTitle());
        assertEquals(2010, response.getReleaseYear());

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(repo).save(movieCaptor.capture());
        Movie capturedMovie = movieCaptor.getValue();

        assertEquals("Inception", capturedMovie.getTitle());
        assertEquals("Christopher Nolan", capturedMovie.getDirector());
    }

    @Test
    public void update_shoudUpdateMovieAndReturnResponse() {
        Long movieId = 1L;
        MovieRequest request = MovieRequest.builder()
                .title("Inception")
                .director("Christopher Nolan")
                .releaseYear(2010)
                .synopsis("Leonardo and others")
                .build();
        Movie savedMovie = Movie.builder()
                .id(movieId)
                .title("Lebron")
                .director("Moises")
                .releaseYear(2011)
                .synopsis("poghi")
                .build();

        when(repo.findById(movieId)).thenReturn(Optional.of(savedMovie));
        when(repo.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieResponse response = service.update(movieId, request);
        assertNotNull(response);
        assertEquals("Inception", response.getTitle());
        assertEquals(2010, response.getReleaseYear());

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(repo).save(movieCaptor.capture());
        Movie capturedMovie = movieCaptor.getValue();

        assertEquals("Inception", capturedMovie.getTitle());
        assertEquals(2010, capturedMovie.getReleaseYear());
    }

    @Test
    public void delete_shouldCallDeleteById_whenIdExists() {
        Long movieId = 1L;
        when(repo.existsById(movieId)).thenReturn(true);
        service.delete(movieId);
        verify(repo, times(1)).deleteById(movieId);
    }

    @Test
    public void getMovieById_shouldReturnMovie_whenIdExists() {
        Movie mockMovie = Movie.builder()
                .id(100L)
                .title("The Matrix")
                .director("Wachowskis")
                .releaseYear(1999)
                .synopsis("Hacker learns the truth about reality.")
                .build();

        when(repo.findById(100L)).thenReturn(Optional.of(mockMovie));

        MovieResponse response = service.getMovieById(100L);

        assertNotNull(response);
        assertEquals("The Matrix", response.getTitle());
        verify(repo).findById(100L);
    }

    @Test
    public void getMovieById_shouldThrowException_whenIdDoesNotExist() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.getMovieById(999L);
        });

        assertEquals("Movie not found with id: 999", exception.getMessage());
        verify(repo).findById(999L);
    }
}
