package org.weasis.dicom.hernia;

import org.dcm4che3.data.Tag;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.TagD;

/**
 * Automatic mesh detection on CT imaging using Hounsfield Unit thresholds.
 *
 * Surgical mesh materials and their typical HU ranges:
 * - Polypropylene (Marlex, Prolene): ~80-200 HU
 * - PTFE (Gore-Tex): ~80-180 HU
 * - Polyester (Mersilene): ~90-200 HU
 * - Composite mesh: variable, often 80-250 HU
 * - Biologic mesh (acellular dermal matrix): ~40-80 HU (harder to detect)
 *
 * Detection strategy:
 * 1. Focus on the anterior abdominal wall region
 * 2. Threshold for mesh-range HU pixels
 * 3. Connected-component analysis to find mesh clusters
 * 4. Filter by minimum cluster size to exclude noise
 */
public class MeshDetector {

    /** Minimum HU for mesh detection (synthetic mesh). */
    public static final int HU_MIN = 80;

    /** Maximum HU for mesh detection. */
    public static final int HU_MAX = 250;

    /** Anterior abdominal wall ROI as fraction of image. */
    private static final double ROI_X_START = 0.15;
    private static final double ROI_X_END = 0.85;
    private static final double ROI_Y_START = 0.05;
    private static final double ROI_Y_END = 0.55;

    /** Minimum cluster size in pixels to be considered mesh. */
    private static final int MIN_CLUSTER_SIZE = 20;

    /**
     * Result of mesh detection analysis.
     */
    public record MeshResult(
        boolean meshDetected,
        double meshAreaMm2,
        int meshPixelCount,
        double meanHU,
        double minHU,
        double maxHU
    ) {}

    /**
     * Detect mesh on the given axial CT image.
     *
     * @param image The DICOM image element
     * @return MeshResult with detection results
     */
    public MeshResult detectMesh(DicomImageElement image) {
        if (image == null || !image.isImageInitialized()) {
            return new MeshResult(false, 0, 0, 0, 0, 0);
        }

        double[] pixelSpacing = getPixelSpacing(image);
        double pixelAreaMm2 = pixelSpacing[0] * pixelSpacing[1];

        Mat mat = getRawMat(image);
        if (mat == null || mat.empty()) {
            return new MeshResult(false, 0, 0, 0, 0, 0);
        }

        int width = mat.cols();
        int height = mat.rows();

        // Convert to float for pixel access
        Mat floatMat = new Mat();
        mat.convertTo(floatMat, CvType.CV_32FC1);

        int roiXStart = clamp((int) (ROI_X_START * width), 0, width - 1);
        int roiXEnd = clamp((int) (ROI_X_END * width), 0, width - 1);
        int roiYStart = clamp((int) (ROI_Y_START * height), 0, height - 1);
        int roiYEnd = clamp((int) (ROI_Y_END * height), 0, height - 1);

        // Create binary mask for mesh-range pixels
        boolean[][] meshMask = new boolean[height][width];
        float[] pixelData = new float[1];

        for (int y = roiYStart; y < roiYEnd; y++) {
            for (int x = roiXStart; x < roiXEnd; x++) {
                floatMat.get(y, x, pixelData);
                int rawPixel = (int) pixelData[0];
                Number huValue = image.pixelToRealValue(rawPixel, null);
                int hu = huValue != null ? huValue.intValue() : rawPixel;

                if (hu >= HU_MIN && hu <= HU_MAX) {
                    meshMask[y][x] = true;
                }
            }
        }

        // Connected component analysis - flood fill
        boolean[][] visited = new boolean[height][width];
        int totalMeshPixels = 0;

        for (int y = roiYStart; y < roiYEnd; y++) {
            for (int x = roiXStart; x < roiXEnd; x++) {
                if (meshMask[y][x] && !visited[y][x]) {
                    int clusterSize = floodFill(meshMask, visited, x, y, width, height);
                    if (clusterSize >= MIN_CLUSTER_SIZE) {
                        totalMeshPixels += clusterSize;
                    }
                }
            }
        }

        // Compute HU stats for detected mesh pixels
        double sumHU = 0;
        int meshPixels = 0;
        double minDetectedHU = Double.MAX_VALUE;
        double maxDetectedHU = Double.MIN_VALUE;

        if (totalMeshPixels >= MIN_CLUSTER_SIZE) {
            for (int y = roiYStart; y < roiYEnd; y++) {
                for (int x = roiXStart; x < roiXEnd; x++) {
                    if (meshMask[y][x]) {
                        floatMat.get(y, x, pixelData);
                        int rawPixel = (int) pixelData[0];
                        Number huValue = image.pixelToRealValue(rawPixel, null);
                        double hu = huValue != null ? huValue.doubleValue() : rawPixel;
                        sumHU += hu;
                        meshPixels++;
                        minDetectedHU = Math.min(minDetectedHU, hu);
                        maxDetectedHU = Math.max(maxDetectedHU, hu);
                    }
                }
            }
        }

        floatMat.release();

        boolean detected = totalMeshPixels >= MIN_CLUSTER_SIZE;
        double areaMm2 = totalMeshPixels * pixelAreaMm2;
        double meanHU = meshPixels > 0 ? sumHU / meshPixels : 0;

        return new MeshResult(
            detected,
            areaMm2,
            totalMeshPixels,
            meanHU,
            detected ? minDetectedHU : 0,
            detected ? maxDetectedHU : 0
        );
    }

    private int floodFill(boolean[][] mask, boolean[][] visited,
                          int startX, int startY, int width, int height) {
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;
        int count = 0;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            count++;

            for (int d = 0; d < 4; d++) {
                int nx = pos[0] + dx[d];
                int ny = pos[1] + dy[d];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height
                    && mask[ny][nx] && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        return count;
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
