ALTER TABLE _users
ADD COLUMN is_verified BOOLEAN DEFAULT false;

CREATE TABLE _verification_tokens(
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_verificationTokens_user FOREIGN KEY (user_id)
    REFERENCES _users(id)
    ON DELETE CASCADE
)