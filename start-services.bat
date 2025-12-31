@echo off
echo ================================================
echo Certificate Generation System - Service Starter
echo ================================================
echo.
echo IMPORTANT: Before running, ensure:
echo 1. PostgreSQL is running
echo 2. Database 'certificate_system' is created
echo 3. Password in application.properties is correct
echo.
pause

echo.
echo Starting Backend (Spring Boot)...
echo.
start "Backend - Spring Boot" cmd /k "cd backend && mvn spring-boot:run"

echo Waiting 20 seconds for backend to start...
timeout /t 20 /nobreak

echo.
echo Starting Frontend (React)...
echo.
start "Frontend - React" cmd /k "cd frontend && npm run dev"

echo.
echo ================================================
echo Services are starting!
echo ================================================
echo.
echo Backend:  http://localhost:8080/api
echo Frontend: http://localhost:5173
echo.
echo Check the opened windows for logs and errors.
echo Close this window when done.
echo.
pause
