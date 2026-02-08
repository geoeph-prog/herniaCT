import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#4fc3f7',
      light: '#8bf6ff',
      dark: '#0093c4',
    },
    secondary: {
      main: '#ce93d8',
      light: '#ffc4ff',
      dark: '#9c64a6',
    },
    background: {
      default: '#0a0a0f',
      paper: '#141420',
    },
    text: {
      primary: '#e8e8ed',
      secondary: '#9e9eb0',
    },
  },
  typography: {
    fontFamily: "'Inter', sans-serif",
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 500,
        },
      },
    },
    MuiToggleButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 500,
          borderColor: 'rgba(255,255,255,0.12)',
          '&.Mui-selected': {
            backgroundColor: 'rgba(79, 195, 247, 0.2)',
            borderColor: '#4fc3f7',
            color: '#4fc3f7',
          },
        },
      },
    },
  },
});

export default theme;
