CREATE TABLE _watches(
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    movie_id BIGINT NOT NULL,
    watch_status VARCHAR(50) NOT NULL
)