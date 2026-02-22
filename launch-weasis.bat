@echo off
REM ============================================================
REM HerniaCT - DICOM Viewer for Hernia Measurement
REM Based on Weasis v4.4.0 (Open Source DICOM Viewer)
REM ============================================================
REM
REM This script launches Weasis to view DICOM files.
REM
REM Usage:
REM   launch-weasis.bat                     (opens Weasis)
REM   launch-weasis.bat "C:\path\to\dicom"  (opens with DICOM folder)
REM
REM Prerequisites:
REM   - Weasis must be installed. Run install-weasis.bat first.
REM ============================================================

setlocal enabledelayedexpansion

REM --- Check common Weasis install locations ---
set "WEASIS_EXE="

REM Check Program Files
if exist "%ProgramFiles%\Weasis\Weasis.exe" (
    set "WEASIS_EXE=%ProgramFiles%\Weasis\Weasis.exe"
    goto :found
)

REM Check Program Files (x86)
if exist "%ProgramFiles(x86)%\Weasis\Weasis.exe" (
    set "WEASIS_EXE=%ProgramFiles(x86)%\Weasis\Weasis.exe"
    goto :found
)

REM Check Local AppData
if exist "%LocalAppData%\Weasis\Weasis.exe" (
    set "WEASIS_EXE=%LocalAppData%\Weasis\Weasis.exe"
    goto :found
)

REM Check if Weasis is on PATH
where Weasis.exe >nul 2>&1
if %errorlevel% equ 0 (
    set "WEASIS_EXE=Weasis.exe"
    goto :found
)

REM Check relative path (portable install in project)
if exist "%~dp0weasis\Weasis.exe" (
    set "WEASIS_EXE=%~dp0weasis\Weasis.exe"
    goto :found
)

echo.
echo ERROR: Weasis not found!
echo.
echo Please install Weasis first by running:
echo   install-weasis.bat
echo.
echo Or download from: https://github.com/nroduit/Weasis/releases/tag/v4.4.0
echo   Download: Weasis-4.4.0-x86-64.msi
echo.
pause
exit /b 1

:found
echo Using Weasis at: %WEASIS_EXE%
echo.

REM --- Launch Weasis ---
if "%~1"=="" (
    REM No arguments - just open Weasis
    echo Launching Weasis...
    start "" "%WEASIS_EXE%"
) else (
    REM Arguments provided - pass DICOM path
    echo Launching Weasis with: %*
    start "" "%WEASIS_EXE%" $dicom:get -l "%~1"
)

endlocal
