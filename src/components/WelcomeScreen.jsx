import React from 'react';
import { Box, Typography, Button, Paper } from '@mui/material';
import {
  FolderOpen as FolderIcon,
  CloudUpload as UploadIcon,
} from '@mui/icons-material';

export default function WelcomeScreen({ onFileOpen }) {
  return (
    <Box
      sx={{
        width: '100%',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
        gap: 3,
      }}
    >
      <Paper
        elevation={0}
        sx={{
          bgcolor: 'rgba(20, 20, 32, 0.8)',
          border: '2px dashed rgba(79, 195, 247, 0.25)',
          borderRadius: 3,
          p: 6,
          textAlign: 'center',
          maxWidth: 520,
          width: '90%',
          cursor: 'pointer',
          transition: 'all 0.2s ease',
          '&:hover': {
            borderColor: 'rgba(79, 195, 247, 0.5)',
            bgcolor: 'rgba(20, 20, 32, 0.95)',
          },
        }}
        onClick={onFileOpen}
      >
        <UploadIcon
          sx={{
            fontSize: 64,
            color: '#4fc3f7',
            mb: 2,
            opacity: 0.7,
          }}
        />
        <Typography
          variant="h5"
          sx={{ fontWeight: 600, color: '#e8e8ed', mb: 1 }}
        >
          Open DICOM Files
        </Typography>
        <Typography
          variant="body2"
          sx={{ color: '#9e9eb0', mb: 3, lineHeight: 1.6 }}
        >
          Click to browse or drag and drop DICOM files (.dcm) directly onto
          this window. Supports CT, MRI, X-Ray, Ultrasound, and more.
        </Typography>
        <Button
          variant="contained"
          startIcon={<FolderIcon />}
          size="large"
          sx={{
            bgcolor: '#4fc3f7',
            color: '#0a0a0f',
            fontWeight: 600,
            px: 4,
            '&:hover': {
              bgcolor: '#8bf6ff',
            },
          }}
        >
          Browse Files
        </Button>
      </Paper>

      <Box sx={{ textAlign: 'center', mt: 1 }}>
        <Typography variant="caption" sx={{ color: '#555', display: 'block' }}>
          HerniaCT DICOM Viewer - Powered by dwv
        </Typography>
        <Typography variant="caption" sx={{ color: '#444', display: 'block', mt: 0.5, fontSize: '0.65rem' }}>
          Not intended for clinical diagnostic use
        </Typography>
      </Box>
    </Box>
  );
}
