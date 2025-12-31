# Database Setup Instructions for PostgreSQL

## Step 1: Open pgAdmin

1. Launch **pgAdmin 4** from your Start menu
2. Connect to PostgreSQL server (usually localhost)
3. Enter your password when prompted

## Step 2: Create Database

### Option A: Using GUI
1. In the left sidebar, right-click on "Databases"
2. Select "Create" > "Database..."
3. In the "Database" field, enter: `certificate_system`
4. Click "Save"

### Option B: Using Query Tool
1. Right-click on "PostgreSQL 14" (or your version)
2. Select "Query Tool"
3. Paste and run:
```sql
CREATE DATABASE certificate_system;
```

## Step 3: Create Tables

1. In pgAdmin, click on the `certificate_system` database
2. Right-click on `certificate_system` > "Query Tool"
3. Copy and paste the entire contents of `database-setup.sql`
4. Click the Execute button (▶) or press F5
5. You should see "Query returned successfully"

## Step 4: Verify Tables

1. In pgAdmin, expand `certificate_system` > Schemas > public > Tables
2. You should see 4 tables:
   - organizers
   - events
   - participants
   - certificates

## ✅ Database is Ready!

Your PostgreSQL database is now configured and ready for the application.

Next steps:
1. Update backend `application.properties` with your PostgreSQL password
2. Start the backend server
3. Start the frontend application

## Common Issues

### Cannot create database
- Make sure you're connected as postgres user or a user with CREATE DATABASE privileges

### Tables not appearing
- Refresh the Tables list in pgAdmin
- Check for any error messages in the Query Tool

### Connection refused
- Make sure PostgreSQL service is running
- Check Services (Windows) > postgresql-x64-XX should be "Running"
