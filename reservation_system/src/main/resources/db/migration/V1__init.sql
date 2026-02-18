-- V1__init.sql
-- Initial schema for reservation system
-- PostgreSQL database schema

-- Enable UUID extension if needed (optional)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL UNIQUE ,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    user_role VARCHAR(50) NOT NULL DEFAULT 'GUEST',
    CONSTRAINT chk_user_role CHECK (user_role IN ('GUEST', 'STAFF', 'ADMIN'))
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- ============================================
-- CONFIRMATION TOKENS TABLE
-- ============================================
CREATE TABLE confirmation_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_confirmation_tokens_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_confirmation_tokens_token ON confirmation_tokens(token);
CREATE INDEX idx_confirmation_tokens_user_id ON confirmation_tokens(user_id);

-- ============================================
-- ROOMS TABLE
-- ============================================
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    capacity INTEGER NOT NULL,
    base_price INTEGER NOT NULL,
    price_per_night DECIMAL(10, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_room_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE', 'CLEANING', 'BLOCKED')),
    CONSTRAINT chk_capacity_positive CHECK (capacity > 0),
    CONSTRAINT chk_base_price_positive CHECK (base_price >= 0)
);

CREATE INDEX idx_rooms_type ON rooms(type);
CREATE INDEX idx_rooms_status ON rooms(status);

-- ============================================
-- RATES TABLE
-- ============================================
CREATE TABLE rates (
    id BIGSERIAL PRIMARY KEY,
    room_type VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price INTEGER NOT NULL,
    CONSTRAINT chk_rates_date_range CHECK (end_date > start_date),
    CONSTRAINT chk_rates_price_positive CHECK (price >= 0)
);

CREATE INDEX idx_rates_room_type ON rates(room_type);
CREATE INDEX idx_rates_dates ON rates(start_date, end_date);

-- ============================================
-- BOOKINGS TABLE
-- ============================================
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL DEFAULT 'RESERVED',
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_intent_id BIGINT,
    invoice_no VARCHAR(100),
    CONSTRAINT chk_booking_status CHECK (status IN ('RESERVED', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'CHECKED_OUT', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in),
    CONSTRAINT chk_total_amount_positive CHECK (total_amount >= 0),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) 
        REFERENCES rooms(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_room_id ON bookings(room_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_dates ON bookings(check_in, check_out);
CREATE INDEX idx_bookings_created_at ON bookings(created_at);

-- ============================================
-- BOOKING GUESTS TABLE
-- ============================================
CREATE TABLE booking_guests (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    booking_id BIGINT NOT NULL,
    CONSTRAINT fk_booking_guests_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE INDEX idx_booking_guests_booking_id ON booking_guests(booking_id);

-- ============================================
-- BOOKING NIGHTS TABLE
-- ============================================
CREATE TABLE booking_nights (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    night_date DATE NOT NULL,
    price INTEGER NOT NULL,
    CONSTRAINT chk_booking_nights_price_positive CHECK (price >= 0),
    CONSTRAINT fk_booking_nights_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_nights_room FOREIGN KEY (room_id) 
        REFERENCES rooms(id) ON DELETE RESTRICT,
    CONSTRAINT uk_booking_nights_unique UNIQUE (booking_id, room_id, night_date)
);

CREATE INDEX idx_booking_nights_booking_id ON booking_nights(booking_id);
CREATE INDEX idx_booking_nights_room_id ON booking_nights(room_id);
CREATE INDEX idx_booking_nights_night_date ON booking_nights(night_date);

-- ============================================
-- BOOKING ROOMS TABLE
-- ============================================
CREATE TABLE booking_rooms (
    id BIGSERIAL PRIMARY KEY ,
    booking_id BIGINT NOT NULL ,
    room_id BIGINT NOT NULL ,
    adults INTEGER NOT NULL DEFAULT 1,
    children INTEGER NOT NULL DEFAULT 0 ,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,

    CONSTRAINT chk_booking_rooms_adults CHECK (adults > 0) ,
    CONSTRAINT chk_booking_rooms_children CHECK (children >= 0) ,

    CONSTRAINT fk_booking_rooms_booking FOREIGN KEY (booking_id)
        REFERENCES bookings(id) ON DELETE CASCADE ,

    CONSTRAINT fk_booking_rooms_room FOREIGN KEY (room_id)
        REFERENCES rooms(id) ON DELETE RESTRICT ,

    CONSTRAINT uk_booking_rooms_unique UNIQUE (booking_id , room_id)
);

CREATE INDEX idx_booking_rooms_booking_id ON booking_rooms(booking_id);
CREATE INDEX idx_booking_rooms_room_id ON booking_rooms(room_id);


-- ============================================
-- ROOM INVENTORY TABLE
-- ============================================
CREATE TABLE room_inventory (
    id BIGSERIAL PRIMARY KEY,
    night_date DATE NOT NULL,
    booked_count INTEGER NOT NULL DEFAULT 0,
    allotment INTEGER NOT NULL DEFAULT 1,
    room_id BIGINT NOT NULL,
    CONSTRAINT chk_room_inventory_booked_count CHECK (booked_count >= 0),
    CONSTRAINT chk_room_inventory_allotment CHECK (allotment > 0),
    CONSTRAINT chk_room_inventory_availability CHECK (booked_count <= allotment),
    CONSTRAINT fk_room_inventory_room FOREIGN KEY (room_id) 
        REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT uk_room_inventory_unique UNIQUE (room_id, night_date)
);

CREATE INDEX idx_room_inventory_room_id ON room_inventory(room_id);
CREATE INDEX idx_room_inventory_night_date ON room_inventory(night_date);
CREATE INDEX idx_room_inventory_dates ON room_inventory(room_id, night_date);

-- ============================================
-- INVOICES TABLE
-- ============================================
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    pdf_path VARCHAR(500),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) ON DELETE RESTRICT
);

CREATE INDEX idx_invoices_booking_id ON invoices(booking_id);

-- ============================================
-- PAYMENTS TABLE
-- ============================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(100) NOT NULL,
    provider_ref VARCHAR(255),
    amount INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PROCESSING',
    booking_id BIGINT NOT NULL,
    CONSTRAINT chk_payment_status CHECK (status IN ('PROCESSING', 'COMPLETED', 'REJECTED')),
    CONSTRAINT chk_payment_amount_positive CHECK (amount >= 0),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) ON DELETE RESTRICT
);

CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_provider_ref ON payments(provider_ref);

-- ============================================
-- TRIGGERS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update updated_at on bookings table
CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SEED ROOMS
-- ============================================
INSERT INTO rooms (number, name, type, capacity, base_price, price_per_night, status)
VALUES
('101', 'Standard Room 101', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),
('102', 'Standard Room 102', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),
('201', 'Deluxe Room 201', 'DELUXE', 3, 120, 120.00, 'AVAILABLE'),
('202', 'Deluxe Room 202', 'DELUXE', 3, 120, 120.00, 'AVAILABLE'),
('301', 'Suite 301', 'SUITE', 4, 200, 200.00, 'AVAILABLE')
ON CONFLICT (number) DO NOTHING;

-- ============================================
-- SEED ROOM INVENTORY (365 DAYS)
-- ============================================
INSERT INTO room_inventory (room_id, night_date, booked_count, allotment)
SELECT
    r.id,
    gs.night_date,
    0 AS booked_count,
    1 AS allotment
FROM rooms r
         CROSS JOIN generate_series(
                CURRENT_DATE,
                CURRENT_DATE + INTERVAL '364 days',
                INTERVAL '1 day'
                    ) AS gs(night_date)
    ON CONFLICT (room_id, night_date) DO NOTHING;





