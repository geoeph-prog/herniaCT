import React, { useRef, useState, useEffect, useCallback } from 'react';
import { App as DwvApp } from 'dwv';
import { Box, Typography, LinearProgress, Snackbar, Alert } from '@mui/material';
import Toolbar from './Toolbar';
import TagDialog from './TagDialog';
import WelcomeScreen from './WelcomeScreen';

export default function DICOMViewer() {
  const dwvAppRef = useRef(null);
  const [loaded, setLoaded] = useState(false);
  const [loadProgress, setLoadProgress] = useState(0);
  const [loading, setLoading] = useState(false);
  const [activeTool, setActiveTool] = useState('Scroll');
  const [showTags, setShowTags] = useState(false);
  const [metaData, setMetaData] = useState(null);
  const [dataLoaded, setDataLoaded] = useState(false);
  const [orientation, setOrientation] = useState('axial');
  const [error, setError] = useState(null);
  const [patientInfo, setPatientInfo] = useState(null);
  const [activeDataId, setActiveDataId] = useState(null);
  const layerGroupRef = useRef(null);
  const fileInputRef = useRef(null);

  // Initialize dwv App
  useEffect(() => {
    const app = new DwvApp();
    dwvAppRef.current = app;

    app.init({
      dataViewConfigs: {
        '*': [{ divId: 'layerGroup0' }],
      },
      tools: {
        Scroll: {},
        ZoomAndPan: {},
        WindowLevel: {},
        Draw: {
          options: ['Ruler', 'Arrow', 'Circle', 'Ellipse', 'Rectangle', 'FreeHand'],
        },
      },
    });

    app.addEventListener('loadstart', () => {
      setLoading(true);
      setLoadProgress(0);
    });

    app.addEventListener('loadprogress', (event) => {
      setLoadProgress(event.loaded);
    });

    app.addEventListener('load', (event) => {
      setDataLoaded(true);
      setLoaded(true);
      setActiveDataId(event.dataid);

      const meta = app.getMetaData(event.dataid);
      setMetaData(meta);

      // Extract patient info from metadata
      if (meta) {
        const getValue = (tag) => {
          try {
            if (meta[tag] && meta[tag].value && meta[tag].value.length > 0) {
              return meta[tag].value[0];
            }
          } catch {
            // ignore
          }
          return '';
        };
        setPatientInfo({
          patientName: getValue('00100010'),
          patientId: getValue('00100020'),
          studyDate: getValue('00080020'),
          modality: getValue('00080060'),
          studyDescription: getValue('00081030'),
          seriesDescription: getValue('0008103E'),
          institution: getValue('00080080'),
          sliceThickness: getValue('00180050'),
          rows: getValue('00280010'),
          columns: getValue('00280011'),
        });
      }

      app.setTool('Scroll');
      setActiveTool('Scroll');
    });

    app.addEventListener('loadend', () => {
      setLoading(false);
    });

    app.addEventListener('loaderror', (event) => {
      setError(event.error || 'Failed to load DICOM file');
      setLoading(false);
    });

    // Keyboard shortcuts
    const handleKeyDown = (event) => {
      app.defaultOnKeydown(event);
    };
    window.addEventListener('keydown', handleKeyDown);

    // Window resize
    const handleResize = () => {
      app.onResize();
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  // Handle file open via file dialog
  const handleFileOpen = useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const handleFileChange = useCallback((event) => {
    const files = event.target.files;
    if (files && files.length > 0) {
      dwvAppRef.current?.loadFiles(Array.from(files));
    }
    // Reset input so same file can be re-selected
    event.target.value = '';
  }, []);

  // Handle drag and drop
  const handleDragOver = useCallback((event) => {
    event.preventDefault();
    event.stopPropagation();
  }, []);

  const handleDrop = useCallback((event) => {
    event.preventDefault();
    event.stopPropagation();
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      dwvAppRef.current?.loadFiles(Array.from(files));
    }
  }, []);

  // Handle tool change
  const handleToolChange = useCallback((tool) => {
    if (!dwvAppRef.current || !dataLoaded) return;
    setActiveTool(tool);
    dwvAppRef.current.setTool(tool);
  }, [dataLoaded]);

  // Handle draw shape change
  const handleShapeChange = useCallback((shape) => {
    if (!dwvAppRef.current || !dataLoaded) return;
    dwvAppRef.current.setTool('Draw');
    setActiveTool('Draw');
    dwvAppRef.current.setToolFeatures({ shapeName: shape });
  }, [dataLoaded]);

  // Handle orientation change (MPR)
  const handleOrientationChange = useCallback((newOrientation) => {
    if (!dwvAppRef.current || !dataLoaded || !activeDataId) return;
    setOrientation(newOrientation);
    dwvAppRef.current.setDataViewConfigs({
      '*': [{
        divId: 'layerGroup0',
        orientation: newOrientation,
      }],
    });
    dwvAppRef.current.render(activeDataId);
  }, [dataLoaded, activeDataId]);

  // Handle reset
  const handleReset = useCallback(() => {
    if (!dwvAppRef.current || !dataLoaded) return;
    dwvAppRef.current.resetLayout();
  }, [dataLoaded]);

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100vh',
        width: '100vw',
        bgcolor: 'background.default',
      }}
    >
      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept=".dcm,.DCM,.dicom,.DICOM,application/dicom"
        style={{ display: 'none' }}
        onChange={handleFileChange}
      />

      {/* Toolbar */}
      <Toolbar
        activeTool={activeTool}
        orientation={orientation}
        dataLoaded={dataLoaded}
        patientInfo={patientInfo}
        onToolChange={handleToolChange}
        onShapeChange={handleShapeChange}
        onOrientationChange={handleOrientationChange}
        onReset={handleReset}
        onFileOpen={handleFileOpen}
        onShowTags={() => setShowTags(true)}
      />

      {/* Loading progress bar */}
      {loading && (
        <LinearProgress
          variant="determinate"
          value={loadProgress}
          sx={{
            height: 3,
            bgcolor: 'rgba(79, 195, 247, 0.1)',
            '& .MuiLinearProgress-bar': {
              bgcolor: 'primary.main',
            },
          }}
        />
      )}

      {/* Main viewer area */}
      <Box
        sx={{
          flex: 1,
          position: 'relative',
          overflow: 'hidden',
        }}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
      >
        {/* dwv layer container */}
        <Box
          id="layerGroup0"
          ref={layerGroupRef}
          sx={{
            width: '100%',
            height: '100%',
            position: 'absolute',
            top: 0,
            left: 0,
            display: dataLoaded ? 'block' : 'none',
            '& canvas': {
              imageRendering: 'pixelated',
            },
          }}
        />

        {/* Welcome screen when no data loaded */}
        {!dataLoaded && !loading && (
          <WelcomeScreen onFileOpen={handleFileOpen} />
        )}

        {/* Patient info overlay */}
        {dataLoaded && patientInfo && (
          <Box
            sx={{
              position: 'absolute',
              top: 12,
              left: 12,
              bgcolor: 'rgba(0,0,0,0.6)',
              borderRadius: 1,
              p: 1,
              px: 1.5,
              pointerEvents: 'none',
              backdropFilter: 'blur(4px)',
            }}
          >
            <Typography variant="caption" sx={{ color: '#4fc3f7', fontWeight: 600, display: 'block' }}>
              {patientInfo.patientName || 'Unknown Patient'}
            </Typography>
            {patientInfo.modality && (
              <Typography variant="caption" sx={{ color: '#9e9eb0', display: 'block', fontSize: '0.65rem' }}>
                {patientInfo.modality}
                {patientInfo.studyDescription ? ` - ${patientInfo.studyDescription}` : ''}
              </Typography>
            )}
          </Box>
        )}
      </Box>

      {/* DICOM Tags Dialog */}
      <TagDialog
        open={showTags}
        onClose={() => setShowTags(false)}
        metaData={metaData}
      />

      {/* Error snackbar */}
      <Snackbar
        open={!!error}
        autoHideDuration={6000}
        onClose={() => setError(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="error" onClose={() => setError(null)} variant="filled">
          {error}
        </Alert>
      </Snackbar>
    </Box>
  );
}
