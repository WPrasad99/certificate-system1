-- Quick Test Account Creation
-- Run this in pgAdmin to create a test account instantly

-- Create test organizer account
-- Email: test@demo.com
-- Password: test123
INSERT INTO organizers (full_name, email, password, institute_name, created_at, updated_at)
VALUES (
    'Test Organizer',
    'test@demo.com',
    '$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG',
    'Demo College',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify account was created
SELECT * FROM organizers WHERE email = 'test@demo.com';
