package org.weasis.dicom.hernia;

/**
 * Hernia-specific window/level presets for CT imaging.
 *
 * Provides optimized viewing presets for hernia evaluation including
 * mesh visualization, soft tissue detail, and abdominal wall assessment.
 */
public final class HerniaPresets {

    private HerniaPresets() {
        // utility class
    }

    /**
     * Preset for mesh visualization.
     * Narrow window centered on typical mesh HU values (metal/synthetic).
     * Polypropylene mesh: ~100-200 HU, PTFE: ~100-180 HU.
     */
    public static final String MESH_PRESET_NAME = "Hernia - Mesh";
    public static final double MESH_WINDOW = 300.0;
    public static final double MESH_LEVEL = 150.0;

    /**
     * Preset for abdominal wall soft tissue detail.
     * Optimized to differentiate muscle layers.
     */
    public static final String SOFT_TISSUE_PRESET_NAME = "Hernia - Soft Tissue";
    public static final double SOFT_TISSUE_WINDOW = 350.0;
    public static final double SOFT_TISSUE_LEVEL = 50.0;

    /**
     * Preset for abdominal contents (bowel/fat/fluid differentiation).
     */
    public static final String ABDOMINAL_PRESET_NAME = "Hernia - Abdominal";
    public static final double ABDOMINAL_WINDOW = 400.0;
    public static final double ABDOMINAL_LEVEL = 40.0;

    /**
     * Wide window preset for overall hernia assessment.
     */
    public static final String HERNIA_WIDE_PRESET_NAME = "Hernia - Wide";
    public static final double HERNIA_WIDE_WINDOW = 1500.0;
    public static final double HERNIA_WIDE_LEVEL = -200.0;

    /**
     * Preset optimized for muscle/fat boundary visualization.
     * Useful for measuring muscle thickness.
     */
    public static final String MUSCLE_FAT_PRESET_NAME = "Hernia - Muscle/Fat";
    public static final double MUSCLE_FAT_WINDOW = 250.0;
    public static final double MUSCLE_FAT_LEVEL = 30.0;

    /** All preset names for iteration. */
    public static final String[] ALL_PRESET_NAMES = {
        MESH_PRESET_NAME,
        SOFT_TISSUE_PRESET_NAME,
        ABDOMINAL_PRESET_NAME,
        HERNIA_WIDE_PRESET_NAME,
        MUSCLE_FAT_PRESET_NAME
    };

    /** All preset windows matching ALL_PRESET_NAMES order. */
    public static final double[] ALL_WINDOWS = {
        MESH_WINDOW,
        SOFT_TISSUE_WINDOW,
        ABDOMINAL_WINDOW,
        HERNIA_WIDE_WINDOW,
        MUSCLE_FAT_WINDOW
    };

    /** All preset levels matching ALL_PRESET_NAMES order. */
    public static final double[] ALL_LEVELS = {
        MESH_LEVEL,
        SOFT_TISSUE_LEVEL,
        ABDOMINAL_LEVEL,
        HERNIA_WIDE_LEVEL,
        MUSCLE_FAT_LEVEL
    };
}
