-- Database initialization script for Docker
-- This script runs when PostgreSQL container starts

-- Enable UUID extension (needed for some applications)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create database if it doesn't exist
-- Note: PostgreSQL creates the database automatically if it doesn't exist
-- when POSTGRES_DB is set in environment variables

-- Set default encoding
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- Grant necessary permissions
-- This is handled by PostgreSQL initialization scripts automatically

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'Database initialized successfully for reservation system';
END $$;
