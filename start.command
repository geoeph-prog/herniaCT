#!/usr/bin/env bash
# =============================================================================
# HerniaCT DICOM Viewer - One-Click Start
# Double-click this file on macOS to run. On Linux, run: bash start.command
# =============================================================================
set -e

# -- Fix PATH for macOS Finder launch (double-click) --
# Finder doesn't load .bashrc/.zshrc, so node/npm may not be in PATH.
export PATH="/usr/local/bin:/opt/homebrew/bin:$HOME/.nvm/versions/node/$(ls "$HOME/.nvm/versions/node/" 2>/dev/null | sort -V | tail -1)/bin:$PATH" 2>/dev/null
# Load nvm if available
[ -s "$HOME/.nvm/nvm.sh" ] && source "$HOME/.nvm/nvm.sh" 2>/dev/null
# Load user shell profile as fallback
[ -f "$HOME/.zshrc" ] && source "$HOME/.zshrc" 2>/dev/null || [ -f "$HOME/.bashrc" ] && source "$HOME/.bashrc" 2>/dev/null || true

BOLD='\033[1m'
CYAN='\033[36m'
GREEN='\033[32m'
RED='\033[31m'
RESET='\033[0m'

# cd to the folder this script is in
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo -e "${CYAN}${BOLD}  HerniaCT DICOM Viewer${RESET}"
echo ""

# Check node is available
if ! command -v node &>/dev/null; then
    echo -e "  ${RED}Node.js not found.${RESET}"
    echo -e "  Please run install.command first, or install Node.js from https://nodejs.org"
    echo ""
    read -p "Press Enter to close..."
    exit 1
fi

# Auto-install if needed
if [ ! -d "node_modules" ]; then
    echo -e "  ${RED}Dependencies not installed. Installing now...${RESET}"
    echo ""
    npm install
    echo ""
fi

echo -e "  ${GREEN}Starting viewer...${RESET}"
echo -e "  Browser will open at ${CYAN}http://localhost:3000${RESET}"
echo -e "  Press ${BOLD}Ctrl+C${RESET} to stop the server."
echo ""

npm start
