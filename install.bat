@echo off
:: =============================================================================
:: HerniaCT DICOM Viewer - One-Click Installer (Windows)
:: =============================================================================

echo.
echo   HerniaCT DICOM Viewer - Installer
echo   =====================================
echo.

:: Get the directory of this script
cd /d "%~dp0"

:: ---------- Check for Node.js ----------
echo [1/3] Checking for Node.js...

where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo   ERROR: Node.js is not installed.
    echo.
    echo   Please install Node.js 18 or later:
    echo   https://nodejs.org/en/download/
    echo.
    echo   Download the Windows Installer (.msi) and run it.
    echo   After installing, close this window and run install.bat again.
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('node --version') do set NODE_VERSION=%%i
echo   Found Node.js %NODE_VERSION%

:: ---------- Check for npm ----------
echo [2/3] Checking for npm...

where npm >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo   ERROR: npm is not installed.
    echo   It usually comes with Node.js. Please reinstall Node.js.
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('npm --version') do set NPM_VERSION=%%i
echo   Found npm %NPM_VERSION%

:: ---------- Install dependencies ----------
echo [3/3] Installing dependencies...
echo.

call npm install

if %ERRORLEVEL% neq 0 (
    echo.
    echo   ERROR: Failed to install dependencies.
    echo   Please check the error messages above and try again.
    echo.
    pause
    exit /b 1
)

echo.
echo   Installation complete!
echo.
echo   To start the viewer, double-click:
echo     start.bat
echo.
echo   Or run: npm start
echo.
pause
