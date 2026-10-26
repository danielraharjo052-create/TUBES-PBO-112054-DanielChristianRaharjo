CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF', 'CUSTOMER'))
);

CREATE TABLE IF NOT EXISTS concerts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    city VARCHAR(60) NOT NULL,
    venue VARCHAR(120) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    CHECK (end_time > start_time)
);

CREATE TABLE IF NOT EXISTS tickets (
    id SERIAL PRIMARY KEY,
    concert_id INTEGER NOT NULL REFERENCES concerts(id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL CHECK (category IN ('PREMIUM', 'NORMAL')),
    price NUMERIC(14,2) NOT NULL CHECK (price >= 0),
    quota INTEGER NOT NULL CHECK (quota >= 0),
    UNIQUE (concert_id, category)
);

CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    ticket_id INTEGER NOT NULL REFERENCES tickets(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_price NUMERIC(14,2) NOT NULL CHECK (total_price >= 0),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('VA', 'EWALLET')),
    payment_reference VARCHAR(40),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUKSES')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password_hash, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'STAFF'),
('customer', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'CUSTOMER')
ON CONFLICT (username) DO NOTHING;

-- Password demo menggunakan SHA-256 untuk kebutuhan tugas: admin123, staff123, customer123.

INSERT INTO concerts (name, city, venue, start_time, end_time)
SELECT 'Konser Contoh', 'Bandung', 'Stadion Siliwangi', CURRENT_TIMESTAMP + INTERVAL '14 days', CURRENT_TIMESTAMP + INTERVAL '14 days 3 hours'
WHERE NOT EXISTS (SELECT 1 FROM concerts WHERE name = 'Konser Contoh');

INSERT INTO tickets (concert_id, category, price, quota)
SELECT c.id, v.category, v.price, v.quota
FROM concerts c
CROSS JOIN (VALUES ('PREMIUM', 350000::numeric, 100), ('NORMAL', 150000::numeric, 300)) AS v(category, price, quota)
WHERE c.name = 'Konser Contoh'
ON CONFLICT (concert_id, category) DO NOTHING;
