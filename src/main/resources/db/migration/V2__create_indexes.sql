CREATE INDEX idx_sessions_movie_time ON sessions (movie_id, start_time);

CREATE INDEX idx_sessions_hall_time ON sessions (hall_id, start_time, end_time);

CREATE INDEX idx_bookings_user_status ON bookings (user_id, status);

CREATE INDEX idx_bookings_session ON bookings (session_id);

CREATE INDEX idx_booking_seats_booking ON booking_seats (booking_id);

CREATE INDEX idx_booking_seats_seat ON booking_seats (seat_id);

CREATE INDEX idx_users_active ON users (id) WHERE deleted_at IS NULL;

CREATE INDEX idx_movies_active ON movies (id) WHERE deleted_at IS NULL;

CREATE INDEX idx_movies_title_fts ON movies USING gin(to_tsvector('simple', title));

CREATE INDEX idx_seats_hall ON seats (hall_id);

CREATE INDEX idx_movie_genres_movie ON movie_genres (movie_id);
CREATE INDEX idx_movie_genres_genre ON movie_genres (genre_id);
