@echo off
setlocal
:: =============================================================================
:: HerniaCT DICOM Viewer - One-Click Installer (Windows)
:: Double-click this file to install.
:: =============================================================================

title HerniaCT - Installer

echo.
echo   HerniaCT DICOM Viewer - Installer
echo   =====================================
echo.

:: cd to the folder this .bat file is in
cd /d "%~dp0"

:: ---------- Check for Node.js ----------
echo [1/3] Checking for Node.js...

where node >nul 2>&1
if errorlevel 1 (
    echo.
    echo   ERROR: Node.js is not installed.
    echo.
    echo   Please install Node.js 18 or later:
    echo   https://nodejs.org/en/download/
    echo.
    echo   Download the Windows Installer (.msi) and run it.
    echo   After installing, close this window and double-click install.bat again.
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('node --version') do set "NODE_VERSION=%%i"
echo   Found Node.js %NODE_VERSION%

:: ---------- Check for npm ----------
echo [2/3] Checking for npm...

where npm >nul 2>&1
if errorlevel 1 (
    echo.
    echo   ERROR: npm is not installed.
    echo   Please reinstall Node.js from https://nodejs.org
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('npm --version') do set "NPM_VERSION=%%i"
echo   Found npm %NPM_VERSION%

:: ---------- Install dependencies ----------
echo.
echo [3/3] Installing dependencies (this may take a minute)...
echo.

call npm install
if errorlevel 1 (
    echo.
    echo   ERROR: Failed to install dependencies.
    echo   Check the error messages above and try again.
    echo.
    pause
    exit /b 1
)

echo.
echo   =====================================
echo   Installation complete!
echo   =====================================
echo.
echo   To start the viewer, double-click: start.bat
echo.
pause
