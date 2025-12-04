CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP WITH TIME ZONE
);

CREATE TABLE genres
(
    id BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE movies
(
    id BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    duration_minutes INTEGER      NOT NULL,
    release_date     DATE,
    rating           DECIMAL(3, 1),
    poster_url       VARCHAR(500),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 10)),
    CONSTRAINT chk_duration CHECK (duration_minutes > 0)
);

CREATE TABLE movie_genres
(
    movie_id BIGINT NOT NULL REFERENCES movies (id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres (id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE halls
(
    id BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    hall_type VARCHAR(20)  NOT NULL,
    capacity  INTEGER      NOT NULL,
    CONSTRAINT chk_capacity CHECK (capacity > 0)
);

CREATE TABLE seats
(
    id BIGSERIAL PRIMARY KEY,
    hall_id          BIGINT        NOT NULL REFERENCES halls (id) ON DELETE CASCADE,
    row_number       INTEGER       NOT NULL,
    seat_number      INTEGER       NOT NULL,
    seat_type        VARCHAR(20)   NOT NULL DEFAULT 'STANDARD',
    price_multiplier DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    CONSTRAINT chk_row_number CHECK (row_number > 0),
    CONSTRAINT chk_seat_number CHECK (seat_number > 0),
    CONSTRAINT chk_price_multiplier CHECK (price_multiplier > 0),
    UNIQUE (hall_id, row_number, seat_number)
);

CREATE TABLE sessions
(
    id BIGSERIAL PRIMARY KEY,
    movie_id   BIGINT         NOT NULL REFERENCES movies (id),
    hall_id    BIGINT         NOT NULL REFERENCES halls (id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_session_time CHECK (end_time > start_time),
    CONSTRAINT chk_base_price CHECK (base_price >= 0)
);

CREATE TABLE bookings
(
    id BIGSERIAL PRIMARY KEY,
    user_id     BIGINT         NOT NULL REFERENCES users (id),
    session_id  BIGINT         NOT NULL REFERENCES sessions (id),
    total_price DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT chk_total_price CHECK (total_price >= 0)
);

CREATE TABLE booking_seats
(
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT         NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    seat_id    BIGINT         NOT NULL REFERENCES seats (id),
    price      DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_seat_price CHECK (price >= 0)
);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_movies_updated_at
    BEFORE UPDATE
    ON movies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE
    ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
