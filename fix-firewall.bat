@echo off
echo ================================================
echo Fixing Windows Firewall for Certificate System
echo ================================================
echo.
echo This script will open ports 5173 (Frontend) and 8080 (Backend)
echo to allow mobile devices to connect.
echo.
echo IMPORTANT: You must Right-Click and "Run as Administrator"
echo.
pause

echo.
echo Opening Port 5173 (Frontend)...
netsh advfirewall firewall add rule name="Certificate System Frontend" dir=in action=allow protocol=TCP localport=5173 profile=any

echo.
echo Opening Port 8080 (Backend)...
netsh advfirewall firewall add rule name="Certificate System Backend" dir=in action=allow protocol=TCP localport=8080 profile=any

echo.
echo ================================================
echo Firewall rules added!
echo Try scanning the QR code again.
echo ================================================
pause
