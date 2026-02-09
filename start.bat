@echo off
setlocal
:: =============================================================================
:: HerniaCT DICOM Viewer - One-Click Start (Windows)
:: Double-click this file to start the viewer.
:: =============================================================================

title HerniaCT - DICOM Viewer

:: cd to the folder this .bat file is in
cd /d "%~dp0"

echo.
echo   HerniaCT DICOM Viewer
echo   =====================================
echo.

:: ---------- Check Node.js is available ----------
where node >nul 2>&1
if errorlevel 1 (
    echo   ERROR: Node.js is not found.
    echo.
    echo   Please install Node.js 18 or later from:
    echo   https://nodejs.org/en/download/
    echo.
    echo   Then double-click install.bat, then start.bat
    echo.
    pause
    exit /b 1
)

:: ---------- Auto-install if node_modules missing ----------
if not exist "node_modules\webpack\bin\webpack.js" (
    echo   Dependencies not installed. Installing now...
    echo   This may take a minute on first run.
    echo.
    call npm install
    if errorlevel 1 (
        echo.
        echo   ERROR: npm install failed. See errors above.
        echo.
        pause
        exit /b 1
    )
    echo.
)

:: ---------- Start the viewer ----------
echo   Starting viewer...
echo   Your browser will open at http://localhost:3000
echo.
echo   Leave this window open while using the viewer.
echo   Press Ctrl+C to stop the server.
echo.

call npx webpack serve --config config/webpack.dev.js
if errorlevel 1 (
    echo.
    echo   The viewer stopped unexpectedly. See errors above.
    echo.
    pause
    exit /b 1
)

pause
