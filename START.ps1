# Quick Start Script for Certificate System
# Run this from the certificate-system folder

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Certificate Generation System" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Check if PostgreSQL database exists
Write-Host "📋 Step 1: Database Setup" -ForegroundColor Yellow
Write-Host "Please ensure you have:" -ForegroundColor White
Write-Host "  ✓ Created database 'certificate_system' in PostgreSQL" -ForegroundColor Green
Write-Host "  ✓ Run the database-setup.sql script" -ForegroundColor Green
Write-Host ""
Read-Host "Press Enter when database is ready"

# Start Backend
Write-Host ""
Write-Host "🚀 Step 2: Starting Backend..." -ForegroundColor Yellow
Write-Host "Opening backend in new window..." -ForegroundColor White

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd backend; Write-Host 'Starting Spring Boot Backend...' -ForegroundColor Green; mvn spring-boot:run"

Write-Host 'Waiting for backend to start (30 seconds)...' -ForegroundColor White
Start-Sleep -Seconds 10

# Start Frontend
Write-Host ""
Write-Host "🎨 Step 3: Starting Frontend..." -ForegroundColor Yellow
Write-Host "Opening frontend in new window..." -ForegroundColor White

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; Write-Host 'Starting React Frontend...' -ForegroundColor Green; npm run dev"

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "✅ Application Starting!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend:  http://localhost:8080/api" -ForegroundColor White
Write-Host "Frontend: http://localhost:5173" -ForegroundColor White
Write-Host ""
Write-Host "📝 Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Open http://localhost:5173 in your browser" -ForegroundColor White
Write-Host "  2. Register as an organizer" -ForegroundColor White
Write-Host "  3. Create an event" -ForegroundColor White
Write-Host "  4. Upload sample_participants.csv" -ForegroundColor White
Write-Host "  5. Generate certificates!" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C in the backend/frontend windows to stop servers" -ForegroundColor Gray
Write-Host ""
