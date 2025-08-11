CREATE TABLE booking (
     id SERIAL PRIMARY KEY,
     name VARCHAR(250) NOT NULL,
     email VARCHAR(150) NOT NULL,
     topic TEXT,
     notes TEXT,
     start_time TIMESTAMP NOT NULL,
     end_time TIMESTAMP NOT NULL,
     time_zone VARCHAR(100),
     status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
     confirmed BOOLEAN DEFAULT FALSE,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);