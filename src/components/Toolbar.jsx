import React, { useState } from 'react';
import {
  AppBar,
  Box,
  IconButton,
  ToggleButton,
  ToggleButtonGroup,
  Tooltip,
  Typography,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Chip,
} from '@mui/material';
import {
  FolderOpen as FolderIcon,
  Mouse as ScrollIcon,
  ZoomIn as ZoomIcon,
  Contrast as ContrastIcon,
  Draw as DrawIcon,
  Straighten as RulerIcon,
  Info as InfoIcon,
  RestartAlt as ResetIcon,
  ViewInAr as MprIcon,
  ArrowForward as ArrowIcon,
  Circle as CircleIcon,
  CropLandscape as RectIcon,
  RadioButtonUnchecked as EllipseIcon,
  Gesture as FreeHandIcon,
} from '@mui/icons-material';

const tools = [
  { id: 'Scroll', label: 'Scroll', icon: <ScrollIcon fontSize="small" />, tip: 'Scroll through slices' },
  { id: 'ZoomAndPan', label: 'Zoom', icon: <ZoomIcon fontSize="small" />, tip: 'Zoom and pan' },
  { id: 'WindowLevel', label: 'W/L', icon: <ContrastIcon fontSize="small" />, tip: 'Adjust window/level (brightness/contrast)' },
];

const drawShapes = [
  { id: 'Ruler', label: 'Ruler', icon: <RulerIcon fontSize="small" /> },
  { id: 'Arrow', label: 'Arrow', icon: <ArrowIcon fontSize="small" /> },
  { id: 'Circle', label: 'Circle', icon: <CircleIcon fontSize="small" /> },
  { id: 'Ellipse', label: 'Ellipse', icon: <EllipseIcon fontSize="small" /> },
  { id: 'Rectangle', label: 'Rectangle', icon: <RectIcon fontSize="small" /> },
  { id: 'FreeHand', label: 'Free Hand', icon: <FreeHandIcon fontSize="small" /> },
];

const orientations = [
  { id: 'axial', label: 'Axial' },
  { id: 'coronal', label: 'Coronal' },
  { id: 'sagittal', label: 'Sagittal' },
];

export default function Toolbar({
  activeTool,
  orientation,
  dataLoaded,
  patientInfo,
  onToolChange,
  onShapeChange,
  onOrientationChange,
  onReset,
  onFileOpen,
  onShowTags,
}) {
  const [drawAnchor, setDrawAnchor] = useState(null);
  const [mprAnchor, setMprAnchor] = useState(null);

  return (
    <AppBar
      position="static"
      elevation={0}
      sx={{
        bgcolor: '#141420',
        borderBottom: '1px solid rgba(255,255,255,0.06)',
        flexDirection: 'row',
        alignItems: 'center',
        px: 2,
        py: 0.5,
        minHeight: 52,
        gap: 1,
      }}
    >
      {/* App title */}
      <Typography
        variant="subtitle1"
        sx={{
          fontWeight: 700,
          color: '#4fc3f7',
          mr: 2,
          letterSpacing: '0.02em',
          userSelect: 'none',
          whiteSpace: 'nowrap',
        }}
      >
        HerniaCT
      </Typography>

      <Divider orientation="vertical" flexItem sx={{ borderColor: 'rgba(255,255,255,0.08)', mx: 0.5 }} />

      {/* Open file */}
      <Tooltip title="Open DICOM files" arrow>
        <IconButton onClick={onFileOpen} size="small" sx={{ color: 'text.secondary' }}>
          <FolderIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Divider orientation="vertical" flexItem sx={{ borderColor: 'rgba(255,255,255,0.08)', mx: 0.5 }} />

      {/* Tool buttons */}
      <ToggleButtonGroup
        value={activeTool}
        exclusive
        size="small"
        sx={{ gap: 0.25 }}
      >
        {tools.map((tool) => (
          <Tooltip key={tool.id} title={tool.tip} arrow>
            <span>
              <ToggleButton
                value={tool.id}
                disabled={!dataLoaded}
                onClick={() => onToolChange(tool.id)}
                sx={{ px: 1.5, py: 0.5, gap: 0.5 }}
              >
                {tool.icon}
                <Typography variant="caption" sx={{ fontSize: '0.7rem' }}>
                  {tool.label}
                </Typography>
              </ToggleButton>
            </span>
          </Tooltip>
        ))}

        {/* Draw tool with dropdown */}
        <Tooltip title="Annotation tools" arrow>
          <span>
            <ToggleButton
              value="Draw"
              disabled={!dataLoaded}
              onClick={(e) => setDrawAnchor(e.currentTarget)}
              sx={{ px: 1.5, py: 0.5, gap: 0.5 }}
            >
              <DrawIcon fontSize="small" />
              <Typography variant="caption" sx={{ fontSize: '0.7rem' }}>
                Draw
              </Typography>
            </ToggleButton>
          </span>
        </Tooltip>
      </ToggleButtonGroup>

      {/* Draw shapes menu */}
      <Menu
        anchorEl={drawAnchor}
        open={Boolean(drawAnchor)}
        onClose={() => setDrawAnchor(null)}
        slotProps={{ paper: { sx: { bgcolor: '#1e1e2e', border: '1px solid rgba(255,255,255,0.08)' } } }}
      >
        {drawShapes.map((shape) => (
          <MenuItem
            key={shape.id}
            onClick={() => { onShapeChange(shape.id); setDrawAnchor(null); }}
            sx={{ fontSize: '0.85rem' }}
          >
            <ListItemIcon sx={{ color: 'text.secondary' }}>{shape.icon}</ListItemIcon>
            <ListItemText>{shape.label}</ListItemText>
          </MenuItem>
        ))}
      </Menu>

      <Divider orientation="vertical" flexItem sx={{ borderColor: 'rgba(255,255,255,0.08)', mx: 0.5 }} />

      {/* MPR orientation */}
      <Tooltip title="Change view orientation (MPR)" arrow>
        <span>
          <IconButton
            disabled={!dataLoaded}
            onClick={(e) => setMprAnchor(e.currentTarget)}
            size="small"
            sx={{ color: 'text.secondary' }}
          >
            <MprIcon fontSize="small" />
          </IconButton>
        </span>
      </Tooltip>

      <Menu
        anchorEl={mprAnchor}
        open={Boolean(mprAnchor)}
        onClose={() => setMprAnchor(null)}
        slotProps={{ paper: { sx: { bgcolor: '#1e1e2e', border: '1px solid rgba(255,255,255,0.08)' } } }}
      >
        {orientations.map((o) => (
          <MenuItem
            key={o.id}
            selected={orientation === o.id}
            onClick={() => { onOrientationChange(o.id); setMprAnchor(null); }}
            sx={{ fontSize: '0.85rem' }}
          >
            {o.label}
          </MenuItem>
        ))}
      </Menu>

      {/* Reset */}
      <Tooltip title="Reset view" arrow>
        <span>
          <IconButton
            disabled={!dataLoaded}
            onClick={onReset}
            size="small"
            sx={{ color: 'text.secondary' }}
          >
            <ResetIcon fontSize="small" />
          </IconButton>
        </span>
      </Tooltip>

      {/* DICOM Tags */}
      <Tooltip title="View DICOM tags" arrow>
        <span>
          <IconButton
            disabled={!dataLoaded}
            onClick={onShowTags}
            size="small"
            sx={{ color: 'text.secondary' }}
          >
            <InfoIcon fontSize="small" />
          </IconButton>
        </span>
      </Tooltip>

      {/* Spacer */}
      <Box sx={{ flex: 1 }} />

      {/* Orientation chip */}
      {dataLoaded && (
        <Chip
          label={orientation.charAt(0).toUpperCase() + orientation.slice(1)}
          size="small"
          variant="outlined"
          sx={{
            borderColor: 'rgba(79, 195, 247, 0.3)',
            color: '#4fc3f7',
            fontSize: '0.7rem',
            height: 24,
          }}
        />
      )}
    </AppBar>
  );
}
