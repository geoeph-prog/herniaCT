import React, { useMemo, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  IconButton,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TextField,
  Typography,
  Box,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';

// Common DICOM tag names for display
const TAG_NAMES = {
  '00080005': 'Specific Character Set',
  '00080008': 'Image Type',
  '00080016': 'SOP Class UID',
  '00080018': 'SOP Instance UID',
  '00080020': 'Study Date',
  '00080021': 'Series Date',
  '00080030': 'Study Time',
  '00080050': 'Accession Number',
  '00080060': 'Modality',
  '00080070': 'Manufacturer',
  '00080080': 'Institution Name',
  '00080090': 'Referring Physician',
  '00081030': 'Study Description',
  '0008103E': 'Series Description',
  '00100010': 'Patient Name',
  '00100020': 'Patient ID',
  '00100030': 'Patient Birth Date',
  '00100040': 'Patient Sex',
  '00101010': 'Patient Age',
  '00180050': 'Slice Thickness',
  '00180060': 'KVP',
  '00180088': 'Spacing Between Slices',
  '00181210': 'Convolution Kernel',
  '00185100': 'Patient Position',
  '00200011': 'Series Number',
  '00200012': 'Acquisition Number',
  '00200013': 'Instance Number',
  '00200032': 'Image Position (Patient)',
  '00200037': 'Image Orientation (Patient)',
  '00200052': 'Frame of Reference UID',
  '00201041': 'Slice Location',
  '00280002': 'Samples Per Pixel',
  '00280004': 'Photometric Interpretation',
  '00280010': 'Rows',
  '00280011': 'Columns',
  '00280030': 'Pixel Spacing',
  '00280100': 'Bits Allocated',
  '00280101': 'Bits Stored',
  '00280102': 'High Bit',
  '00280103': 'Pixel Representation',
  '00280120': 'Pixel Padding Value',
  '00281050': 'Window Center',
  '00281051': 'Window Width',
  '00281052': 'Rescale Intercept',
  '00281053': 'Rescale Slope',
  '7FE00010': 'Pixel Data',
};

export default function TagDialog({ open, onClose, metaData }) {
  const [search, setSearch] = useState('');

  const tags = useMemo(() => {
    if (!metaData) return [];
    const result = [];
    for (const [tag, entry] of Object.entries(metaData)) {
      // Skip non-tag entries
      if (tag.length !== 8) continue;
      const group = tag.substring(0, 4).toUpperCase();
      const element = tag.substring(4, 8).toUpperCase();
      const name = TAG_NAMES[tag] || entry.tag?.name || `(${group},${element})`;
      let value = '';
      if (entry.value) {
        if (Array.isArray(entry.value)) {
          value = entry.value.length > 5
            ? entry.value.slice(0, 5).join(', ') + '...'
            : entry.value.join(', ');
        } else {
          value = String(entry.value);
        }
      }
      // Truncate very long values
      if (value.length > 120) value = value.substring(0, 120) + '...';
      result.push({
        tag: `(${group},${element})`,
        name,
        value,
        vr: entry.vr || '',
      });
    }
    result.sort((a, b) => a.tag.localeCompare(b.tag));
    return result;
  }, [metaData]);

  const filteredTags = useMemo(() => {
    if (!search) return tags;
    const s = search.toLowerCase();
    return tags.filter(
      (t) =>
        t.tag.toLowerCase().includes(s) ||
        t.name.toLowerCase().includes(s) ||
        t.value.toLowerCase().includes(s)
    );
  }, [tags, search]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          bgcolor: '#141420',
          border: '1px solid rgba(255,255,255,0.08)',
          maxHeight: '80vh',
        },
      }}
    >
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1, pr: 6 }}>
        <Typography variant="h6" sx={{ fontWeight: 600, color: '#4fc3f7' }}>
          DICOM Tags
        </Typography>
        <Box sx={{ flex: 1 }} />
        <TextField
          size="small"
          placeholder="Search tags..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{
            width: 220,
            '& .MuiOutlinedInput-root': {
              fontSize: '0.85rem',
              bgcolor: 'rgba(255,255,255,0.04)',
            },
          }}
        />
        <IconButton
          onClick={onClose}
          sx={{ position: 'absolute', right: 8, top: 8, color: 'text.secondary' }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent sx={{ p: 0 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell sx={{ bgcolor: '#1a1a2e', fontWeight: 600, width: 130 }}>Tag</TableCell>
              <TableCell sx={{ bgcolor: '#1a1a2e', fontWeight: 600, width: 50 }}>VR</TableCell>
              <TableCell sx={{ bgcolor: '#1a1a2e', fontWeight: 600, width: 200 }}>Name</TableCell>
              <TableCell sx={{ bgcolor: '#1a1a2e', fontWeight: 600 }}>Value</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredTags.map((t) => (
              <TableRow key={t.tag} hover>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem', color: '#ce93d8' }}>
                  {t.tag}
                </TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem', color: '#9e9eb0' }}>
                  {t.vr}
                </TableCell>
                <TableCell sx={{ fontSize: '0.8rem' }}>{t.name}</TableCell>
                <TableCell
                  sx={{
                    fontSize: '0.8rem',
                    fontFamily: 'monospace',
                    wordBreak: 'break-all',
                    maxWidth: 300,
                  }}
                >
                  {t.value}
                </TableCell>
              </TableRow>
            ))}
            {filteredTags.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  {tags.length === 0 ? 'No metadata available' : 'No matching tags found'}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </DialogContent>
    </Dialog>
  );
}
