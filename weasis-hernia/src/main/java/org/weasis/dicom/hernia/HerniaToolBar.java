package org.weasis.dicom.hernia;

import javax.swing.JButton;
import javax.swing.JComboBox;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.viewer2d.EventManager;

/**
 * Toolbar providing hernia-specific window/level presets.
 *
 * Adds a combo box for quick switching between mesh, soft tissue,
 * muscle/fat, and other hernia-optimized viewing presets.
 */
public class HerniaToolBar extends WtoolBar {

    public static final String BAR_NAME = "Hernia Presets";

    private final JComboBox<String> presetCombo;

    public HerniaToolBar(int position) {
        super(BAR_NAME, position);

        // Preset selector combo
        presetCombo = new JComboBox<>(HerniaPresets.ALL_PRESET_NAMES);
        presetCombo.setToolTipText("Select hernia-specific window/level preset");
        presetCombo.addActionListener(e -> applySelectedPreset());
        add(presetCombo);

        // Quick apply button
        JButton btnApply = new JButton("Apply W/L");
        btnApply.setToolTipText("Apply the selected hernia W/L preset");
        btnApply.addActionListener(e -> applySelectedPreset());
        add(btnApply);
    }

    private void applySelectedPreset() {
        int idx = presetCombo.getSelectedIndex();
        if (idx < 0 || idx >= HerniaPresets.ALL_WINDOWS.length) {
            return;
        }

        double window = HerniaPresets.ALL_WINDOWS[idx];
        double level = HerniaPresets.ALL_LEVELS[idx];

        // Apply W/L to the currently selected view
        EventManager mgr = EventManager.getInstance();
        mgr.getAction(ActionW.WINDOW).ifPresent(a -> a.setRealValue(window));
        mgr.getAction(ActionW.LEVEL).ifPresent(a -> a.setRealValue(level));
    }
}
