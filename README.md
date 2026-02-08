# HerniaCT - DICOM Medical Image Viewer

A user-friendly DICOM medical image viewer with one-click install and one-click start. Built on [dwv](https://github.com/ivmartel/dwv) (DICOM Web Viewer).

**Not intended for clinical diagnostic use.**

## Quick Start

### 1. Install (one time)

**macOS / Linux:**
```bash
./install.sh
```

**Windows:**
Double-click `install.bat`

> Requires [Node.js 18+](https://nodejs.org). The installer will check and guide you if it's missing.

### 2. Start

**macOS / Linux:**
```bash
./start.sh
```

**Windows:**
Double-click `start.bat`

The viewer opens automatically in your browser at `http://localhost:3000`.

## Features

- **Open DICOM files** - Click "Browse Files" or drag and drop `.dcm` files onto the viewer
- **Scroll** - Scroll through CT/MRI slices
- **Window/Level** - Adjust brightness and contrast (click and drag)
- **Zoom & Pan** - Zoom in/out and pan the image
- **Annotations** - Draw rulers, arrows, circles, ellipses, rectangles, and freehand
- **MPR Views** - Switch between axial, coronal, and sagittal orientations
- **DICOM Tags** - Inspect all DICOM metadata with search
- **Patient Info Overlay** - Shows patient name, modality, and study info
- **Keyboard Shortcuts** - Standard dwv keyboard shortcuts supported

## Supported Formats

Supports standard DICOM files including:
- CT (Computed Tomography)
- MRI (Magnetic Resonance Imaging)
- X-Ray / CR (Computed Radiography)
- Ultrasound
- Mammography
- Nuclear Medicine

## Requirements

- [Node.js](https://nodejs.org) 18 or later
- A modern web browser (Chrome, Firefox, Edge, Safari)

## License

This project uses [dwv](https://github.com/ivmartel/dwv) which is licensed under GPL-3.0.
