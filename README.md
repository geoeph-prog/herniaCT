# HerniaCT - DICOM Viewer for Hernia Measurement

A custom DICOM viewer built on [Weasis v4.4.0](https://github.com/nroduit/Weasis) (open source), focused on hernia measurement and analysis tools.

## Quick Start

### Windows
1. Run `install-weasis.bat` to download and install Weasis
2. Run `launch-weasis.bat` to open the viewer
3. To open a DICOM folder directly: `launch-weasis.bat "C:\path\to\dicom\folder"`

### Linux (Debian/Ubuntu)
1. Download the `.deb` from [Weasis v4.4.0 releases](https://github.com/nroduit/Weasis/releases/tag/v4.4.0)
2. Install: `sudo dpkg -i weasis_4.4.0-1_amd64.deb`
3. Run: `./launch-weasis.sh` or `./launch-weasis.sh /path/to/dicom`

### macOS
1. Download the `.pkg` from [Weasis v4.4.0 releases](https://github.com/nroduit/Weasis/releases/tag/v4.4.0)
2. Install the package
3. Run: `./launch-weasis.sh`

## Prerequisites

- **Weasis v4.4.0** - The viewer itself (includes its own bundled JRE)
- **For building from source**: JDK 21+, Maven 3.8.1+

## Project Structure

```
herniaCT/
  launch-weasis.bat      # Windows launcher
  launch-weasis.sh       # Linux/macOS launcher
  install-weasis.bat     # Windows installer helper
  .gitignore
  README.md
```

## Building from Source

Weasis source code is available at https://github.com/nroduit/Weasis (tag v4.4.0).

```bash
git clone --branch v4.4.0 https://github.com/nroduit/Weasis.git
cd Weasis
mvn clean install -DskipTests
```

Requires JDK 21 and Maven 3.8.1+.

## Weasis Version

This project uses **Weasis v4.4.0** (released May 2024), which requires **JDK 21**.

## License

Weasis is dual-licensed under [EPL-2.0](https://www.eclipse.org/legal/epl-2.0/) and [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0).
