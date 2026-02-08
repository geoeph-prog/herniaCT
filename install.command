#!/usr/bin/env bash
# =============================================================================
# HerniaCT DICOM Viewer - One-Click Installer
# Double-click this file on macOS to run. On Linux, run: bash install.command
# =============================================================================
set -e

BOLD='\033[1m'
CYAN='\033[36m'
GREEN='\033[32m'
YELLOW='\033[33m'
RED='\033[31m'
RESET='\033[0m'

echo ""
echo -e "${CYAN}${BOLD}  HerniaCT DICOM Viewer - Installer${RESET}"
echo -e "  ====================================="
echo ""

# cd to the folder this script is in (handles double-click from Finder)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ---------- Check for Node.js ----------
echo -e "${BOLD}[1/3] Checking for Node.js...${RESET}"

if command -v node &>/dev/null; then
    NODE_VERSION=$(node --version)
    echo -e "  ${GREEN}Found Node.js ${NODE_VERSION}${RESET}"

    MAJOR=$(echo "$NODE_VERSION" | sed 's/v//' | cut -d. -f1)
    if [ "$MAJOR" -lt 18 ]; then
        echo -e "  ${YELLOW}Warning: Node.js 18+ is recommended. You have ${NODE_VERSION}${RESET}"
        echo ""
        echo -e "  To update, visit: ${CYAN}https://nodejs.org${RESET}"
        echo -e "  Or use nvm:       ${CYAN}nvm install 20${RESET}"
        echo ""
        read -p "  Continue anyway? (y/N) " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "  ${RED}Installation cancelled.${RESET}"
            echo ""; read -p "Press Enter to close..."
            exit 1
        fi
    fi
else
    echo -e "  ${RED}Node.js is not installed.${RESET}"
    echo ""
    echo -e "  Please install Node.js 18 or later from:"
    echo -e "    ${CYAN}https://nodejs.org/en/download/${RESET}"
    echo ""
    echo -e "  Quick install:"
    echo -e "    macOS (Homebrew):  ${CYAN}brew install node${RESET}"
    echo -e "    Ubuntu/Debian:     ${CYAN}curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - && sudo apt-get install -y nodejs${RESET}"
    echo -e "    nvm (any OS):      ${CYAN}nvm install 20${RESET}"
    echo ""
    echo ""; read -p "Press Enter to close..."
    exit 1
fi

# ---------- Check for npm ----------
echo -e "${BOLD}[2/3] Checking for npm...${RESET}"

if command -v npm &>/dev/null; then
    NPM_VERSION=$(npm --version)
    echo -e "  ${GREEN}Found npm ${NPM_VERSION}${RESET}"
else
    echo -e "  ${RED}npm not found. It usually comes with Node.js.${RESET}"
    echo -e "  Please reinstall Node.js from ${CYAN}https://nodejs.org${RESET}"
    echo ""; read -p "Press Enter to close..."
    exit 1
fi

# ---------- Install dependencies ----------
echo -e "${BOLD}[3/3] Installing dependencies...${RESET}"
echo ""

npm install

echo ""
echo -e "${GREEN}${BOLD}  Installation complete!${RESET}"
echo ""
echo -e "  To start the viewer, double-click: ${CYAN}start.command${RESET}"
echo ""
read -p "Press Enter to close..."
