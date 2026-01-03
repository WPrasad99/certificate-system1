@echo off
REM Quick deployment script for Render (Windows)

echo =========================================
echo    CertiCraft Deployment Helper
echo =========================================
echo.

REM Check if git is initialized
if not exist ".git" (
    echo Initializing Git repository...
    git init
    git add .
    git commit -m "Initial commit for deployment"
    echo [32m✓ Git initialized[0m
) else (
    echo [32m✓ Git already initialized[0m
)

REM Check if remote is set
git remote >nul 2>&1
if errorlevel 1 (
    echo.
    echo Please enter your GitHub repository URL:
    echo Example: https://github.com/username/certicraft.git
    set /p REPO_URL="Repository URL: "
    
    git remote add origin %REPO_URL%
    echo [32m✓ Remote added[0m
)

REM Push to GitHub
echo.
echo Pushing to GitHub...
git branch -M main
git push -u origin main

echo.
echo =========================================
echo    Next Steps:
echo =========================================
echo 1. Go to https://render.com and sign up/login
echo 2. Click 'New +' -^> 'Web Service'
echo 3. Connect your GitHub repository
echo 4. Follow instructions in DEPLOYMENT.md
echo.
echo For detailed guide, see deployment_guide.md
echo =========================================
pause
