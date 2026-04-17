CREATE TABLE students
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    phone      VARCHAR(50),
    birth_date DATE,
    active     BOOLEAN      NOT NULL DEFAULT TRUE
);
