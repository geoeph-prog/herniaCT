import React from 'react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import theme from './theme';
import DICOMViewer from './components/DICOMViewer';

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <DICOMViewer />
    </ThemeProvider>
  );
}
