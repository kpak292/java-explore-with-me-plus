CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL,
    confirmed_requests BIGINT,
    created_on TIMESTAMP NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    paid BOOLEAN,
    participant_limit INT,
    published_on TIMESTAMP,
    request_moderation BOOLEAN,
    state VARCHAR(100) NOT NULL,
    title VARCHAR(120) NOT NULL,
    views BIGINT,
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (location_id) REFERENCES locations (id)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    created_on TIMESTAMP NOT NULL,
    status VARCHAR(100) NOT NULL,
    FOREIGN KEY (requester_id) REFERENCES users (id),
    FOREIGN KEY (event_id) REFERENCES events (id)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN,
    title VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_compilations (
    event_id BIGINT,
    compilation_id BIGINT,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (compilation_id) REFERENCES compilations (id)
);