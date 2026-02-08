#!/usr/bin/env bash
# =============================================================================
# HerniaCT DICOM Viewer - One-Click Start
# Double-click this file on macOS to run. On Linux, run: bash start.command
# =============================================================================
set -e

BOLD='\033[1m'
CYAN='\033[36m'
GREEN='\033[32m'
RED='\033[31m'
RESET='\033[0m'

# cd to the folder this script is in (handles double-click from Finder)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo -e "${CYAN}${BOLD}  HerniaCT DICOM Viewer${RESET}"
echo ""

# Auto-install if needed
if [ ! -d "node_modules" ]; then
    echo -e "  ${RED}Dependencies not installed. Running installer...${RESET}"
    echo ""
    npm install
    echo ""
fi

echo -e "  ${GREEN}Starting viewer...${RESET}"
echo -e "  Opening browser at ${CYAN}http://localhost:3000${RESET}"
echo -e "  Press ${BOLD}Ctrl+C${RESET} to stop the server."
echo ""

npm start
