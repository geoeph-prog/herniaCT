@echo off
REM ============================================================
REM HerniaCT - Weasis Installer Helper
REM Downloads and installs Weasis v4.4.0 for Windows
REM ============================================================

setlocal

echo ============================================================
echo  HerniaCT - Weasis DICOM Viewer Setup
echo  Version: 4.4.0
echo ============================================================
echo.

REM Check if Weasis is already installed
if exist "%ProgramFiles%\Weasis\Weasis.exe" (
    echo Weasis is already installed at: %ProgramFiles%\Weasis
    echo.
    echo To reinstall, uninstall first from Windows Settings.
    pause
    exit /b 0
)

REM Check if MSI already downloaded
set "MSI_FILE=%~dp0Weasis-4.4.0-x86-64.msi"

if exist "%MSI_FILE%" (
    echo Found installer: %MSI_FILE%
    echo.
    goto :install
)

echo Weasis installer not found locally.
echo.
echo Please download Weasis-4.4.0-x86-64.msi from:
echo   https://github.com/nroduit/Weasis/releases/tag/v4.4.0
echo.
echo Save it to this folder:
echo   %~dp0
echo.
echo Then run this script again.
echo.

REM Try to open browser to download page
start "" "https://github.com/nroduit/Weasis/releases/tag/v4.4.0"

pause
exit /b 1

:install
echo Installing Weasis...
echo.
msiexec /i "%MSI_FILE%" /passive
if %errorlevel% equ 0 (
    echo.
    echo Weasis installed successfully!
    echo You can now run launch-weasis.bat to start the viewer.
) else (
    echo.
    echo Installation may have failed. Error code: %errorlevel%
    echo Try running the MSI installer manually.
)

echo.
pause
endlocal
