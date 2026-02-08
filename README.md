# HerniaCT - DICOM Medical Image Viewer

A user-friendly DICOM medical image viewer with one-click install and one-click start.
Built on [dwv](https://github.com/ivmartel/dwv) (DICOM Web Viewer) - the actual dwv source code is included directly.

**Not intended for clinical diagnostic use.**

## Quick Start

### 1. Install (one time only)

**macOS:** Double-click `install.command`
**Windows:** Double-click `install.bat`
**Linux terminal:** `bash install.command`

> Requires [Node.js 18+](https://nodejs.org). The installer checks and guides you if it's missing.

### 2. Start (every time)

**macOS:** Double-click `start.command`
**Windows:** Double-click `start.bat`
**Linux terminal:** `bash start.command`

The viewer opens automatically in your browser at `http://localhost:3000`.
If dependencies aren't installed yet, the start script runs the installer automatically.

## How to Use

1. Click **Choose File** to load DICOM files (.dcm) from your computer
2. Use the **tools** panel to switch between:
   - **Scroll** - scroll through CT/MRI slices
   - **WindowLevel** - adjust brightness/contrast (click + drag)
   - **ZoomAndPan** - zoom and pan the image
   - **Draw** - annotate with rulers, arrows, shapes
   - **Brush / Floodfill / Livewire** - segmentation tools
   - **Filter** - image filters
3. Use **Layout** dropdown to switch between single view, dual view, or MPR
4. **Reset Views** to return to the default layout

## Supported Formats

Standard DICOM files including:
- CT (Computed Tomography)
- MRI (Magnetic Resonance Imaging)
- X-Ray / CR (Computed Radiography)
- Ultrasound
- Mammography
- Nuclear Medicine

## Requirements

- [Node.js](https://nodejs.org) 18 or later (includes npm)
- A modern web browser (Chrome, Firefox, Edge, Safari)

## Credits

This viewer is built on [dwv](https://github.com/ivmartel/dwv) by ivmartel, licensed under GPL-3.0.

## License

GPL-3.0 (same as dwv)
