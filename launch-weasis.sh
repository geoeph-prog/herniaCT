#!/bin/bash
# ============================================================
# HerniaCT - DICOM Viewer for Hernia Measurement
# Based on Weasis v4.4.0 (Open Source DICOM Viewer)
# ============================================================
#
# Usage:
#   ./launch-weasis.sh                      (opens Weasis)
#   ./launch-weasis.sh /path/to/dicom       (opens with DICOM folder)
#
# Prerequisites:
#   - Weasis must be installed (apt, deb, or portable)
# ============================================================

WEASIS_EXE=""

# Check standard Linux install location
if [ -x "/opt/weasis/bin/Weasis" ]; then
    WEASIS_EXE="/opt/weasis/bin/Weasis"
# Check if on PATH
elif command -v Weasis &>/dev/null; then
    WEASIS_EXE="Weasis"
# Check macOS app bundle
elif [ -x "/Applications/Weasis.app/Contents/MacOS/Weasis" ]; then
    WEASIS_EXE="/Applications/Weasis.app/Contents/MacOS/Weasis"
# Check relative portable install
elif [ -x "$(dirname "$0")/weasis/bin/Weasis" ]; then
    WEASIS_EXE="$(dirname "$0")/weasis/bin/Weasis"
fi

if [ -z "$WEASIS_EXE" ]; then
    echo ""
    echo "ERROR: Weasis not found!"
    echo ""
    echo "Install on Debian/Ubuntu:"
    echo "  sudo dpkg -i weasis_4.4.0-1_amd64.deb"
    echo ""
    echo "Download from: https://github.com/nroduit/Weasis/releases/tag/v4.4.0"
    echo ""
    exit 1
fi

echo "Using Weasis at: $WEASIS_EXE"

if [ -z "$1" ]; then
    echo "Launching Weasis..."
    "$WEASIS_EXE" &
else
    echo "Launching Weasis with: $1"
    "$WEASIS_EXE" '$dicom:get -l "'"$1"'"' &
fi
