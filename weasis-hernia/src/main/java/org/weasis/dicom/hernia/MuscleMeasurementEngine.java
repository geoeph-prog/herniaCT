package org.weasis.dicom.hernia;

import org.dcm4che3.data.Tag;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.TagD;

/**
 * Engine for automatic muscle identification and measurement on axial CT.
 *
 * Identifies and measures thickness of:
 * - Psoas major (bilateral) - HU range: -29 to +150
 * - Iliacus (bilateral)
 * - Rectus abdominis (bilateral)
 * - Internal oblique (bilateral)
 * - External oblique (bilateral)
 * - Transversus abdominis (bilateral)
 *
 * Muscles are identified by anatomical position + HU thresholding on axial images.
 * Ideally measured at L3-L4 vertebral level.
 *
 * HU thresholds from Aubrey et al. (JPEN 2014):
 * Skeletal muscle: -29 to +150 HU
 */
public class MuscleMeasurementEngine {

    public static final int MUSCLE_HU_MIN = -29;
    public static final int MUSCLE_HU_MAX = 150;

    // Anatomical region fractions of image (approximate for L3-L4 axial CT)

    // Psoas: posterior, paravertebral, bilateral
    private static final double PSOAS_Y_START = 0.50;
    private static final double PSOAS_Y_END = 0.80;
    private static final double PSOAS_X_RIGHT_START = 0.35;
    private static final double PSOAS_X_RIGHT_END = 0.48;
    private static final double PSOAS_X_LEFT_START = 0.52;
    private static final double PSOAS_X_LEFT_END = 0.65;

    // Iliacus: lateral to psoas, in iliac fossa
    private static final double ILIACUS_Y_START = 0.55;
    private static final double ILIACUS_Y_END = 0.85;
    private static final double ILIACUS_X_RIGHT_START = 0.25;
    private static final double ILIACUS_X_RIGHT_END = 0.38;
    private static final double ILIACUS_X_LEFT_START = 0.62;
    private static final double ILIACUS_X_LEFT_END = 0.75;

    // Rectus abdominis: anterior midline
    private static final double RECTUS_Y_START = 0.10;
    private static final double RECTUS_Y_END = 0.45;
    private static final double RECTUS_X_RIGHT_START = 0.38;
    private static final double RECTUS_X_RIGHT_END = 0.48;
    private static final double RECTUS_X_LEFT_START = 0.52;
    private static final double RECTUS_X_LEFT_END = 0.62;

    // Lateral abdominal wall muscles
    private static final double LATERAL_Y_START = 0.20;
    private static final double LATERAL_Y_END = 0.60;
    private static final double LATERAL_X_RIGHT_START = 0.08;
    private static final double LATERAL_X_RIGHT_END = 0.25;
    private static final double LATERAL_X_LEFT_START = 0.75;
    private static final double LATERAL_X_LEFT_END = 0.92;

    /**
     * Results of muscle measurement analysis.
     */
    public record MuscleResults(
        double rightPsoasThickness,
        double leftPsoasThickness,
        double rightIliacusThickness,
        double leftIliacusThickness,
        double rightRectusThickness,
        double leftRectusThickness,
        double rightIntObliqueThickness,
        double leftIntObliqueThickness,
        double rightExtObliqueThickness,
        double leftExtObliqueThickness,
        double rightTransversusThickness,
        double leftTransversusThickness
    ) {}

    /**
     * Measure all target muscles on the given axial CT image.
     *
     * @param image The DICOM image element (should be axial CT at L3-L4 level)
     * @return MuscleResults with thickness in mm for each muscle
     */
    public MuscleResults measureMuscles(DicomImageElement image) {
        if (image == null || !image.isImageInitialized()) {
            return emptyResults();
        }

        double[] pixelSpacing = getPixelSpacing(image);
        double pixSpacingX = pixelSpacing[0];
        double pixSpacingY = pixelSpacing[1];

        // Get raw image as OpenCV Mat
        Mat mat = getRawMat(image);
        if (mat == null || mat.empty()) {
            return emptyResults();
        }

        int width = mat.cols();
        int height = mat.rows();

        // Build HU map from the raw pixel data
        int[][] huMap = buildHUMap(image, mat, width, height);

        // Measure each muscle region
        double rightPsoas = measureMuscleThicknessInRegion(
            huMap, width, height, pixSpacingY,
            PSOAS_X_RIGHT_START, PSOAS_X_RIGHT_END, PSOAS_Y_START, PSOAS_Y_END);
        double leftPsoas = measureMuscleThicknessInRegion(
            huMap, width, height, pixSpacingY,
            PSOAS_X_LEFT_START, PSOAS_X_LEFT_END, PSOAS_Y_START, PSOAS_Y_END);

        double rightIliacus = measureMuscleThicknessInRegion(
            huMap, width, height, pixSpacingY,
            ILIACUS_X_RIGHT_START, ILIACUS_X_RIGHT_END, ILIACUS_Y_START, ILIACUS_Y_END);
        double leftIliacus = measureMuscleThicknessInRegion(
            huMap, width, height, pixSpacingY,
            ILIACUS_X_LEFT_START, ILIACUS_X_LEFT_END, ILIACUS_Y_START, ILIACUS_Y_END);

        double rightRectus = measureMuscleThicknessInRegion(
            huMap, width, height, pixSpacingX,
            RECTUS_X_RIGHT_START, RECTUS_X_RIGHT_END, RECTUS_Y_START, RECTUS_Y_END);
        double leftRectus = measureMuscleThicknessInRegion(
            huMap, width, height, pixSpacingX,
            RECTUS_X_LEFT_START, RECTUS_X_LEFT_END, RECTUS_Y_START, RECTUS_Y_END);

        // Lateral muscles - subdivide into 3 layers
        double rightExtOblique = measureLateralLayerThickness(
            huMap, width, height, pixSpacingX,
            LATERAL_X_RIGHT_START, LATERAL_X_RIGHT_END,
            LATERAL_Y_START, LATERAL_Y_END, 0);
        double leftExtOblique = measureLateralLayerThickness(
            huMap, width, height, pixSpacingX,
            LATERAL_X_LEFT_START, LATERAL_X_LEFT_END,
            LATERAL_Y_START, LATERAL_Y_END, 0);

        double rightIntOblique = measureLateralLayerThickness(
            huMap, width, height, pixSpacingX,
            LATERAL_X_RIGHT_START, LATERAL_X_RIGHT_END,
            LATERAL_Y_START, LATERAL_Y_END, 1);
        double leftIntOblique = measureLateralLayerThickness(
            huMap, width, height, pixSpacingX,
            LATERAL_X_LEFT_START, LATERAL_X_LEFT_END,
            LATERAL_Y_START, LATERAL_Y_END, 1);

        double rightTransversus = measureLateralLayerThickness(
            huMap, width, height, pixSpacingX,
            LATERAL_X_RIGHT_START, LATERAL_X_RIGHT_END,
            LATERAL_Y_START, LATERAL_Y_END, 2);
        double leftTransversus = measureLateralLayerThickness(
            huMap, width, height, pixSpacingX,
            LATERAL_X_LEFT_START, LATERAL_X_LEFT_END,
            LATERAL_Y_START, LATERAL_Y_END, 2);

        return new MuscleResults(
            rightPsoas, leftPsoas,
            rightIliacus, leftIliacus,
            rightRectus, leftRectus,
            rightIntOblique, leftIntOblique,
            rightExtOblique, leftExtOblique,
            rightTransversus, leftTransversus
        );
    }

    /**
     * Build a 2D Hounsfield Unit map from the image using pixelToRealValue.
     */
    private int[][] buildHUMap(DicomImageElement image, Mat mat, int width, int height) {
        int[][] huMap = new int[height][width];
        // Convert Mat to 32-bit float for pixel access
        Mat floatMat = new Mat();
        mat.convertTo(floatMat, CvType.CV_32FC1);

        float[] pixelData = new float[1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                floatMat.get(y, x, pixelData);
                int rawPixel = (int) pixelData[0];
                Number huValue = image.pixelToRealValue(rawPixel, null);
                huMap[y][x] = huValue != null ? huValue.intValue() : rawPixel;
            }
        }
        floatMat.release();
        return huMap;
    }

    /**
     * Measure muscle thickness within an anatomical ROI.
     * Scans vertical columns within the region and measures the
     * maximum contiguous run of muscle-HU pixels, converted to mm.
     */
    private double measureMuscleThicknessInRegion(
        int[][] huMap, int width, int height, double pixelSpacing,
        double xStartFrac, double xEndFrac, double yStartFrac, double yEndFrac) {

        int xStart = clamp((int) (xStartFrac * width), 0, width - 1);
        int xEnd = clamp((int) (xEndFrac * width), 0, width - 1);
        int yStart = clamp((int) (yStartFrac * height), 0, height - 1);
        int yEnd = clamp((int) (yEndFrac * height), 0, height - 1);

        double maxThicknessPx = 0;

        for (int x = xStart; x <= xEnd; x++) {
            int currentRun = 0;
            int bestRun = 0;
            for (int y = yStart; y <= yEnd; y++) {
                int hu = huMap[y][x];
                if (hu >= MUSCLE_HU_MIN && hu <= MUSCLE_HU_MAX) {
                    currentRun++;
                    bestRun = Math.max(bestRun, currentRun);
                } else {
                    currentRun = 0;
                }
            }
            maxThicknessPx = Math.max(maxThicknessPx, bestRun);
        }

        if (maxThicknessPx == 0) {
            return Double.NaN;
        }
        return maxThicknessPx * pixelSpacing;
    }

    /**
     * Measure thickness of a specific layer in the lateral abdominal wall.
     * The lateral wall has 3 muscle layers separated by fascial planes (low HU).
     *
     * @param layerIndex 0 = outermost (ext oblique), 1 = middle (int oblique),
     *                   2 = innermost (transversus)
     */
    private double measureLateralLayerThickness(
        int[][] huMap, int width, int height, double pixelSpacing,
        double xStartFrac, double xEndFrac,
        double yStartFrac, double yEndFrac, int layerIndex) {

        int xStart = clamp((int) (xStartFrac * width), 0, width - 1);
        int xEnd = clamp((int) (xEndFrac * width), 0, width - 1);
        int yStart = clamp((int) (yStartFrac * height), 0, height - 1);
        int yEnd = clamp((int) (yEndFrac * height), 0, height - 1);

        double totalThickness = 0;
        int validRows = 0;

        for (int y = yStart; y <= yEnd; y++) {
            int layerCount = 0;
            int currentLayerThickness = 0;
            boolean inMuscle = false;

            boolean isRightSide = xEndFrac < 0.5;
            int scanStart = isRightSide ? xEnd : xStart;
            int scanEnd = isRightSide ? xStart : xEnd;
            int scanDir = isRightSide ? -1 : 1;

            for (int x = scanStart; x != scanEnd + scanDir; x += scanDir) {
                int hu = huMap[y][x];
                boolean isMuscle = hu >= MUSCLE_HU_MIN && hu <= MUSCLE_HU_MAX;

                if (isMuscle && !inMuscle) {
                    inMuscle = true;
                    currentLayerThickness = 1;
                } else if (isMuscle) {
                    currentLayerThickness++;
                } else if (inMuscle) {
                    if (currentLayerThickness >= 3) {
                        if (layerCount == layerIndex) {
                            totalThickness += currentLayerThickness;
                            validRows++;
                        }
                        layerCount++;
                    }
                    inMuscle = false;
                    currentLayerThickness = 0;
                }
            }
            if (inMuscle && currentLayerThickness >= 3) {
                if (layerCount == layerIndex) {
                    totalThickness += currentLayerThickness;
                    validRows++;
                }
            }
        }

        if (validRows == 0) {
            return Double.NaN;
        }
        double avgThicknessPx = totalThickness / validRows;
        return avgThicknessPx * pixelSpacing;
    }

    private double[] getPixelSpacing(DicomImageElement image) {
        double[] spacing = {1.0, 1.0};
        double[] ps = TagD.getTagValue(image, Tag.PixelSpacing, double[].class);
        if (ps != null && ps.length >= 2) {
            spacing[0] = ps[1]; // column spacing = X
            spacing[1] = ps[0]; // row spacing = Y
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

    private MuscleResults emptyResults() {
        return new MuscleResults(
            Double.NaN, Double.NaN, Double.NaN, Double.NaN,
            Double.NaN, Double.NaN, Double.NaN, Double.NaN,
            Double.NaN, Double.NaN, Double.NaN, Double.NaN
        );
    }
}
