# 🚀 Service Status & Next Steps

## ✅ Current Status

### Frontend (React) - ✅ RUNNING
- **Status**: Successfully started
- **URL**: http://localhost:5173
- **Command**: `npm run dev`
- **Location**: `frontend/`

### Backend (Spring Boot) - ⚠️ NEEDS SETUP
- **Status**: Not running (database connection issue)
- **Expected URL**: http://localhost:8080/api
- **Issue**: PostgreSQL database not configured

---

## 📋 TO START BACKEND - Follow These Steps:

### Step 1: Setup PostgreSQL Database (5 minutes)

1. **Open pgAdmin 4** (from Start menu)

2. **Connect to PostgreSQL**
   - Enter your postgres password when prompted

3. **Create Database**
   - Right-click "Databases" → "Create" → "Database"
   - Name: `certificate_system`
   - Click "Save"

4. **Run Setup Script**
   - Click on `certificate_system` database
   - Right-click → "Query Tool"
   - Open file and copy ALL content from:
     ```
     C:\Users\Pratamesh\OneDrive\Desktop\gdghackathon\database-setup.sql
     ```
   - Paste into Query Tool
   - Click Execute (▶ button) or press F5
   - You should see "Query returned successfully"

### Step 2: Configure Backend Password

1. **Open file**:
   ```
   certificate-system\backend\src\main\resources\application.properties
   ```

2. **Update line 8** with your PostgreSQL password:
   ```properties
   spring.datasource.password=YOUR_POSTGRES_PASSWORD
   ```
   (Replace `YOUR_POSTGRES_PASSWORD` with the password you set for postgres user)

3. **Save the file**

### Step 3: Start Backend

**Option A - Using Command Prompt:**
```cmd
cd C:\Users\Pratamesh\OneDrive\Desktop\gdghackathon\certificate-system\backend
mvn spring-boot:run
```

**Option B - Using Batch File:**
```cmd
cd C:\Users\Pratamesh\OneDrive\Desktop\gdghackathon\certificate-system
start-services.bat
```

### Step 4: Verify Backend is Running

Wait about 30-60 seconds, then you should see:
```
Started CertificateApplication in X.XXX seconds
```

---

## 🎯 Quick Test

Once backend is running:

1. **Open browser**: http://localhost:5173
2. **You should see**: Login page
3. **Test**: Try registering a new account
   - If it works → Backend is connected! ✅
   - If error → Check backend console for errors

---

## 🐛 If Backend Still Won't Start

**Check these:**

1. **PostgreSQL is running?**
   - Open Services (Windows + R → `services.msc`)
   - Find `postgresql-x64-XX` (where XX is version)
   - Status should be "Running"
   - If not, right-click → Start

2. **Database exists?**
   - Open pgAdmin
   - Check if `certificate_system` database is visible

3. **Password correct?**
   - Check `application.properties` line 8
   - Try connecting via pgAdmin with same password

4. **Port 8080 free?**
   ```powershell
   netstat -ano | findstr :8080
   ```
   - If something is using it, kill that process or change port in application.properties

---

## 📞 Current Service Commands

**Frontend (Already Running)**:
```bash
# Running on http://localhost:5173
# Keep this terminal open
```

**Backend (Start this)**:
```bash
cd C:\Users\Pratamesh\OneDrive\Desktop\gdghackathon\certificate-system\backend
mvn spring-boot:run
```

---

## ✅ When Both Are Running

You'll have:
- ✅ Frontend: http://localhost:5173  
- ✅ Backend: http://localhost:8080/api

Then you can:
1. Register an organizer account
2. Create events
3. Upload participants (use `sample_participants.csv`)
4. Generate certificates
5. Download PDFs

---

**Need help? See DATABASE_SETUP.md for detailed PostgreSQL instructions!**
