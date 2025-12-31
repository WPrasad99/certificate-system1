@echo off
echo ================================================
echo Pushing Certificate System to GitHub
echo ================================================
echo.

REM Initialize Git repository
echo [1/6] Initializing Git repository...
git init
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed or not in PATH!
    echo Please install Git from: https://git-scm.com/download/win
    pause
    exit /b 1
)

REM Add all files
echo [2/6] Adding files...
git add .

REM Commit
echo [3/6] Creating commit...
git commit -m "Initial commit: Certificate Generation System"

REM Set branch name
echo [4/6] Setting branch to main...
git branch -M main

REM Add remote
echo [5/6] Adding GitHub remote...
git remote add origin https://github.com/WPrasad99/certificate-system.git

REM Push to GitHub
echo [6/6] Pushing to GitHub...
echo.
echo You may be prompted for GitHub credentials:
echo Username: Your GitHub username
echo Password: Your Personal Access Token (NOT your password!)
echo.
echo To create a Personal Access Token:
echo 1. Go to: https://github.com/settings/tokens
echo 2. Click "Generate new token (classic)"
echo 3. Select "repo" scope
echo 4. Copy the token and use it as password
echo.
pause
git push -u origin main

echo.
echo ================================================
echo Done! Check your repository at:
echo https://github.com/WPrasad99/certificate-system
echo ================================================
pause
