package org.weasis.dicom.hernia;

import org.dcm4che3.data.Tag;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.TagD;

/**
 * Calculator for abdominal volumes on CT series.
 *
 * Computes:
 * - Intra-abdominal volume: Volume within the peritoneal cavity
 * - Extra-abdominal (hernia sac) volume: Volume of herniated contents
 *   outside the abdominal wall fascia
 * - Loss of domain ratio: Hernia vol / (Abdominal vol + Hernia vol) x 100
 *
 * Method:
 * - For each axial slice, segment the abdominal cavity using HU thresholding
 * - Multiply cross-sectional area by slice thickness
 * - Sum across all slices
 *
 * Body contour threshold: > -500 HU (separates body from air)
 */
public class VolumeCalculator {

    private static final int BODY_CONTOUR_HU = -500;

    /** Y fraction marking approximate anterior abdominal wall boundary. */
    private static final double ANTERIOR_WALL_Y_FRACTION = 0.35;

    /**
     * Volume calculation results.
     */
    public record VolumeResult(
        double intraAbdominalVolumeMl,
        double extraAbdominalVolumeMl,
        double lossOfDomainPercent,
        int slicesAnalyzed
    ) {}

    /**
     * Calculate volumes across an entire CT series.
     *
     * @param series The loaded DICOM series
     * @return VolumeResult with computed volumes
     */
    public VolumeResult calculateVolumes(MediaSeries<DicomImageElement> series) {
        if (series == null) {
            return new VolumeResult(0, 0, 0, 0);
        }

        int sliceCount = series.size(null);
        if (sliceCount == 0) {
            return new VolumeResult(0, 0, 0, 0);
        }

        double totalIntraAbdominalMm3 = 0;
        double totalExtraAbdominalMm3 = 0;
        int slicesAnalyzed = 0;

        for (int i = 0; i < sliceCount; i++) {
            DicomImageElement image = series.getMedia(i, null, null);
            if (image == null || !image.isImageInitialized()) {
                continue;
            }

            double sliceThickness = getSliceThickness(image);
            double[] pixelSpacing = getPixelSpacing(image);
            double pixelAreaMm2 = pixelSpacing[0] * pixelSpacing[1];

            Mat mat = getRawMat(image);
            if (mat == null || mat.empty()) {
                continue;
            }

            int width = mat.cols();
            int height = mat.rows();

            Mat floatMat = new Mat();
            mat.convertTo(floatMat, CvType.CV_32FC1);

            int intraAbdominalPixels = 0;
            int extraAbdominalPixels = 0;

            float[] pixelData = new float[1];

            for (int y = 0; y < height; y++) {
                // Find body edges in this row
                int leftEdge = -1;
                int rightEdge = -1;

                for (int x = 0; x < width; x++) {
                    floatMat.get(y, x, pixelData);
                    int rawPixel = (int) pixelData[0];
                    Number huValue = image.pixelToRealValue(rawPixel, null);
                    int hu = huValue != null ? huValue.intValue() : rawPixel;

                    if (hu > BODY_CONTOUR_HU) {
                        if (leftEdge == -1) {
                            leftEdge = x;
                        }
                        rightEdge = x;
                    }
                }

                if (leftEdge == -1) {
                    continue;
                }

                for (int x = leftEdge; x <= rightEdge; x++) {
                    floatMat.get(y, x, pixelData);
                    int rawPixel = (int) pixelData[0];
                    Number huValue = image.pixelToRealValue(rawPixel, null);
                    int hu = huValue != null ? huValue.intValue() : rawPixel;

                    if (hu > BODY_CONTOUR_HU) {
                        double yFrac = (double) y / height;
                        if (yFrac < ANTERIOR_WALL_Y_FRACTION
                            && isAnteriorProtrusion(floatMat, image, x, y, width, height)) {
                            extraAbdominalPixels++;
                        } else {
                            intraAbdominalPixels++;
                        }
                    }
                }
            }

            floatMat.release();

            totalIntraAbdominalMm3 += intraAbdominalPixels * pixelAreaMm2 * sliceThickness;
            totalExtraAbdominalMm3 += extraAbdominalPixels * pixelAreaMm2 * sliceThickness;
            slicesAnalyzed++;
        }

        double intraAbdominalMl = totalIntraAbdominalMm3 / 1000.0;
        double extraAbdominalMl = totalExtraAbdominalMm3 / 1000.0;

        double totalVolume = intraAbdominalMl + extraAbdominalMl;
        double lossOfDomainPercent = totalVolume > 0
            ? (extraAbdominalMl / totalVolume) * 100.0
            : 0;

        return new VolumeResult(
            intraAbdominalMl,
            extraAbdominalMl,
            lossOfDomainPercent,
            slicesAnalyzed
        );
    }

    /**
     * Check if a pixel is part of an anterior protrusion (hernia sac).
     * Scans downward looking for air gap between protrusion and main cavity.
     */
    private boolean isAnteriorProtrusion(Mat floatMat, DicomImageElement image,
                                         int x, int y, int width, int height) {
        boolean foundBody = false;
        boolean foundGap = false;
        int airGapPixels = 0;

        float[] pixelData = new float[1];

        for (int scanY = y; scanY < height; scanY++) {
            floatMat.get(scanY, x, pixelData);
            int rawPixel = (int) pixelData[0];
            Number huValue = image.pixelToRealValue(rawPixel, null);
            int hu = huValue != null ? huValue.intValue() : rawPixel;

            if (hu > BODY_CONTOUR_HU) {
                if (foundGap) {
                    return true;
                }
                foundBody = true;
            } else if (foundBody) {
                foundGap = true;
                airGapPixels++;
                if (airGapPixels > 10) {
                    break;
                }
            }
        }

        return false;
    }

    private double getSliceThickness(DicomImageElement image) {
        Double thickness = TagD.getTagValue(image, Tag.SliceThickness, Double.class);
        return thickness != null ? thickness : 1.0;
    }

    private double[] getPixelSpacing(DicomImageElement image) {
        double[] spacing = {1.0, 1.0};
        double[] ps = TagD.getTagValue(image, Tag.PixelSpacing, double[].class);
        if (ps != null && ps.length >= 2) {
            spacing[0] = ps[1];
            spacing[1] = ps[0];
        }
        return spacing;
    }

    private Mat getRawMat(DicomImageElement image) {
        try {
            var planarImage = image.getImage(null);
            return planarImage != null ? planarImage.toMat() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
