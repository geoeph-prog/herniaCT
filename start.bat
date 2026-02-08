@echo off
:: =============================================================================
:: HerniaCT DICOM Viewer - One-Click Start (Windows)
:: =============================================================================

cd /d "%~dp0"

echo.
echo   HerniaCT DICOM Viewer
echo.

if not exist "node_modules" (
    echo   Dependencies not installed. Running installer...
    echo.
    call install.bat
    if %ERRORLEVEL% neq 0 exit /b 1
)

echo   Starting viewer...
echo   Opens in your browser at http://localhost:3000
echo   Press Ctrl+C to stop.
echo.

call npm start
