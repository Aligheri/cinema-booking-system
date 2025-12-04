INSERT INTO genres (name, description)
VALUES ('Action', 'Action-packed movies with exciting sequences'),
       ('Comedy', 'Humorous movies intended to make audiences laugh'),
       ('Drama', 'Serious, plot-driven presentations'),
       ('Horror', 'Movies intended to scare and thrill'),
       ('Sci-Fi', 'Science fiction exploring futuristic concepts'),
       ('Romance', 'Movies focusing on romantic relationships'),
       ('Thriller', 'Suspenseful movies designed to keep viewers on edge'),
       ('Animation', 'Animated feature films'),
       ('Documentary', 'Non-fiction films presenting factual information'),
       ('Fantasy', 'Movies featuring magical and supernatural elements');

INSERT INTO halls (name, hall_type, capacity)
VALUES ('Hall 1 - Standard', 'STANDARD_2D', 120),
       ('Hall 2 - Premium 3D', 'STANDARD_3D', 80),
       ('Hall 3 - IMAX', 'IMAX', 200),
       ('Hall 4 - VIP Lounge', 'VIP', 40);

INSERT INTO seats (hall_id, row_number, seat_number, seat_type, price_multiplier)
SELECT 1,
       row_num,
       seat_num,
       CASE
           WHEN row_num >= 8 THEN 'VIP'
           WHEN seat_num = 1 OR seat_num = 12 THEN 'WHEELCHAIR'
           ELSE 'STANDARD'
           END,
       CASE
           WHEN row_num >= 8 THEN 1.50
           WHEN seat_num = 1 OR seat_num = 12 THEN 0.80
           ELSE 1.00
           END
FROM generate_series(1, 10) AS row_num,
     generate_series(1, 12) AS seat_num;

INSERT INTO seats (hall_id, row_number, seat_number, seat_type, price_multiplier)
SELECT 2,
       row_num,
       seat_num,
       CASE
           WHEN row_num >= 6 THEN 'VIP'
           ELSE 'STANDARD'
           END,
       CASE
           WHEN row_num >= 6 THEN 1.50
           ELSE 1.00
           END
FROM generate_series(1, 8) AS row_num,
     generate_series(1, 10) AS seat_num;

INSERT INTO seats (hall_id, row_number, seat_number, seat_type, price_multiplier)
SELECT 3,
       row_num,
       seat_num,
       CASE
           WHEN row_num BETWEEN 7 AND 10 THEN 'VIP'
           WHEN row_num <= 3 THEN 'STANDARD'
           ELSE 'STANDARD'
           END,
       CASE
           WHEN row_num BETWEEN 7 AND 10 THEN 1.75
           WHEN row_num <= 3 THEN 0.90
           ELSE 1.00
           END
FROM generate_series(1, 15) AS row_num,
     generate_series(1, 14) AS seat_num;

INSERT INTO seats (hall_id, row_number, seat_number, seat_type, price_multiplier)
SELECT 4,
       row_num,
       seat_num,
       CASE
           WHEN seat_num IN (3, 4, 5, 6) THEN 'LOVESEAT'
           ELSE 'VIP'
           END,
       CASE
           WHEN seat_num IN (3, 4, 5, 6) THEN 2.00
           ELSE 1.50
           END
FROM generate_series(1, 5) AS row_num,
     generate_series(1, 8) AS seat_num;

INSERT INTO users (email, password_hash, first_name, last_name, role)
VALUES ('admin@cinema.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Admin', 'User', 'ADMIN');

INSERT INTO movies (title, description, duration_minutes, release_date, rating, poster_url)
VALUES ('The Matrix Resurrections',
        'Return to a world of two realities: one, everyday life; the other, what lies behind it.', 148, '2021-12-22',
        7.5, '/posters/matrix.jpg'),
       ('Dune: Part Two',
        'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.',
        166, '2024-03-01', 8.8, '/posters/dune2.jpg'),
       ('Oppenheimer',
        'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.',
        180, '2023-07-21', 8.9, '/posters/oppenheimer.jpg'),
       ('Barbie',
        'Barbie and Ken are having the time of their lives in the colorful and seemingly perfect world of Barbie Land.',
        114, '2023-07-21', 7.3, '/posters/barbie.jpg'),
       ('Spider-Man: Across the Spider-Verse',
        'Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People.', 140,
        '2023-06-02', 8.7, '/posters/spiderman.jpg');

INSERT INTO movie_genres (movie_id, genre_id)
VALUES
    (1, 1),
    (1, 5),
    (2, 1),
    (2, 5),
    (2, 3),
    (3, 3),
    (3, 7),
    (4, 2),
    (4, 10),
    (5, 1),
    (5, 8),
    (5, 5);
