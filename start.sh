#!/usr/bin/env bash
# =============================================================================
# HerniaCT DICOM Viewer - One-Click Start
# =============================================================================
set -e

BOLD='\033[1m'
CYAN='\033[36m'
GREEN='\033[32m'
RED='\033[31m'
RESET='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo -e "${CYAN}${BOLD}  HerniaCT DICOM Viewer${RESET}"
echo ""

# Auto-install if needed
if [ ! -d "node_modules" ]; then
    echo -e "  ${RED}Dependencies not installed. Running installer...${RESET}"
    echo ""
    bash "$SCRIPT_DIR/install.sh"
fi

echo -e "  ${GREEN}Starting viewer...${RESET}"
echo -e "  Opens in your browser at ${CYAN}http://localhost:3000${RESET}"
echo -e "  Press ${BOLD}Ctrl+C${RESET} to stop."
echo ""

npm start
