-- V2__seed_data.sql
-- Seed data: Sample rooms + 365 days inventory
-- PostgreSQL seed data script

-- ============================================
-- INSERT SAMPLE ROOMS
-- ============================================
INSERT INTO rooms (number, name, type, capacity, base_price, price_per_night, status) VALUES
-- Standard Rooms
('101', 'Standard Room 101', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),
('102', 'Standard Room 102', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),
('103', 'Standard Room 103', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),
('104', 'Standard Room 104', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),
('105', 'Standard Room 105', 'STANDARD', 2, 80, 80.00, 'AVAILABLE'),

-- Deluxe Rooms
('201', 'Deluxe Room 201', 'DELUXE', 3, 120, 120.00, 'AVAILABLE'),
('202', 'Deluxe Room 202', 'DELUXE', 3, 120, 120.00, 'AVAILABLE'),
('203', 'Deluxe Room 203', 'DELUXE', 3, 120, 120.00, 'AVAILABLE'),
('204', 'Deluxe Room 204', 'DELUXE', 3, 120, 120.00, 'AVAILABLE'),

-- Suites
('301', 'Executive Suite 301', 'SUITE', 4, 200, 200.00, 'AVAILABLE'),
('302', 'Executive Suite 302', 'SUITE', 4, 200, 200.00, 'AVAILABLE'),
('303', 'Presidential Suite 303', 'SUITE', 6, 350, 350.00, 'AVAILABLE'),

-- Family Rooms
('401', 'Family Room 401', 'FAMILY', 5, 150, 150.00, 'AVAILABLE'),
('402', 'Family Room 402', 'FAMILY', 5, 150, 150.00, 'AVAILABLE'),

-- Economy Rooms
('501', 'Economy Room 501', 'ECONOMY', 2, 60, 60.00, 'AVAILABLE'),
('502', 'Economy Room 502', 'ECONOMY', 2, 60, 60.00, 'AVAILABLE'),
('503', 'Economy Room 503', 'ECONOMY', 2, 60, 60.00, 'AVAILABLE');

-- ============================================
-- GENERATE 365 DAYS OF INVENTORY FOR EACH ROOM
-- ============================================
-- This generates inventory records for the next 365 days starting from today
-- Each room gets an allotment of 1 (single room booking) and booked_count starts at 0

INSERT INTO room_inventory (room_id, night_date, booked_count, allotment)
SELECT 
    r.id AS room_id,
    (CURRENT_DATE + d.day_offset) AS night_date,
    0 AS booked_count,
    1 AS allotment
FROM rooms r
CROSS JOIN generate_series(0, 364) AS d(day_offset)
ON CONFLICT (room_id, night_date) DO NOTHING;

