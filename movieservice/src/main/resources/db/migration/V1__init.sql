CREATE TABLE _movies(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    synopsis TEXT,
    director VARCHAR(255)NOT NULL,
    release_year INT NOT NULL
);