package org.weasis.dicom.hernia;

import bibliothek.gui.dock.common.CLocation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.viewer2d.EventManager;

/**
 * HerniaTool - Main hernia evaluation panel for Weasis.
 *
 * Provides auto-readouts for:
 * - Muscle identification and measurements (psoas, iliacus, rectus abdominis,
 *   internal oblique, external oblique, transversus abdominis)
 * - Intra-abdominal and extra-abdominal (hernia sac) volume
 * - Loss of domain calculations
 * - Mesh detection via HU thresholds
 * - Clinical calculators (Carbonell RDR, Tanaka LOD ratio, component separation)
 */
public class HerniaTool extends PluginTool implements SeriesViewerListener {

    public static final String BUTTON_NAME = "Hernia Evaluation";

    private final JScrollPane rootPane = new JScrollPane();
    private final JPanel mainPanel = new JPanel();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    // Muscle measurement display fields
    private final JTextField txtRightPsoas = new JTextField(8);
    private final JTextField txtLeftPsoas = new JTextField(8);
    private final JTextField txtRightIliacus = new JTextField(8);
    private final JTextField txtLeftIliacus = new JTextField(8);
    private final JTextField txtRightRectus = new JTextField(8);
    private final JTextField txtLeftRectus = new JTextField(8);
    private final JTextField txtRightIntOblique = new JTextField(8);
    private final JTextField txtLeftIntOblique = new JTextField(8);
    private final JTextField txtRightExtOblique = new JTextField(8);
    private final JTextField txtLeftExtOblique = new JTextField(8);
    private final JTextField txtRightTransversus = new JTextField(8);
    private final JTextField txtLeftTransversus = new JTextField(8);

    // Volume display fields
    private final JTextField txtIntraAbdominalVol = new JTextField(10);
    private final JTextField txtExtraAbdominalVol = new JTextField(10);
    private final JTextField txtLossOfDomainRatio = new JTextField(10);

    // Calculator display fields
    private final JTextField txtCarbonellRDR = new JTextField(10);
    private final JTextField txtTanakaLOD = new JTextField(10);
    private final JTextField txtComponentSep = new JTextField(10);
    private final JTextField txtHerniaDefectWidth = new JTextField(8);
    private final JTextField txtHerniaDefectHeight = new JTextField(8);

    // Mesh detection fields
    private final JTextField txtMeshDetected = new JTextField(10);
    private final JTextField txtMeshArea = new JTextField(10);
    private final JTextField txtMeshHURange = new JTextField(10);

    // Slice adjustment spinner
    private final JSpinner spinnerSlice = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));

    // Engines
    private final MuscleMeasurementEngine muscleEngine = new MuscleMeasurementEngine();
    private final MeshDetector meshDetector = new MeshDetector();
    private final VolumeCalculator volumeCalculator = new VolumeCalculator();
    private final HerniaCalculators calculators = new HerniaCalculators();

    public HerniaTool(Type type) {
        super(BUTTON_NAME, type, 120);
        setDockableWidth(320);
        rootPane.setBorder(BorderFactory.createEmptyBorder());
        initializeUI();
    }

    private void initializeUI() {
        mainPanel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed pane with measurement panels
        tabbedPane.addTab("Muscles", createMusclePanel());
        tabbedPane.addTab("Volume", createVolumePanel());
        tabbedPane.addTab("Mesh", createMeshPanel());
        tabbedPane.addTab("Calculators", createCalculatorPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom: Analyze button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAnalyze = new JButton("Analyze Current Slice");
        btnAnalyze.addActionListener(e -> analyzeCurrentSlice());
        JButton btnAnalyzeSeries = new JButton("Analyze Full Series");
        btnAnalyzeSeries.addActionListener(e -> analyzeFullSeries());
        bottomPanel.add(btnAnalyze);
        bottomPanel.add(btnAnalyzeSeries);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        rootPane.setViewportView(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel title = new JLabel("HerniaCT Evaluation");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);

        JPanel slicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        slicePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        slicePanel.add(new JLabel("Axial Slice:"));
        spinnerSlice.setPreferredSize(new Dimension(80, 25));
        spinnerSlice.addChangeListener(e -> onSliceChanged());
        slicePanel.add(spinnerSlice);
        header.add(slicePanel);

        return header;
    }

    private JPanel createMusclePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Column headers
        gbc.gridy = 0;
        gbc.gridx = 0;
        panel.add(createBoldLabel("Muscle"), gbc);
        gbc.gridx = 1;
        panel.add(createBoldLabel("Right (mm)"), gbc);
        gbc.gridx = 2;
        panel.add(createBoldLabel("Left (mm)"), gbc);

        // Muscle rows
        addMuscleRow(panel, gbc, 1, "Psoas", txtRightPsoas, txtLeftPsoas);
        addMuscleRow(panel, gbc, 2, "Iliacus", txtRightIliacus, txtLeftIliacus);
        addMuscleRow(panel, gbc, 3, "Rectus Abd.", txtRightRectus, txtLeftRectus);
        addMuscleRow(panel, gbc, 4, "Int. Oblique", txtRightIntOblique, txtLeftIntOblique);
        addMuscleRow(panel, gbc, 5, "Ext. Oblique", txtRightExtOblique, txtLeftExtOblique);
        addMuscleRow(panel, gbc, 6, "Transversus", txtRightTransversus, txtLeftTransversus);

        // Info note
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 4, 2, 4);
        JLabel note = new JLabel("<html><i>Thickness measured at L3-L4 axial level</i></html>");
        note.setFont(note.getFont().deriveFont(10f));
        panel.add(note, gbc);

        // Spacer to push everything to top
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createVolumePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Intra-abdominal volume
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Intra-abdominal Vol:"), gbc);
        gbc.gridx = 1;
        txtIntraAbdominalVol.setEditable(false);
        panel.add(txtIntraAbdominalVol, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("mL"), gbc);

        row++;

        // Extra-abdominal volume (hernia sac)
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Hernia Sac Vol:"), gbc);
        gbc.gridx = 1;
        txtExtraAbdominalVol.setEditable(false);
        panel.add(txtExtraAbdominalVol, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("mL"), gbc);

        row++;

        // Separator
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        panel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        gbc.gridwidth = 1;

        row++;

        // Loss of Domain Ratio
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(createBoldLabel("Loss of Domain:"), gbc);
        gbc.gridx = 1;
        txtLossOfDomainRatio.setEditable(false);
        panel.add(txtLossOfDomainRatio, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("%"), gbc);

        row++;

        // Info
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 4, 2, 4);
        JLabel info = new JLabel(
            "<html><i>LOD = Hernia Sac Vol / (Abdominal Vol + Hernia Sac Vol) x 100</i></html>");
        info.setFont(info.getFont().deriveFont(10f));
        panel.add(info, gbc);

        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createMeshPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(createBoldLabel("Mesh Detection"), gbc);

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Mesh Detected:"), gbc);
        gbc.gridx = 1;
        txtMeshDetected.setEditable(false);
        panel.add(txtMeshDetected, gbc);

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Mesh Area:"), gbc);
        gbc.gridx = 1;
        txtMeshArea.setEditable(false);
        panel.add(txtMeshArea, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("mm\u00B2"), gbc);

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("HU Range:"), gbc);
        gbc.gridx = 1;
        txtMeshHURange.setEditable(false);
        txtMeshHURange.setText(MeshDetector.HU_MIN + " to " + MeshDetector.HU_MAX);
        panel.add(txtMeshHURange, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("HU"), gbc);

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 4, 2, 4);
        JLabel info = new JLabel(
            "<html><i>Mesh identified by HU thresholds in anterior<br>"
                + "abdominal wall region (polypropylene/PTFE range)</i></html>");
        info.setFont(info.getFont().deriveFont(10f));
        panel.add(info, gbc);

        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createCalculatorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Input section
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(createBoldLabel("Defect Measurements"), gbc);
        gbc.gridwidth = 1;

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Defect Width (cm):"), gbc);
        gbc.gridx = 1;
        txtHerniaDefectWidth.setText("0.0");
        panel.add(txtHerniaDefectWidth, gbc);

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Defect Height (cm):"), gbc);
        gbc.gridx = 1;
        txtHerniaDefectHeight.setText("0.0");
        panel.add(txtHerniaDefectHeight, gbc);

        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnCalc = new JButton("Recalculate");
        btnCalc.addActionListener(e -> recalculateClinical());
        panel.add(btnCalc, gbc);
        gbc.gridwidth = 1;

        row++;

        // Results separator
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        panel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        gbc.gridwidth = 1;

        row++;

        // Carbonell RDR
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Carbonell RDR:"), gbc);
        gbc.gridx = 1;
        txtCarbonellRDR.setEditable(false);
        panel.add(txtCarbonellRDR, gbc);

        row++;

        // Tanaka LOD
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Tanaka LOD (%):"), gbc);
        gbc.gridx = 1;
        txtTanakaLOD.setEditable(false);
        panel.add(txtTanakaLOD, gbc);

        row++;

        // Component separation
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel("Comp. Sep. Needed:"), gbc);
        gbc.gridx = 1;
        txtComponentSep.setEditable(false);
        panel.add(txtComponentSep, gbc);

        row++;

        // Info
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 4, 2, 4);
        JLabel info = new JLabel(
            "<html><i>"
                + "Carbonell RDR = (R Rectus + L Rectus) / Defect Width<br>"
                + "RDR &lt; 2 suggests need for myofascial release<br>"
                + "Tanaka LOD = Hernia Vol / (Abd Vol + Hernia Vol) x 100<br>"
                + "LOD &gt; 20% = significant loss of domain"
                + "</i></html>");
        info.setFont(info.getFont().deriveFont(10f));
        panel.add(info, gbc);

        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private void addMuscleRow(JPanel panel, GridBagConstraints gbc,
                              int row, String name, JTextField right, JTextField left) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(name), gbc);
        gbc.gridx = 1;
        right.setEditable(false);
        panel.add(right, gbc);
        gbc.gridx = 2;
        left.setEditable(false);
        panel.add(left, gbc);
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    /**
     * Analyze the current axial slice for muscles and mesh.
     */
    private void analyzeCurrentSlice() {
        ViewCanvas<DicomImageElement> view =
            EventManager.getInstance().getSelectedViewPane();
        if (view == null || view.getSeries() == null) {
            return;
        }

        DicomImageElement image = view.getImage();
        if (image == null) {
            return;
        }

        // Run muscle measurement
        MuscleMeasurementEngine.MuscleResults results =
            muscleEngine.measureMuscles(image);
        updateMuscleDisplay(results);

        // Run mesh detection
        MeshDetector.MeshResult meshResult = meshDetector.detectMesh(image);
        updateMeshDisplay(meshResult);

        // Update calculators with current measurements
        recalculateClinical();
    }

    /**
     * Analyze the full series for volume calculations.
     */
    private void analyzeFullSeries() {
        MediaSeries<DicomImageElement> series =
            EventManager.getInstance().getSelectedSeries();
        if (series == null) {
            return;
        }

        // Calculate volumes
        VolumeCalculator.VolumeResult volResult =
            volumeCalculator.calculateVolumes(series);
        updateVolumeDisplay(volResult);

        // Also analyze current slice
        analyzeCurrentSlice();
    }

    private void updateMuscleDisplay(MuscleMeasurementEngine.MuscleResults results) {
        if (results == null) {
            return;
        }
        txtRightPsoas.setText(formatMm(results.rightPsoasThickness()));
        txtLeftPsoas.setText(formatMm(results.leftPsoasThickness()));
        txtRightIliacus.setText(formatMm(results.rightIliacusThickness()));
        txtLeftIliacus.setText(formatMm(results.leftIliacusThickness()));
        txtRightRectus.setText(formatMm(results.rightRectusThickness()));
        txtLeftRectus.setText(formatMm(results.leftRectusThickness()));
        txtRightIntOblique.setText(formatMm(results.rightIntObliqueThickness()));
        txtLeftIntOblique.setText(formatMm(results.leftIntObliqueThickness()));
        txtRightExtOblique.setText(formatMm(results.rightExtObliqueThickness()));
        txtLeftExtOblique.setText(formatMm(results.leftExtObliqueThickness()));
        txtRightTransversus.setText(formatMm(results.rightTransversusThickness()));
        txtLeftTransversus.setText(formatMm(results.leftTransversusThickness()));
    }

    private void updateVolumeDisplay(VolumeCalculator.VolumeResult result) {
        if (result == null) {
            return;
        }
        txtIntraAbdominalVol.setText(String.format("%.1f", result.intraAbdominalVolumeMl()));
        txtExtraAbdominalVol.setText(String.format("%.1f", result.extraAbdominalVolumeMl()));
        txtLossOfDomainRatio.setText(String.format("%.1f", result.lossOfDomainPercent()));
    }

    private void updateMeshDisplay(MeshDetector.MeshResult result) {
        if (result == null) {
            return;
        }
        txtMeshDetected.setText(result.meshDetected() ? "Yes" : "No");
        txtMeshArea.setText(String.format("%.1f", result.meshAreaMm2()));
    }

    private void recalculateClinical() {
        try {
            double defectWidth = Double.parseDouble(txtHerniaDefectWidth.getText());
            double rightRectus = parseMm(txtRightRectus.getText());
            double leftRectus = parseMm(txtLeftRectus.getText());
            double intraVol = parseVolume(txtIntraAbdominalVol.getText());
            double extraVol = parseVolume(txtExtraAbdominalVol.getText());

            // Carbonell RDR
            double rdr = calculators.carbonellRDR(rightRectus, leftRectus, defectWidth * 10);
            txtCarbonellRDR.setText(Double.isNaN(rdr) ? "N/A" : String.format("%.2f", rdr));

            // Tanaka LOD
            double lod = calculators.tanakaLossOfDomain(intraVol, extraVol);
            txtTanakaLOD.setText(Double.isNaN(lod) ? "N/A" : String.format("%.1f", lod));

            // Component separation likelihood
            String csl = calculators.componentSeparationLikelihood(
                defectWidth, rdr, lod);
            txtComponentSep.setText(csl);
        } catch (NumberFormatException e) {
            // Input not yet valid
        }
    }

    private void onSliceChanged() {
        // Navigate to the selected slice and re-analyze
        int sliceIndex = (int) spinnerSlice.getValue();
        ViewCanvas<DicomImageElement> view =
            EventManager.getInstance().getSelectedViewPane();
        if (view != null) {
            ActionW scrollAction = ActionW.SCROLL_SERIES;
            EventManager.getInstance().getAction(scrollAction)
                .ifPresent(a -> a.setSliderValue(sliceIndex));
        }
    }

    private String formatMm(double value) {
        if (Double.isNaN(value)) {
            return "N/A";
        }
        return String.format("%.1f", value);
    }

    private double parseMm(String text) {
        if (text == null || text.isEmpty() || "N/A".equals(text)) {
            return Double.NaN;
        }
        return Double.parseDouble(text);
    }

    private double parseVolume(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text);
    }

    @Override
    public Component getToolComponent() {
        return getToolComponentFromJScrollPane(rootPane);
    }

    @Override
    protected void changeToolWindowAnchor(CLocation clocation) {
        // No anchor-specific behavior needed
    }

    @Override
    public void changingViewContentEvent(SeriesViewerEvent event) {
        if (event.getEventType() == SeriesViewerEvent.EVENT.SELECT_VIEW
            || event.getEventType() == SeriesViewerEvent.EVENT.LAYOUT) {
            // Could auto-analyze on slice change if desired
        }
    }
}
