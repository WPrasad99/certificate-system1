# 🔑 Login & Test Credentials Guide

## ⚠️ Important: No Pre-existing Accounts

The system starts with **ZERO accounts**. You must register first!

---

## Option 1: Register New Account (Recommended)

### Steps:
1. Go to http://localhost:5173
2. Click **"Register here"** link
3. Fill in the form:

**Use these test credentials:**
```
Full Name: Test Organizer
Email: test@demo.com
Password: test123
Institute: Demo College
```

4. Click **"Register"**
5. You'll be automatically logged in!

---

## Option 2: Quick Database Insert (For Testing)

If you want a pre-existing account, run this SQL:

### Using pgAdmin:
1. Open pgAdmin
2. Connect to `certificate_system` database
3. Right-click database → Query Tool
4. Paste and run:

```sql
-- Insert test organizer account
-- Password: "test123" (BCrypt encrypted)
INSERT INTO organizers (full_name, email, password, institute_name, created_at, updated_at)
VALUES (
    'Test Organizer',
    'test@demo.com',
    '$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG',
    'Demo College',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

**Then login with:**
- Email: `test@demo.com`
- Password: `test123`

---

## Multiple Test Accounts

Run this to create several test accounts:

```sql
-- Test Account 1
INSERT INTO organizers (full_name, email, password, institute_name, created_at, updated_at)
VALUES (
    'John Doe',
    'john@example.com',
    '$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG',
    'ABC University',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test Account 2
INSERT INTO organizers (full_name, email, password, institute_name, created_at, updated_at)
VALUES (
    'Jane Smith',
    'jane@example.com',
    '$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG',
    'XYZ College',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test Account 3
INSERT INTO organizers (full_name, email, password, institute_name, created_at, updated_at)
VALUES (
    'Admin User',
    'admin@test.com',
    '$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG',
    'Test Institute',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

**All have password:** `test123`

---

## Test Credentials Summary

| Email | Password | Name | Institute |
|-------|----------|------|-----------|
| test@demo.com | test123 | Test Organizer | Demo College |
| john@example.com | test123 | John Doe | ABC University |
| jane@example.com | test123 | Jane Smith | XYZ College |
| admin@test.com | test123 | Admin User | Test Institute |

---

## ✅ Recommended Flow

**For First Time:**
1. Make sure services are running
2. Go to http://localhost:5173
3. Click "Register here"
4. Use test credentials above
5. Login automatically after registration

**If Login Page Appears:**
- You don't have an account yet
- Click "Register here" first
- Or run SQL insert script above

---

## 🐛 Troubleshooting

### "Invalid credentials" error
- Account doesn't exist yet
- Register first, or run SQL script

### Can't see Register link
- Check if you're on login page
- Link is at bottom: "Don't have an account? Register here"

### Registration fails
- Check backend is running
- Check database is connected
- Look for errors in backend terminal

---

## Quick Test Account Creation

**Fastest way:**

1. **Open pgAdmin**
2. **Run this ONE command:**
```sql
INSERT INTO organizers (full_name, email, password, institute_name, created_at, updated_at)
VALUES ('Test User', 'test@demo.com', '$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG', 'Demo College', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

3. **Login with:**
   - Email: `test@demo.com`
   - Password: `test123`

Done! 🎉

---

## Password Info

The encrypted password `$2a$10$N9qmPaLQW5Z3j3Gk8G5EK.xVX5xt5Jx2rLZC1KPtD0VqpQHmXqYPG` is BCrypt hash of `test123`.

You can use this same hash for all test accounts - they'll all have password `test123`.

---

**Choose Option 1 (Register) if you want to test the registration flow.**
**Choose Option 2 (SQL Insert) if you want to skip straight to login!**
