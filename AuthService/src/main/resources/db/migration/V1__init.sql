-- Create a sequence for Hibernate's GenerationType.AUTO
CREATE SEQUENCE IF NOT EXISTS _users_seq START WITH 1 INCREMENT BY 50;

-- Create the users table
CREATE TABLE _users (
    id BIGINT NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT pk__users PRIMARY KEY (id)
);