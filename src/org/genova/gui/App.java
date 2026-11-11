package org.genova.gui;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public final class App extends JFrame {
    private DataTable data;
    private Results results;
    private Design.Prepared completedDesign;
    private String completedDescription, completedEngine;
    private final Map<AnalysisMode, JRadioButton> modes = new EnumMap<>(AnalysisMode.class);
    private final JComboBox<String> score = new JComboBox<>(), object = new JComboBox<>();
    private final JComboBox<String> urReplicate = new JComboBox<>(), urCrossed = new JComboBox<>(), urGroup = new JComboBox<>();
    private final JComboBox<String> mVariable = new JComboBox<>(), mGroup = new JComboBox<>(), mItem = new JComboBox<>();
    private final JTextField mDItems = new JTextField(), mWeights = new JTextField(), mAWeights = new JTextField();
    private final JTextArea mVariableOrder = text("Import data to see the fixed-variable order.");
    private final JRadioButton mPxi = new JRadioButton("P × I · items nested in variable",true);
    private final JRadioButton mPir = new JRadioButton("P × (I:R) · shared R groups");
    private final JPanel mGroupField = field("Shared random group (R)",mGroup);
    private final JPanel mDItemsField = field("D-study item counts (optional)",mDItems);
    private final JTextArea mDesignInfo = text("P × I: exactly four columns. Every person answers every item within each fixed variable. D-study counts and optional w/a weights follow displayed variable order.");
    private final JRadioButton urOneWay = new JRadioButton("One-way · nested replicates", true);
    private final JRadioButton urTwoWay = new JRadioButton("Two-way · crossed + nested replicates");
    private final JRadioButton urNestedCross = new JRadioButton("Nested cross · (P:C) × (I:R)");
    private final JRadioButton urCrossedMissing = new JRadioButton("Crossed P × I · missing cells");
    private final JRadioButton urNestedMissing = new JRadioButton("Nested P × (I:H) · missing cells");
    private final JRadioButton urMatrixSampling = new JRadioButton("Matrix sampling · (P × I):R");
    private final JPanel urCrossedField = field("Crossed random facet (P)", urCrossed);
    private final JPanel urGroupField = field("Object grouping facet (C)", urGroup);
    private final JPanel urReplicateField = field("Replicate unit nested within object/cell", urReplicate);
    private final JTextArea urDesignInfo = text("One-way pilot: exactly three columns (object, nested replicate, score). Unequal replicate counts are allowed; crossed items and D-studies are not.");
    private final JPanel facets = new JPanel();
    private final JPanel genovaControls = vertical();
    private final JPanel urControls = vertical();
    private final JPanel mControls = vertical();
    private final JTextArea modeInfo = text(AnalysisMode.GENOVA.description());
    private final List<FacetControls> controls = new ArrayList<>();
    private final JCheckBox dStudy = new JCheckBox("Run D-study", true);
    private final JLabel source = new JLabel("No data loaded"), status = new JLabel("Import a table or load the example data."), resultLabel = new JLabel("No analysis yet");
    private final JButton run = new JButton("Run analysis"), workbookSave = new JButton("Export results (Excel)"), export = new JButton("Export table (CSV)"), reportSave = new JButton("Save report (TXT)");
    private final JTabbedPane tabs = new JTabbedPane();
    private final SurfacePanel surface = new SurfacePanel(this::generateHeatmap, this::exportSurface);
    private final HeatmapPanel heatmap = new HeatmapPanel(this::generateHeatmap, this::exportHeatmap, surface::setGrid);
    private final JTable preview = table(), anova = table(), components = table(), decisions = table();
    private final JTextArea report = new JTextArea(), notice = new JTextArea();
    private final JPanel settings = new JPanel(new BorderLayout());
    private JScrollPane leftScroll;
    private final JProgressBar progress = new JProgressBar();
    private final List<JButton> inputButtons = new ArrayList<>();
    private JButton exampleButton, urEmptyExample;
    private boolean settingUp;
    private static final Color INK = new Color(27,45,62), MUTED = new Color(94,108,123), BG = new Color(246,248,250);

    private record FacetControls(String column, JCheckBox include, JComboBox<String> kind,
                                  JComboBox<String> parent, JTextField sizes) {}

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.application.name", "GENOVA Studio");
        Locale.setDefault(Locale.ENGLISH);
        for (int i = 0; i + 1 < args.length; i++) {
            if ("--demo-dir".equals(args[i])) System.setProperty("genova.demo.dir", args[i + 1]);
        }
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            if (System.getProperty("genova.demo.file") != null || System.getProperty("genova.demo.manual") != null)
                app.configureDemoPresentation();
            app.setVisible(true);
            if (Arrays.asList(args).contains("--demo")) app.sample();
            if (System.getProperty("genova.demo.file") != null) {
                javax.swing.Timer timer = new javax.swing.Timer(700, e -> { ((javax.swing.Timer)e.getSource()).stop(); app.runAutomatedDemo(); });
                timer.setRepeats(false); timer.start();
            }
        });
    }

    public App() {
        super("GENOVA Studio · Mac Preview");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1240, 860); setMinimumSize(new Dimension(1060, 720)); setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0,16)); root.setBackground(BG); root.setBorder(new EmptyBorder(20,24,16,24)); setContentPane(root);
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("GENOVA Studio"); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28)); title.setForeground(INK);
        JPanel brand = vertical(); brand.add(title); brand.add(label("Generalizability theory · from data to design", 13, MUTED)); header.add(brand, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,6)); actions.setOpaque(false);
        actions.add(button("Import CSV / Excel", this::importFile)); actions.add(button("Paste table", this::paste));
        exampleButton = button("Example data", this::sample); actions.add(exampleButton);
        header.add(actions, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);

        JPanel left = new ScrollPanel(); left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS)); left.setBorder(new EmptyBorder(4,4,8,12));
        left.add(label("01  Data and design", 19, INK)); left.add(Box.createVerticalStrut(8));
        JPanel modeChoices = vertical();
        ButtonGroup modeGroup = new ButtonGroup();
        for (AnalysisMode choice : AnalysisMode.values()) {
            String shortLabel = switch (choice) {
                case GENOVA -> "GENOVA · balanced G/D · ready";
                case UR_GENOVA -> "urGENOVA · unbalanced G · pilot";
                case M_GENOVA -> "mGENOVA · multivariate G/D · pilot";
            };
            JRadioButton option = new JRadioButton(shortLabel);
            option.setToolTipText(choice.description());
            option.setOpaque(false);
            option.setAlignmentX(LEFT_ALIGNMENT);
            option.addActionListener(e -> updateMode());
            if (choice == AnalysisMode.GENOVA) option.setSelected(true);
            modeGroup.add(option);
            modeChoices.add(option);
            modes.put(choice, option);
        }
        JPanel modeField = vertical();
        modeField.add(label("Analysis type", 12, MUTED));
        modeField.add(Box.createVerticalStrut(3));
        modeField.add(modeChoices);
        modeField.add(Box.createVerticalStrut(7));
        left.add(modeField);
        left.add(modeInfo); left.add(Box.createVerticalStrut(9));
        source.setForeground(MUTED); source.setAlignmentX(LEFT_ALIGNMENT); left.add(source);
        JPanel sharedControls = vertical();
        sharedControls.add(Box.createVerticalStrut(12)); sharedControls.add(field("Score column", score)); sharedControls.add(field("Object of measurement (e.g., person)", object));
        left.add(sharedControls);
        genovaControls.add(Box.createVerticalStrut(12));
        JTextArea intro = text("Each row is one observation. Select the facets to analyze and specify their crossed or nested relationships. Random facets support inference beyond the observed levels; fixed facets do not.");
        genovaControls.add(intro); genovaControls.add(Box.createVerticalStrut(10));
        facets.setLayout(new BoxLayout(facets, BoxLayout.Y_AXIS)); facets.setOpaque(false); facets.setAlignmentX(LEFT_ALIGNMENT); genovaControls.add(facets);
        genovaControls.add(Box.createVerticalStrut(14)); dStudy.setOpaque(false); dStudy.setAlignmentX(LEFT_ALIGNMENT); genovaControls.add(dStudy);
        genovaControls.add(text("Leave D-study levels blank to use the observed counts. Enter values such as 1,2,3,4 to compare scenarios. Lists pair by position; a single value applies to all scenarios. For a nested facet, enter levels per parent group."));
        genovaControls.add(Box.createVerticalStrut(10));
        genovaControls.add(text("Current scope: complete, balanced univariate designs with single-parent nesting or nesting chains. For nesting chains of two or more levels, run a G-study only; D-study is blocked pending independent validation. The object of measurement cannot be nested. Missing or duplicate cells are not filled or averaged."));
        left.add(genovaControls);
        urControls.add(Box.createVerticalStrut(12));
        JPanel urChoices = vertical();
        ButtonGroup urDesignGroup = new ButtonGroup();
        for (JRadioButton option : List.of(urOneWay, urTwoWay, urNestedCross, urCrossedMissing, urNestedMissing, urMatrixSampling)) {
            option.setOpaque(false); option.setAlignmentX(LEFT_ALIGNMENT);
            option.addActionListener(e -> updateUrDesign());
            urDesignGroup.add(option); urChoices.add(option);
        }
        urControls.add(label("Unbalanced design", 12, MUTED));
        urControls.add(urChoices);
        urControls.add(Box.createVerticalStrut(12));
        urCrossedField.setVisible(false); urControls.add(urCrossedField);
        urGroupField.setVisible(false); urControls.add(urGroupField);
        urControls.add(urReplicateField);
        urControls.add(urDesignInfo);
        urEmptyExample = button("Load empty-cell example", () -> loadUrTwoWayExample(true));
        urEmptyExample.setAlignmentX(LEFT_ALIGNMENT);
        urEmptyExample.setVisible(false);
        urControls.add(Box.createVerticalStrut(8)); urControls.add(urEmptyExample);
        urControls.setVisible(false); left.add(urControls);
        mControls.add(Box.createVerticalStrut(12));
        ButtonGroup mDesignGroup=new ButtonGroup();
        for(JRadioButton option:List.of(mPxi,mPir)){
            option.setOpaque(false);option.setAlignmentX(LEFT_ALIGNMENT);
            option.addActionListener(e->updateMDesign());mDesignGroup.add(option);mControls.add(option);
        }
        mControls.add(Box.createVerticalStrut(10));
        mControls.add(field("Fixed variable (V)",mVariable));
        mControls.add(mVariableOrder);
        mGroupField.setVisible(false);mControls.add(mGroupField);
        mControls.add(field("Item (I) nested in variable (and R, if selected)",mItem));
        mDItems.setToolTipText("One positive count per fixed variable, in the order first seen in the imported table.");
        mControls.add(mDItemsField);
        mWeights.setToolTipText("Optional universe-score weights w, one number per fixed variable in displayed order; e.g. 0.33333,0.33333,0.33333.");
        mControls.add(field("Universe weights w (optional)",mWeights));
        mAWeights.setToolTipText("Optional estimation weights a in displayed variable order. Leave blank to use a=w. When a differs from w, composite results are MSEs, not G/Phi coefficients.");
        mControls.add(field("Estimation weights a (optional; blank = w)",mAWeights));
        mControls.add(mDesignInfo);
        mControls.setVisible(false); left.add(mControls);
        left.add(Box.createVerticalGlue());
        leftScroll = new JScrollPane(left); leftScroll.setBorder(null); leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        settings.add(leftScroll);

        JPanel right = new JPanel(new BorderLayout(0,8)); right.setOpaque(false);
        resultLabel.setForeground(MUTED); right.add(resultLabel, BorderLayout.NORTH);
        tabs.addTab("Data preview", new JScrollPane(preview)); tabs.addTab("ANOVA", new JScrollPane(anova));
        tabs.addTab("Variance components", new JScrollPane(components)); tabs.addTab("D-study", new JScrollPane(decisions));
        tabs.addTab("D-study heatmap", heatmap);
        tabs.addTab("3D plot", surface);
        report.setEditable(false); report.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); report.setMargin(new Insets(12,12,12,12));
        tabs.addTab("Full report", new JScrollPane(report)); tabs.addTab("Help", new JScrollPane(help())); right.add(tabs, BorderLayout.CENTER);
        notice.setEditable(false); notice.setLineWrap(true); notice.setWrapStyleWord(true); notice.setOpaque(false); notice.setForeground(new Color(137,84,15)); notice.setRows(3);
        right.add(notice, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settings, right); split.setDividerLocation(365); split.setResizeWeight(0); split.setBorder(null); root.add(split, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10,6)); footer.setOpaque(false);
        JPanel state = new JPanel(new BorderLayout(8,4)); state.setOpaque(false); status.setForeground(MUTED); state.add(status, BorderLayout.CENTER);
        progress.setIndeterminate(true); progress.setPreferredSize(new Dimension(70,12)); progress.setVisible(false); state.add(progress, BorderLayout.WEST); footer.add(state, BorderLayout.CENTER);
        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); footerActions.setOpaque(false);
        footerActions.add(workbookSave); footerActions.add(export); footerActions.add(reportSave); footerActions.add(run); footer.add(footerActions, BorderLayout.EAST); root.add(footer, BorderLayout.SOUTH);
        run.setEnabled(false); workbookSave.setEnabled(false); export.setEnabled(false); reportSave.setEnabled(false);
        score.setEnabled(false); object.setEnabled(false); urReplicate.setEnabled(false); urCrossed.setEnabled(false); urGroup.setEnabled(false); mVariable.setEnabled(false); mGroup.setEnabled(false); mItem.setEnabled(false);
        score.setToolTipText("Import or paste data to choose a score column.");
        object.setToolTipText("Import or paste data to choose the object of measurement.");
        urReplicate.setToolTipText("Choose the observation ID nested within each object or crossed cell.");
        urCrossed.setToolTipText("Choose the random facet crossed with the object of measurement.");
        urGroup.setToolTipText("Choose the group containing each object P; P IDs may be reused in different C groups.");
        score.addActionListener(e -> { if (!settingUp) { rebuildFacets(); rebuildUrOptions(); rebuildMOptions(); } });
        object.addActionListener(e -> { if (!settingUp) { rebuildFacets(); rebuildUrOptions(); rebuildMOptions(); } });
        urReplicate.addActionListener(e -> { if (!settingUp) rebuildUrCrossedOptions(); });
        urCrossed.addActionListener(e -> { if (!settingUp) invalidateResults(); });
        urGroup.addActionListener(e -> { if (!settingUp) invalidateResults(); });
        mVariable.addActionListener(e -> { if (!settingUp) rebuildMGroupOptions(); });
        mGroup.addActionListener(e -> { if (!settingUp) rebuildMItemOptions(); });
        mItem.addActionListener(e -> { if (!settingUp) invalidateResults(); });
        mDItems.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
        });
        mWeights.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
        });
        mAWeights.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
        });
        dStudy.addActionListener(e -> invalidateResults()); run.addActionListener(e -> analyze()); workbookSave.addActionListener(e -> saveWorkbook()); export.addActionListener(e -> exportTable()); reportSave.addActionListener(e -> saveReport());
        tabs.addChangeListener(e -> export.setEnabled(exportableTab()));
    }

    /** Keep the public demonstration focused on the production GENOVA workflow. */
    private void configureDemoPresentation() {
        JRadioButton ur = modes.get(AnalysisMode.UR_GENOVA);
        JRadioButton m = modes.get(AnalysisMode.M_GENOVA);
        if (ur != null) ur.setVisible(false);
        if (m != null) m.setVisible(false);
    }

    private static JLabel label(String s, int size, Color c) { JLabel l = new JLabel(s); l.setFont(new Font(Font.SANS_SERIF, Font.BOLD,size)); l.setForeground(c); l.setAlignmentX(LEFT_ALIGNMENT); return l; }
    private static JPanel vertical() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT); return p; }
    private static JTextArea text(String s) { JTextArea t = new JTextArea(s); t.setEditable(false); t.setLineWrap(true); t.setWrapStyleWord(true); t.setOpaque(false); t.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12)); t.setForeground(MUTED); t.setAlignmentX(LEFT_ALIGNMENT); t.setColumns(25); return t; }
    private static JPanel field(String name, JComponent component) { JPanel p = vertical(); p.add(label(name,12,MUTED)); p.add(Box.createVerticalStrut(3)); component.setAlignmentX(LEFT_ALIGNMENT); component.setMaximumSize(new Dimension(Integer.MAX_VALUE,30)); p.add(component); p.add(Box.createVerticalStrut(7)); return p; }
    private JButton button(String text, Runnable action) { JButton b = new JButton(text); b.addActionListener(e -> action.run()); inputButtons.add(b); return b; }
    private static JTable table() { JTable t = new JTable(); t.setRowHeight(28); t.setShowVerticalLines(false); t.setFillsViewportHeight(true); t.setAutoCreateRowSorter(true); t.setSelectionBackground(new Color(218,235,241)); t.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12)); return t; }
    private static void showTable(JTable table, List<String> headers, List<List<String>> rows) {
        table.setModel(new DefaultTableModel(rows.stream().map(List::toArray).toArray(Object[][]::new), headers.toArray()) { public boolean isCellEditable(int r, int c) { return false; } });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < headers.size(); i++) {
            var column = table.getColumnModel().getColumn(i);
            column.setMinWidth(40);
            int width = switch (headers.get(i)) {
                case "Scenario" -> 70;
                case "Facet levels" -> 160;
                case "Variable" -> 140;
                case "Universe σ²" -> 110;
                case "Relative error σ²δ", "Absolute error σ²Δ" -> 120;
                case "Relative error / MSE", "Absolute error / MSE" -> 140;
                case "G coefficient" -> 110;
                case "Φ" -> 105;
                case "df", "SE" -> 70;
                case "Type" -> 135;
                default -> i == 0 ? 175 : 105;
            };
            column.setPreferredWidth(width);
        }
    }
    private void load(DataTable t, String filename) {
        data = t; settingUp = true; mDItems.setText(""); mWeights.setText(""); mAWeights.setText(""); score.removeAllItems(); object.removeAllItems();
        t.headers().forEach(h -> { score.addItem(h); object.addItem(h); });
        score.setSelectedItem(t.headers().get(t.headers().size()-1)); object.setSelectedItem(t.headers().get(0));
        for (String h : t.headers()) {
            if (List.of("score","value","rating","得分","分数").contains(h.toLowerCase(Locale.ROOT))) score.setSelectedItem(h);
            if (List.of("person","student","participant","人员","学生").contains(h.toLowerCase(Locale.ROOT))) object.setSelectedItem(h);
        }
        settingUp = false; rebuildFacets(); rebuildUrOptions(); rebuildMOptions();
        score.setEnabled(true); object.setEnabled(true); urReplicate.setEnabled(true); urCrossed.setEnabled(true); urGroup.setEnabled(true); mVariable.setEnabled(true); mGroup.setEnabled(true); mItem.setEnabled(true);
        source.setText(t.rows().size()+" rows · "+t.headers().size()+" columns"); source.setToolTipText(filename);
        showTable(preview,t.headers(),t.rows().subList(0,Math.min(500,t.rows().size()))); tabs.setSelectedIndex(0);
        status.setText("Loaded. Previewing the first 500 rows. Confirm column roles before running.");
        run.setEnabled(selectedMode().available());
        if (!selectedMode().available()) status.setText("Loaded for preview. " + selectedMode() + " computation is under development.");
    }
    private AnalysisMode selectedMode() {
        for (AnalysisMode choice : AnalysisMode.values()) if (modes.get(choice).isSelected()) return choice;
        return AnalysisMode.GENOVA;
    }
    private void updateMode() {
        AnalysisMode selected = selectedMode();
        if (selected == null) return;
        invalidateResults();
        modeInfo.setText(selected.description());
        genovaControls.setVisible(selected == AnalysisMode.GENOVA);
        urControls.setVisible(selected == AnalysisMode.UR_GENOVA);
        mControls.setVisible(selected == AnalysisMode.M_GENOVA);
        exampleButton.setEnabled(true);
        run.setEnabled(selected.available() && data != null);
        tabs.setTitleAt(3, selected == AnalysisMode.UR_GENOVA ? "D-study (N/A)" : "D-study");
        resultLabel.setText(selected.available() ? "No analysis yet" : selected + " · analysis not yet available");
        status.setText(selected.available()
            ? (data == null ? "Import a table or load the example data." : "Confirm the design, then run the analysis.")
            : "This computation module is under development. Imported data can be previewed.");
        settings.revalidate(); settings.repaint();
    }
    private void updateMDesign() {
        mGroupField.setVisible(mPir.isSelected());
        ((JLabel)mDItemsField.getComponent(0)).setText(mPir.isSelected()
            ? "D-study I counts by variable and R group (optional)" : "D-study item counts (optional)");
        mDItems.setToolTipText(mPir.isSelected()
            ? "Separate variables with semicolons and R-group counts with commas; e.g. 3,6,3;4,3,3."
            : "One positive count per fixed variable, in the displayed order.");
        mDesignInfo.setText(mPir.isSelected()
            ? "P × (I:R): exactly five columns. P and R are shared across fixed variables, while I is nested in both R and variable. Every person answers every item. Leave D-study counts blank for observed counts; otherwise enter one comma-separated R-count row per variable, separated by semicolons. Optional w and a weights follow variable order."
            : "P × I: exactly four columns. Every person answers every item within each fixed variable. Leave D-study counts blank for observed items. Optional w and a weights follow the displayed variable order.");
        mDItems.setText("");mWeights.setText("");mAWeights.setText("");
        rebuildMGroupOptions();
        invalidateResults();settings.revalidate();settings.repaint();
    }
    private void updateUrDesign() {
        urCrossedField.setVisible(urTwoWay.isSelected() || urNestedCross.isSelected() || urNestedMissing.isSelected() || urMatrixSampling.isSelected());
        urGroupField.setVisible(urNestedCross.isSelected());
        urEmptyExample.setVisible(urTwoWay.isSelected());
        ((JLabel)urCrossedField.getComponent(0)).setText(urNestedCross.isSelected() ? "Crossed grouping facet (R)"
            : urMatrixSampling.isSelected() ? "Matrix-sampling group (R)"
            : urNestedMissing.isSelected() ? "Grouping facet (H)" : "Crossed random facet (P)");
        ((JLabel)urReplicateField.getComponent(0)).setText(urNestedCross.isSelected() ? "Facet I nested within R"
            : urMatrixSampling.isSelected() ? "Facet I within R"
            : urNestedMissing.isSelected() ? "Facet I nested within H"
            : urCrossedMissing.isSelected() ? "Crossed facet (I)" : "Replicate unit nested within object/cell");
        urCrossed.setToolTipText(urNestedCross.isSelected() ? "Choose R, whose nested I facets cross the objects P."
            : urMatrixSampling.isSelected() ? "Choose matrix-sampling group R; P and I are each nested within R."
            : urNestedMissing.isSelected() ? "Choose H, whose nested I facets cross the objects P." : "Choose the random facet crossed with the object of measurement.");
        urReplicate.setToolTipText(urNestedCross.isSelected() ? "Choose I, nested within each R group; I labels may be reused in another R group."
            : urMatrixSampling.isSelected() ? "Choose I within each R group; I labels may be reused in another R group."
            : urNestedMissing.isSelected() ? "Choose I, nested within each H group; I labels may be reused in another H group."
            : urCrossedMissing.isSelected() ? "Choose I, whose levels are the same crossed units for every object P; absent rows represent missing cells."
            : "Choose the observation ID nested within each object or crossed cell.");
        urDesignInfo.setText(urMatrixSampling.isSelected()
            ? "Matrix-sampling pilot: exactly four columns (group R, object P, facet I, score). P and I are both nested within R; each R group must have a complete P × I table, though group sizes may differ. Matches manual Appendix H; no D-study."
            : urNestedMissing.isSelected()
            ? "Nested missing-cell pilot: exactly four columns (object P, group H, nested facet I, score). Every P × H block needs at least two observed I levels. Absent P × I rows are missing observations, not zero scores. Matches manual Appendix G; no D-study."
            : urCrossedMissing.isSelected()
            ? "Crossed missing-cell pilot: exactly three columns (object P, crossed facet I, score). Each observed P × I cell occurs once; absent rows are missing observations, not zero scores. Continuous scores are accepted. Matches manual Appendix F; no D-study."
            : urNestedCross.isSelected()
            ? "Nested-cross random pilot: exactly five columns (object P, group C, crossed group R, nested facet I, score). Every P × I cell must occur once. Unequal P-per-C and I-per-R counts are allowed. Matches manual Appendix A; no D-study."
            : urTwoWay.isSelected()
            ? "Two-way random pilot: exactly four columns (crossed facet P, object H, replicate ID, score). Replicates are nested in each P × H cell; absent rows may leave an empty cell. This design matches manual Appendices D/E, but other configurations and original-executable parity are not fully verified. No D-study."
            : "One-way random pilot: exactly three columns (object, nested replicate ID, score). Unequal replicate counts are allowed. Replicate labels reused across objects are treated as distinct nested units. Do not use this option for crossed items or a D-study.");
        invalidateResults();
        status.setText(data == null ? "Import a table or load the example data." : "Confirm the selected unbalanced design and column roles, then run the G-study.");
        settings.revalidate(); settings.repaint();
    }
    private void rebuildUrOptions() {
        if (data == null) return;
        String previous = (String) urReplicate.getSelectedItem();
        String previousGroup = (String) urGroup.getSelectedItem();
        settingUp = true;
        urReplicate.removeAllItems();
        urGroup.removeAllItems();
        for (String column : data.headers())
            if (!column.equals(score.getSelectedItem()) && !column.equals(object.getSelectedItem())) {
                urReplicate.addItem(column); urGroup.addItem(column);
            }
        if (previousGroup != null && data.headers().contains(previousGroup) &&
            !previousGroup.equals(score.getSelectedItem()) && !previousGroup.equals(object.getSelectedItem())) urGroup.setSelectedItem(previousGroup);
        if (previous != null && data.headers().contains(previous) &&
            !previous.equals(score.getSelectedItem()) && !previous.equals(object.getSelectedItem())) urReplicate.setSelectedItem(previous);
        else {
            outer: for (String preferred : List.of("item", "task", "replicate", "observation"))
                for (String column : data.headers())
                    if (column.equalsIgnoreCase(preferred) && !column.equals(score.getSelectedItem()) && !column.equals(object.getSelectedItem())) {
                        urReplicate.setSelectedItem(column);
                        break outer;
                    }
        }
        settingUp = false;
        rebuildUrCrossedOptions();
    }
    private void rebuildUrCrossedOptions() {
        if (data == null) return;
        String previous = (String) urCrossed.getSelectedItem();
        settingUp = true;
        urCrossed.removeAllItems();
        for (String column : data.headers())
            if (!column.equals(score.getSelectedItem()) && !column.equals(object.getSelectedItem()) && !column.equals(urReplicate.getSelectedItem()))
                urCrossed.addItem(column);
        if (previous != null && data.headers().contains(previous) &&
            !previous.equals(score.getSelectedItem()) && !previous.equals(object.getSelectedItem()) && !previous.equals(urReplicate.getSelectedItem()))
            urCrossed.setSelectedItem(previous);
        else {
            outer: for (String preferred : List.of("p", "facet", "condition", "rater"))
                for (String column : data.headers())
                    if (column.equalsIgnoreCase(preferred) && !column.equals(score.getSelectedItem()) &&
                        !column.equals(object.getSelectedItem()) && !column.equals(urReplicate.getSelectedItem())) {
                        urCrossed.setSelectedItem(column);
                        break outer;
                    }
        }
        settingUp = false;
        invalidateResults();
    }
    private void rebuildMOptions() {
        if(data==null)return;
        String previous=(String)mVariable.getSelectedItem();
        settingUp=true;mVariable.removeAllItems();
        for(String column:data.headers())if(!column.equals(score.getSelectedItem())&&!column.equals(object.getSelectedItem()))mVariable.addItem(column);
        if(previous!=null&&data.headers().contains(previous)&&!previous.equals(score.getSelectedItem())&&!previous.equals(object.getSelectedItem()))mVariable.setSelectedItem(previous);
        else for(String column:data.headers())if(column.equalsIgnoreCase("variable")||column.equalsIgnoreCase("scale"))mVariable.setSelectedItem(column);
        settingUp=false;
        rebuildMGroupOptions();
    }
    private void rebuildMGroupOptions() {
        if(data==null)return;
        String previous=(String)mGroup.getSelectedItem();
        settingUp=true;mGroup.removeAllItems();
        for(String column:data.headers())if(!column.equals(score.getSelectedItem())&&!column.equals(object.getSelectedItem())&&!column.equals(mVariable.getSelectedItem()))mGroup.addItem(column);
        if(previous!=null&&data.headers().contains(previous)&&!previous.equals(score.getSelectedItem())
            &&!previous.equals(object.getSelectedItem())&&!previous.equals(mVariable.getSelectedItem()))mGroup.setSelectedItem(previous);
        else for(String column:data.headers())if(column.equalsIgnoreCase("group")||column.equalsIgnoreCase("r")||column.equalsIgnoreCase("rater"))mGroup.setSelectedItem(column);
        settingUp=false;rebuildMItemOptions();
    }
    private void updateMOrderText() {
        if(data==null)return;
        int index=data.headers().indexOf((String)mVariable.getSelectedItem());
        LinkedHashSet<String> names=new LinkedHashSet<>();
        if(index>=0)for(List<String> row:data.rows())names.add(row.get(index));
        String order=names.isEmpty()?"Import data to see the fixed-variable order."
            :"Variable order: "+String.join(", ",names);
        if(mPir.isSelected()){
            index=data.headers().indexOf((String)mGroup.getSelectedItem());
            LinkedHashSet<String> groups=new LinkedHashSet<>();
            if(index>=0)for(List<String> row:data.rows())groups.add(row.get(index));
            order+=" · R-group order: "+String.join(", ",groups);
        }
        mVariableOrder.setText(order);
    }
    private void rebuildMItemOptions() {
        if(data==null)return;
        String previous=(String)mItem.getSelectedItem();
        settingUp=true;mItem.removeAllItems();
        for(String column:data.headers())if(!column.equals(score.getSelectedItem())&&!column.equals(object.getSelectedItem())&&!column.equals(mVariable.getSelectedItem())&&(!mPir.isSelected()||!column.equals(mGroup.getSelectedItem())))mItem.addItem(column);
        if(previous!=null&&data.headers().contains(previous)&&!previous.equals(score.getSelectedItem())&&!previous.equals(object.getSelectedItem())&&!previous.equals(mVariable.getSelectedItem())&&(!mPir.isSelected()||!previous.equals(mGroup.getSelectedItem())))mItem.setSelectedItem(previous);
        else for(String column:data.headers())if(column.equalsIgnoreCase("item")||column.equalsIgnoreCase("task"))mItem.setSelectedItem(column);
        settingUp=false;updateMOrderText();invalidateResults();
    }
    private boolean exportableTab() {
        int index = tabs.getSelectedIndex();
        return results != null && (index == 1 || index == 2 || (index == 3 && !results.decisions().headers().isEmpty()));
    }
    private void rebuildFacets() {
        facets.removeAll(); controls.clear();
        if (data == null) return;
        for (String col : data.headers()) {
            if (col.equals(score.getSelectedItem()) || col.equals(object.getSelectedItem())) continue;
            JPanel block = vertical(); block.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(220,227,233)),new EmptyBorder(7,0,9,0)));
            JCheckBox include = new JCheckBox(col, false); include.setOpaque(false); include.setAlignmentX(LEFT_ALIGNMENT); include.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); block.add(include);
            JComboBox<String> kind = new JComboBox<>(new String[]{"Random", "Fixed"});
            JComboBox<String> parent = new JComboBox<>(); parent.addItem("Crossed / not nested");
            for (String other : data.headers()) if (!other.equals(col) && !other.equals(score.getSelectedItem())) parent.addItem(other);
            JTextField sizes = new JTextField(); sizes.setToolTipText("Example: 1,2,3,4. Leave blank to use observed levels.");
            block.add(field("Inference",kind)); block.add(field("Nested within",parent)); block.add(field("D-study levels",sizes));
            controls.add(new FacetControls(col,include,kind,parent,sizes));
            include.addActionListener(e -> invalidateResults()); kind.addActionListener(e -> invalidateResults()); parent.addActionListener(e -> invalidateResults());
            sizes.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateResults(); }
            });
            facets.add(block);
        }
        facets.revalidate(); facets.repaint(); invalidateResults();
    }
    private void invalidateResults() {
        results = null; completedDesign = null; completedDescription = null; completedEngine = null;
        heatmap.setAvailable(false); surface.setAvailable(false);
        workbookSave.setEnabled(false); export.setEnabled(false); reportSave.setEnabled(false); resultLabel.setText("Design changed. Run the analysis again.");
        for (JTable t : List.of(anova,components,decisions)) showTable(t,List.of(),List.of()); report.setText(""); notice.setText("");
    }
    private void importFile() {
        String demoFile = System.getProperty("genova.demo.file");
        if (demoFile != null && !demoFile.isBlank()) {
            Path p = Path.of(demoFile);
            if (!Files.isRegularFile(p)) { error(new IOException("Demo workbook not found: " + p)); return; }
            try {
                String selected = null;
                if (p.toString().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                    List<String> sheets = DataTable.sheets(p);
                    if (sheets.isEmpty()) throw new IllegalArgumentException("The workbook has no worksheets.");
                    selected = sheets.size() == 1 ? sheets.get(0) : sheets.get(0);
                }
                load(DataTable.read(p, selected), p.toString());
                status.setText("Imported demo workbook: " + p.getFileName());
            } catch (Exception ex) { error(ex); }
            return;
        }
        JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Import long-format data"); chooser.setFileFilter(new FileNameExtensionFilter("Tables (.csv, .tsv, .txt, .xlsx)","csv","tsv","txt","xlsx"));
        String demoDir = System.getProperty("genova.demo.dir");
        if (demoDir == null || demoDir.isBlank()) {
            Path localDemoDir = Path.of("genova-gui", "demo", "data").toAbsolutePath();
            if (Files.isDirectory(localDemoDir)) demoDir = localDemoDir.toString();
        }
        if (demoDir != null && !demoDir.isBlank()) {
            File directory = new File(demoDir);
            if (directory.isDirectory()) chooser.setCurrentDirectory(directory);
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path p = chooser.getSelectedFile().toPath();
        try {
            String selected = null;
            if (p.toString().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                List<String> sheets = DataTable.sheets(p);
                if (sheets.isEmpty()) throw new IllegalArgumentException("The workbook has no worksheets.");
                selected = sheets.size() == 1 ? sheets.get(0) : (String)JOptionPane.showInputDialog(this,"Select a worksheet","Excel",JOptionPane.PLAIN_MESSAGE,null,sheets.toArray(),sheets.get(0));
                if (selected == null) return;
            }
            load(DataTable.read(p,selected),p.toString());
            if (System.getProperty("genova.demo.manual") != null) startInteractiveShowcase();
        } catch (Exception ex) { error(ex); }
    }

    /**
     * Presentation-only walkthrough used by the recording build. It opens each
     * real Swing combo box long enough for the option list to be visible, then
     * configures the official nested example, scrolls to the D-study controls,
     * runs the unchanged engine, and leaves the result tabs under user control.
     */
    private void startInteractiveShowcase() {
        if (data == null || selectedMode() != AnalysisMode.GENOVA) return;
        toFront();
        requestFocus();
        for (FacetControls f : controls) {
            f.include().setSelected(true);
            if (f.column().equalsIgnoreCase("item")) {
                f.parent().setSelectedItem("rater");
                f.sizes().setText("1,2,3,4");
            }
        }
        dStudy.setSelected(true);
        invalidateResults();
        status.setText("Interactive walkthrough: choose column roles and facet settings");
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> { status.setText("Score column options: person · rater · item · score"); score.showPopup(); });
        steps.add(() -> { score.hidePopup(); status.setText("Object of measurement options: person · rater · item · score"); object.showPopup(); });
        if (!controls.isEmpty()) {
            FacetControls rater = controls.stream().filter(f -> f.column().equalsIgnoreCase("rater")).findFirst().orElse(null);
            FacetControls item = controls.stream().filter(f -> f.column().equalsIgnoreCase("item")).findFirst().orElse(null);
            if (rater != null) {
                steps.add(() -> { object.hidePopup(); status.setText("rater · Inference options: Random · Fixed"); rater.kind().showPopup(); });
                steps.add(() -> { rater.kind().hidePopup(); status.setText("rater · Nested within: Crossed / not nested · person · item"); rater.parent().showPopup(); });
            }
            if (item != null) {
                steps.add(() -> { if (rater != null) rater.parent().hidePopup(); status.setText("item · Inference options: Random · Fixed"); item.kind().showPopup(); });
                steps.add(() -> { item.kind().hidePopup(); status.setText("item · Nested within: Crossed / not nested · person · rater"); item.parent().showPopup(); });
            }
        }
        final int[] step = {0};
        javax.swing.Timer menuTimer = new javax.swing.Timer(1900, null);
        menuTimer.addActionListener(e -> {
            if (step[0] < steps.size()) {
                steps.get(step[0]++).run();
                return;
            }
            menuTimer.stop();
            for (Component c : facets.getComponents()) c.setVisible(true);
            leftScroll.getVerticalScrollBar().setValue(leftScroll.getVerticalScrollBar().getMaximum());
            status.setText("D-study controls visible · ready to run");
            javax.swing.Timer runTimer = new javax.swing.Timer(2200, ev -> {
                ((javax.swing.Timer) ev.getSource()).stop();
                analyze();
            });
            runTimer.setRepeats(false);
            runTimer.start();
        });
        menuTimer.setInitialDelay(800);
        menuTimer.start();
    }

    /** Launch-time demo path: import the isolated workbook, configure the official
     * nested design, run the real GENOVA engine, and prepare the two visual views. */
    private void runAutomatedDemo() {
        if (data != null) return;
        importFile();
        if (data == null || selectedMode() != AnalysisMode.GENOVA) return;
        for (FacetControls f : controls) {
            f.include().setSelected(true);
            if (f.column().equalsIgnoreCase("item")) {
                f.parent().setSelectedItem("rater");
                f.sizes().setText("1,2,3,4");
            }
        }
        dStudy.setSelected(true);
        invalidateResults();
        status.setText("Demo configured: person × (item:rater) · ready to run");
        analyze();
    }
    private void paste() {
        JTextArea area = new JTextArea(17,65); area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
        try { area.setText((String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)); } catch (Exception ignored) {}
        JPanel panel = new JPanel(new BorderLayout(0,8)); panel.add(new JLabel("Paste cells from Excel with a header row, or paste CSV / TSV."),BorderLayout.NORTH); panel.add(new JScrollPane(area));
        if (JOptionPane.showConfirmDialog(this,panel,"Paste table",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
            try { load(DataTable.parse(area.getText()),"Pasted data"); } catch (Exception ex) { error(ex); }
    }
    private void sample() {
        if(selectedMode()==AnalysisMode.M_GENOVA){
            String appendix=mPir.isSelected()?"C":"B";
            String resource=mPir.isSelected()?"/samples/m_pir_manual_c.csv":"/samples/m_pxi_manual_b.csv";
            try(InputStream in=App.class.getResourceAsStream(resource)){
                if(in==null)throw new IOException("The mGENOVA Appendix "+appendix+" example is missing from this build.");
                load(DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)),"mGENOVA manual Appendix "+appendix+" example");
                object.setSelectedItem("person");mVariable.setSelectedItem("variable");
                if(mPir.isSelected())mGroup.setSelectedItem("group");
                mItem.setSelectedItem("item");mDItems.setText("");mWeights.setText("");mAWeights.setText("");
                status.setText("Manual Appendix "+appendix+" example configured. Click Run analysis for a multivariate G/D-study.");
            }catch(Exception ex){error(ex);}
            return;
        }
        if (selectedMode() == AnalysisMode.UR_GENOVA) {
            if (urMatrixSampling.isSelected()) { loadUrMatrixSamplingExample(); return; }
            if (urNestedMissing.isSelected()) { loadUrNestedMissingExample(); return; }
            if (urCrossedMissing.isSelected()) { loadUrCrossedMissingExample(); return; }
            if (urNestedCross.isSelected()) { loadUrNestedCrossExample(); return; }
            if (urTwoWay.isSelected()) { loadUrTwoWayExample(false); return; }
            try (InputStream in = App.class.getResourceAsStream("/samples/ur_one_way.csv")) {
                if (in == null) throw new IOException("The urGENOVA pilot example is missing from this build.");
                load(DataTable.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)), "Unbalanced one-way pilot example");
                status.setText("Pilot example configured. Click Run analysis for a one-way random G-study.");
            } catch (Exception ex) { error(ex); }
            return;
        }
        try (InputStream in = App.class.getResourceAsStream("/samples/official_nested.csv")) {
            if (in == null) throw new IOException("The example data is missing from this build.");
            load(DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)),"Official P × (I:R) example");
            for (FacetControls f : controls) {
                f.include().setSelected(true);
                if (f.column().equals("item")) { f.parent().setSelectedItem("rater"); f.sizes().setText("1,2,3,4"); }
            }
            dStudy.setSelected(true); status.setText("Example configured: person × (item:rater). Click Run analysis.");
        } catch (Exception ex) { error(ex); }
    }
    private void loadUrTwoWayExample(boolean empty) {
        String resource = empty ? "/samples/ur_two_way_manual_e.csv" : "/samples/ur_two_way_manual_d.csv";
        try (InputStream in = App.class.getResourceAsStream(resource)) {
            if (in == null) throw new IOException("The urGENOVA manual example is missing from this build.");
            load(DataTable.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)),
                empty ? "urGENOVA manual Appendix E example" : "urGENOVA manual Appendix D example");
            object.setSelectedItem("h");
            urReplicate.setSelectedItem("replicate");
            urCrossed.setSelectedItem("p");
            status.setText("Manual Appendix " + (empty ? "E (one empty cell)" : "D")
                + " example configured. Click Run analysis for the two-way random G-study.");
        } catch (Exception ex) { error(ex); }
    }
    private void loadUrNestedCrossExample() {
        try (InputStream in = App.class.getResourceAsStream("/samples/ur_nested_cross_manual_a.csv")) {
            if (in == null) throw new IOException("The urGENOVA Appendix A example is missing from this build.");
            load(DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)),"urGENOVA manual Appendix A example");
            object.setSelectedItem("p"); urReplicate.setSelectedItem("i");
            urCrossed.setSelectedItem("r"); urGroup.setSelectedItem("c");
            status.setText("Manual Appendix A example configured. Click Run analysis for the nested-cross random G-study.");
        } catch (Exception ex) { error(ex); }
    }
    private void loadUrCrossedMissingExample() {
        try (InputStream in = App.class.getResourceAsStream("/samples/ur_crossed_missing_manual_f.csv")) {
            if (in == null) throw new IOException("The urGENOVA Appendix F example is missing from this build.");
            load(DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)),"urGENOVA manual Appendix F example");
            object.setSelectedItem("person"); urReplicate.setSelectedItem("item");
            status.setText("Manual Appendix F example configured. Click Run analysis for the crossed missing-cell G-study.");
        } catch (Exception ex) { error(ex); }
    }
    private void loadUrNestedMissingExample() {
        try (InputStream in = App.class.getResourceAsStream("/samples/ur_nested_missing_manual_g.csv")) {
            if (in == null) throw new IOException("The urGENOVA Appendix G example is missing from this build.");
            load(DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)),"urGENOVA manual Appendix G example");
            object.setSelectedItem("person"); urReplicate.setSelectedItem("item"); urCrossed.setSelectedItem("h");
            status.setText("Manual Appendix G example configured. Click Run analysis for the nested missing-cell G-study.");
        } catch (Exception ex) { error(ex); }
    }
    private void loadUrMatrixSamplingExample() {
        try (InputStream in = App.class.getResourceAsStream("/samples/ur_matrix_sampling_manual_h.csv")) {
            if (in == null) throw new IOException("The urGENOVA Appendix H example is missing from this build.");
            load(DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)),"urGENOVA manual Appendix H example");
            object.setSelectedItem("p"); urReplicate.setSelectedItem("i"); urCrossed.setSelectedItem("r");
            status.setText("Manual Appendix H example configured. Click Run analysis for the matrix-sampling G-study.");
        } catch (Exception ex) { error(ex); }
    }
    private void analyze() {
        if (selectedMode() == AnalysisMode.UR_GENOVA) { analyzeUr(); return; }
        if (selectedMode() == AnalysisMode.M_GENOVA) { analyzeM(); return; }
        if (selectedMode() != AnalysisMode.GENOVA) return;
        try {
            List<Design.Facet> configured = new ArrayList<>();
            for (FacetControls f : controls) if (f.include().isSelected()) configured.add(new Design.Facet(f.column(),f.parent().getSelectedIndex()==0?"":(String)f.parent().getSelectedItem(),f.kind().getSelectedIndex()==1,f.sizes().getText()));
            Design.Prepared design = Design.prepare(data,(String)score.getSelectedItem(),(String)object.getSelectedItem(),configured,dStudy.isSelected());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running analysis…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() throws Exception { return Engine.run(design); }
                protected void done() {
                    busy(false);
                    try {
                        results = get(); completedDesign = design; completedDescription = design.description(); completedEngine = "jGENOVA 1.0";
                        showTable(anova,results.anova().headers(),results.anova().rows()); showTable(components,results.components().headers(),results.components().rows()); showTable(decisions,results.decisions().headers(),results.decisions().rows());
                        report.setText(results.report()); report.setCaretPosition(0); notice.setText(results.notices()); resultLabel.setText(design.description());
                        tabs.setSelectedIndex(design.scenarios().isEmpty()?2:3); workbookSave.setEnabled(true); reportSave.setEnabled(true); export.setEnabled(tabs.getSelectedIndex() >= 1 && tabs.getSelectedIndex() <= 3);
                        status.setText("Analysis complete · "+data.rows().size()+" observations · "+design.scenarios().size()+" D-study scenarios");
                        long randomFacets = design.facets().stream().skip(1).filter(f -> !f.fixed()).count();
                        if (randomFacets == 2) {
                            List<Design.Facet> plotFacets = design.facets().stream().skip(1).filter(f -> !f.fixed()).toList();
                            heatmap.setFacets(plotFacets.get(0).column(), plotFacets.get(1).column());
                        }
                        heatmap.setAvailable(!design.scenarios().isEmpty() && randomFacets == 2);
                        surface.setAvailable(!design.scenarios().isEmpty() && randomFacets == 2);
                        exportAutomatedDemoBase();
                        if (System.getProperty("genova.demo.file") != null && !design.scenarios().isEmpty() && randomFacets == 2) {
                            javax.swing.Timer timer = new javax.swing.Timer(500, event -> {
                                ((javax.swing.Timer) event.getSource()).stop();
                                generateHeatmap();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        }
                        if (System.getProperty("genova.demo.manual") != null && !design.scenarios().isEmpty() && randomFacets == 2) {
                            javax.swing.Timer timer = new javax.swing.Timer(900, event -> {
                                ((javax.swing.Timer) event.getSource()).stop();
                                generateHeatmap();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        }
                    } catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void generateHeatmap() {
        if (completedDesign == null || results == null || selectedMode() != AnalysisMode.GENOVA) return;
        try {
            int xf = heatmap.xFrom(), xt = heatmap.xTo(), yf = heatmap.yFrom(), yt = heatmap.yTo();
            if (xf > xt || yf > yt || (long)(xt-xf+1)*(yt-yf+1) > 30)
                throw new IllegalArgumentException("Use ascending ranges with at most 30 grid points.");
            Design.Prepared design = completedDesign;
            DataTable sourceData = data;
            String scoreColumn = (String)score.getSelectedItem(), objectColumn = (String)object.getSelectedItem();
            heatmap.setBusy(true); surface.setBusy(true);
            status.setText("Computing " + ((xt-xf+1)*(yt-yf+1)) + " D-study grid points…");
            new SwingWorker<DStudyGrid,Void>() {
                protected DStudyGrid doInBackground() throws Exception {
                    return DStudyGrid.run(sourceData, scoreColumn, objectColumn, design, xf, xt, yf, yt);
                }
                protected void done() {
                    if (completedDesign != design || selectedMode() != AnalysisMode.GENOVA) return;
                    heatmap.setBusy(false); surface.setBusy(false);
                    try {
                        heatmap.setGrid(get());
                        status.setText("D-study heatmap and 3D plot ready · " + heatmap.grid().xLevels().length
                            * heatmap.grid().yLevels().length + " engine scenarios");
                        exportAutomatedDemoVisuals();
                    } catch (Exception ex) {
                        status.setText("Heatmap calculation failed. Review the message.");
                        error(ex.getCause() == null ? ex : ex.getCause());
                    }
                }
            }.execute();
        } catch (Exception ex) { error(ex); }
    }

    private void exportHeatmap() {
        if (heatmap.grid() != null) save("genova_dstudy_grid.csv", "\uFEFF" + heatmap.grid().csv());
    }
    private void exportSurface() {
        if (surface.grid() == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export D-study 3D plot");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG image (.png)", "png"));
        chooser.setSelectedFile(new File("genova_dstudy_3d.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path chosen = chooser.getSelectedFile().toPath();
        Path path = chosen.toString().toLowerCase(Locale.ROOT).endsWith(".png") ? chosen : Path.of(chosen + ".png");
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this,"This file already exists. Replace it?", "Export plot",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Path temp = null;
        try {
            temp = Files.createTempFile(path.toAbsolutePath().getParent(), ".genova-plot-", ".png");
            if (!ImageIO.write(surface.image(1600, 1000), "png", temp.toFile()))
                throw new IOException("PNG export is unavailable in this runtime.");
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            status.setText("3D plot saved: " + path.getFileName());
        } catch (Exception ex) { error(ex); }
        finally { if (temp != null) try { Files.deleteIfExists(temp); } catch (IOException ignored) {} }
    }

    private Path demoOutputDirectory() {
        String value = System.getProperty("genova.demo.output");
        if (value == null || value.isBlank()) return null;
        Path out = Path.of(value);
        try { Files.createDirectories(out); return out; } catch (IOException ex) { return null; }
    }

    private void exportAutomatedDemoBase() {
        Path out = demoOutputDirectory();
        if (out == null || results == null || completedDescription == null) return;
        try {
            WorkbookExport.write(out.resolve("genova-studio-results.xlsx"), results, completedDescription,
                data.rows().size(), (String)object.getSelectedItem(), (String)score.getSelectedItem(), completedEngine);
            Files.writeString(out.resolve("genova-studio-report.txt"), report.getText(), StandardCharsets.UTF_8);
            Files.writeString(out.resolve("genova-studio-dstudy.csv"), "\uFEFF" + results.decisions().csv(), StandardCharsets.UTF_8);
        } catch (Exception ex) { status.setText("Demo analysis complete; automatic export needs attention."); }
    }

    private void exportAutomatedDemoVisuals() {
        Path out = demoOutputDirectory();
        if (out == null || heatmap.grid() == null) return;
        try {
            Files.writeString(out.resolve("genova-studio-dstudy-grid.csv"), "\uFEFF" + heatmap.grid().csv(), StandardCharsets.UTF_8);
            Files.write(out.resolve("genova-studio-3d.png"), toPng(surface.image(1600, 1000)));
        } catch (Exception ignored) { }
    }

    private static byte[] toPng(java.awt.image.BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes); return bytes.toByteArray();
    }
    private void analyzeM() {
        if(mPir.isSelected()){analyzeMNested();return;}
        try {
            MpXiDesign.Prepared design=MpXiDesign.prepare(data,(String)score.getSelectedItem(),
                (String)object.getSelectedItem(),(String)mVariable.getSelectedItem(),(String)mItem.getSelectedItem());
            int[] dItems=null;
            if(!mDItems.getText().isBlank()){
                String[] parts=mDItems.getText().split(",",-1);
                if(parts.length!=design.variables().size())throw new IllegalArgumentException(
                    "Enter one D-study item count for each fixed variable: "+String.join(", ",design.variables()));
                dItems=new int[parts.length];
                for(int i=0;i<parts.length;i++)try{dItems[i]=Integer.parseInt(parts[i].trim());}
                    catch(NumberFormatException ex){throw new IllegalArgumentException("D-study item counts must be positive whole numbers.");}
            }
            final int[] requested=dItems;
            final double[] requestedWeights=parseMWeights(mWeights,design.variables(),"w");
            final double[] requestedA=parseMWeights(mAWeights,design.variables(),"a");
            invalidateResults();busy(true);status.setText("Data structure validated. Running multivariate P × I G/D-study…");
            new SwingWorker<Results,Void>(){
                protected Results doInBackground(){return MpXiEngine.run(design,requested,requestedWeights,requestedA);}
                protected void done(){
                    busy(false);
                    try{
                        showMResult(get(),design.description(),design.observations().size(),"mGENOVA P × I Java pilot");
                    }catch(Exception ex){invalidateResults();status.setText("Analysis failed. Review the message.");error(ex.getCause()==null?ex:ex.getCause());}
                }
            }.execute();
        }catch(Exception ex){invalidateResults();error(ex);}
    }
    private double[] parseMWeights(JTextField input,List<String> variables,String label) {
        if(input.getText().isBlank())return null;
        String[] parts=input.getText().split(",",-1);
        if(parts.length!=variables.size())throw new IllegalArgumentException(
            "Enter one "+label+" weight for each fixed variable: "+String.join(", ",variables));
        double[] weights=new double[parts.length];
        for(int i=0;i<parts.length;i++)try{weights[i]=Double.parseDouble(parts[i].trim());}
            catch(NumberFormatException ex){throw new IllegalArgumentException(label+" weights must be numeric values.");}
        return weights;
    }
    private void analyzeMNested() {
        try {
            MpIrDesign.Prepared design=MpIrDesign.prepare(data,(String)score.getSelectedItem(),
                (String)object.getSelectedItem(),(String)mVariable.getSelectedItem(),
                (String)mGroup.getSelectedItem(),(String)mItem.getSelectedItem());
            int[][] dItems=null;
            if(!mDItems.getText().isBlank()){
                String[] rows=mDItems.getText().split(";",-1);
                if(rows.length!=design.variables().size())throw new IllegalArgumentException(
                    "Enter one semicolon-separated D-study R-count row for each fixed variable: "
                        +String.join(", ",design.variables()));
                dItems=new int[rows.length][];
                for(int v=0;v<rows.length;v++){
                    String[] parts=rows[v].split(",",-1);
                    dItems[v]=new int[parts.length];
                    for(int r=0;r<parts.length;r++)try{dItems[v][r]=Integer.parseInt(parts[r].trim());}
                        catch(NumberFormatException ex){throw new IllegalArgumentException("D-study R-group item counts must be positive whole numbers.");}
                }
            }
            final int[][] requested=dItems;
            final double[] requestedWeights=parseMWeights(mWeights,design.variables(),"w");
            final double[] requestedA=parseMWeights(mAWeights,design.variables(),"a");
            invalidateResults();busy(true);status.setText("Data structure validated. Running multivariate P × (I:R) G/D-study…");
            new SwingWorker<Results,Void>(){
                protected Results doInBackground(){return MpIrEngine.run(design,requested,requestedWeights,requestedA);}
                protected void done(){
                    busy(false);
                    try{showMResult(get(),design.description(),design.observations().size(),"mGENOVA P × (I:R) Java pilot");}
                    catch(Exception ex){invalidateResults();status.setText("Analysis failed. Review the message.");error(ex.getCause()==null?ex:ex.getCause());}
                }
            }.execute();
        }catch(Exception ex){invalidateResults();error(ex);}
    }
    private void showMResult(Results result,String description,int observations,String engine) {
        results=result;completedDescription=description;completedEngine=engine;
        showTable(anova,result.anova().headers(),result.anova().rows());
        showTable(components,result.components().headers(),result.components().rows());
        showTable(decisions,result.decisions().headers(),result.decisions().rows());
        report.setText(result.report());report.setCaretPosition(0);notice.setText(result.notices());
        resultLabel.setText(description);tabs.setSelectedIndex(3);
        workbookSave.setEnabled(true);reportSave.setEnabled(true);export.setEnabled(exportableTab());
        status.setText("Multivariate pilot complete · "+observations+" observations · 1 D-study scenario");
    }
    private void analyzeUr() {
        if (urMatrixSampling.isSelected()) { analyzeUrMatrixSampling(); return; }
        if (urNestedMissing.isSelected()) { analyzeUrNestedMissing(); return; }
        if (urCrossedMissing.isSelected()) { analyzeUrCrossedMissing(); return; }
        if (urNestedCross.isSelected()) { analyzeUrNestedCross(); return; }
        if (urTwoWay.isSelected()) { analyzeUrTwoWay(); return; }
        try {
            UrDesign.Prepared design = UrDesign.prepare(data, (String) score.getSelectedItem(),
                (String) object.getSelectedItem(), (String) urReplicate.getSelectedItem());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running one-way unbalanced G-study…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() { return UrEngine.run(design); }
                protected void done() {
                    busy(false);
                    try {
                        showUrResult(get(), design.description(), design.observations(), "urGENOVA one-way Java pilot");
                    } catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void analyzeUrTwoWay() {
        try {
            UrTwoWayDesign.Prepared design = UrTwoWayDesign.prepare(data, (String) score.getSelectedItem(),
                (String) object.getSelectedItem(), (String) urCrossed.getSelectedItem(), (String) urReplicate.getSelectedItem());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running two-way unbalanced G-study…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() { return UrTwoWayEngine.run(design); }
                protected void done() {
                    busy(false);
                    try {
                        showUrResult(get(), design.description(), design.observations(), "urGENOVA two-way Java pilot");
                    } catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void analyzeUrNestedCross() {
        try {
            UrNestedCrossDesign.Prepared design = UrNestedCrossDesign.prepare(data, (String)score.getSelectedItem(),
                (String)object.getSelectedItem(), (String)urGroup.getSelectedItem(),
                (String)urCrossed.getSelectedItem(), (String)urReplicate.getSelectedItem());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running nested-cross unbalanced G-study…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() { return UrNestedCrossEngine.run(design); }
                protected void done() {
                    busy(false);
                    try { showUrResult(get(),design.description(),design.observations().size(),"urGENOVA nested-cross Java pilot"); }
                    catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void analyzeUrCrossedMissing() {
        try {
            UrCrossedMissingDesign.Prepared design = UrCrossedMissingDesign.prepare(data, (String)score.getSelectedItem(),
                (String)object.getSelectedItem(), (String)urReplicate.getSelectedItem());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running crossed missing-cell G-study…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() { return UrCrossedMissingEngine.run(design); }
                protected void done() {
                    busy(false);
                    try { showUrResult(get(),design.description(),design.observations().size(),"urGENOVA missing-cross Java pilot"); }
                    catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void analyzeUrNestedMissing() {
        try {
            UrNestedMissingDesign.Prepared design = UrNestedMissingDesign.prepare(data, (String)score.getSelectedItem(),
                (String)object.getSelectedItem(), (String)urCrossed.getSelectedItem(), (String)urReplicate.getSelectedItem());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running nested missing-cell G-study…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() { return UrNestedMissingEngine.run(design); }
                protected void done() {
                    busy(false);
                    try { showUrResult(get(),design.description(),design.observations().size(),"urGENOVA nested missing-cell Java pilot"); }
                    catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void analyzeUrMatrixSampling() {
        try {
            UrMatrixSamplingDesign.Prepared design = UrMatrixSamplingDesign.prepare(data, (String)score.getSelectedItem(),
                (String)urCrossed.getSelectedItem(), (String)object.getSelectedItem(), (String)urReplicate.getSelectedItem());
            invalidateResults(); busy(true); status.setText("Data structure validated. Running matrix-sampling G-study…");
            new SwingWorker<Results,Void>() {
                protected Results doInBackground() { return UrMatrixSamplingEngine.run(design); }
                protected void done() {
                    busy(false);
                    try { showUrResult(get(),design.description(),design.observations().size(),"urGENOVA matrix-sampling Java pilot"); }
                    catch (Exception ex) { invalidateResults(); status.setText("Analysis failed. Review the message."); error(ex.getCause()==null?ex:ex.getCause()); }
                }
            }.execute();
        } catch (Exception ex) { invalidateResults(); error(ex); }
    }
    private void showUrResult(Results result, String description, int observations, String engine) {
        results = result; completedDescription = description; completedEngine = engine;
        showTable(anova,results.anova().headers(),results.anova().rows());
        showTable(components,results.components().headers(),results.components().rows());
        showTable(decisions,List.of(),List.of());
        report.setText(results.report()); report.setCaretPosition(0); notice.setText(results.notices());
        resultLabel.setText(description); tabs.setSelectedIndex(2);
        workbookSave.setEnabled(true); reportSave.setEnabled(true); export.setEnabled(exportableTab());
        status.setText("Pilot G-study complete · " + observations + " observations · no D-study");
    }
    private void busy(boolean value) { run.setEnabled(!value); inputButtons.forEach(b -> b.setEnabled(!value)); setEnabledRecursive(settings,!value); progress.setVisible(value); }
    private static void setEnabledRecursive(Component c, boolean enabled) { c.setEnabled(enabled); if (c instanceof Container p) for (Component child : p.getComponents()) setEnabledRecursive(child,enabled); }
    private void saveWorkbook() {
        if (results == null || completedDescription == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export analysis results to Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel workbook (.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("genova_results.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path chosen = chooser.getSelectedFile().toPath();
        Path path = chosen.toString().toLowerCase(Locale.ROOT).endsWith(".xlsx") ? chosen : Path.of(chosen + ".xlsx");
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this,"This file already exists. Replace it?", "Export results",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Path temp = null;
        try {
            Path dir = path.toAbsolutePath().getParent();
            temp = Files.createTempFile(dir, ".genova-export-", ".xlsx");
            WorkbookExport.write(temp, results, completedDescription, data.rows().size(),
                (String)object.getSelectedItem(), (String)score.getSelectedItem(), completedEngine);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            status.setText("Excel workbook saved: " + path.getFileName());
        } catch (Exception ex) { error(ex); }
        finally { if (temp != null) try { Files.deleteIfExists(temp); } catch (IOException ignored) {} }
    }
    private void exportTable() {
        if (results == null) return;
        Results.Table selected = switch(tabs.getSelectedIndex()) { case 1 -> results.anova(); case 2 -> results.components(); case 3 -> results.decisions(); default -> null; };
        if (selected != null && !selected.headers().isEmpty()) save("genova_results.csv","\uFEFF"+selected.csv());
    }
    private void saveReport() { if (results != null) save("genova_report.txt",results.report()); }
    private void save(String filename, String content) {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File(filename));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this,"This file already exists. Replace it?", "Save results",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { Files.writeString(path,content,StandardCharsets.UTF_8); status.setText("Saved: "+path.getFileName()); } catch (Exception ex) { error(ex); }
    }
    private void error(Throwable ex) {
        JTextArea message = text(ex.getMessage()==null?"This operation could not be completed. Check the input file.":ex.getMessage()); message.setColumns(45);
        JOptionPane.showMessageDialog(this,message,"Check data or design",JOptionPane.WARNING_MESSAGE);
    }
    private static final class ScrollPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return new Dimension(350,650); }
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return Math.max(16,r.height-32); }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private static JTextArea help() {
        JTextArea t = new JTextArea();
        t.setText("""
            Getting started

            GENOVA: complete balanced univariate G- and D-studies using the original jGENOVA engine. D-study is blocked for nesting chains of two or more levels after an independent QC comparison found inconsistent coefficients; G-study remains available.
            urGENOVA: six limited Java G-study pilots, not the original urGENOVA executable. The one-way model allows unequal counts of replicates nested within each object. The two-way model has two crossed random factors and replicates nested within each crossed cell; it accepts unequal cell counts or an empty cell. The nested-cross model has objects P nested in C, and facet I nested in crossed group R; it accepts unequal P-per-C and I-per-R counts, but requires a complete P × I table. The crossed missing-cell model accepts one score per present P × I cell, with absent rows denoting missing observations. The nested missing-cell model crosses P with I nested within H and permits absent P × I cells while retaining every P × H block. The matrix-sampling model has P and I each nested within R, with complete P × I tables of unequal size by R. Scores need not be binary. Results match printed examples in the urGENOVA 2.1 manual, Appendices A, D, E, F, G, and H, but have not been compared with the executable. Other designs and D-studies are not yet available.
            mGENOVA: two experimental multivariate random-effects G/D-study pilots. P × I nests items within fixed variables V; P × (I:R) nests items within both R and V. Persons P (and R in the second design) are shared across variables. The Java implementations match printed mGENOVA manual Appendices B, C, and D values, but have not been compared with the original executable. Both permit custom universe weights w and estimation weights a. Other multivariate designs are not supported.

            Import CSV, TSV, or Excel .xlsx, or paste a long-format table with headers. Each row is one observation. Choose a numeric score and an object-of-measurement column. In the urGENOVA pilot, also choose a replicate ID. The same replicate label in different objects is treated as a different nested unit. Do not select this mode if those labels actually identify the same crossed item across objects. In mGENOVA, choose fixed-variable and item columns, plus R for P × (I:R); item labels may repeat across their nesting groups but every person must respond to every item.

            The one-way pilot accepts exactly three columns (object, nested replicate ID, score), at least two objects, and two observations for every object. The two-way pilot accepts exactly four columns (crossed random facet, object, nested replicate ID, score), at least two levels of each crossed factor, and positive residual degrees of freedom. The nested-cross pilot accepts exactly five columns (object P, containing group C, crossed group R, nested facet I, score), with every P × I cell present once. The crossed missing-cell pilot accepts exactly three columns (object P, crossed facet I, score), with one row per present cell and positive interaction degrees of freedom. The nested missing-cell pilot accepts exactly four columns (object P, group H, I nested within H, score), with at least two observed I levels in every P × H block. The matrix-sampling pilot accepts exactly four columns (group R, object P, facet I, score), with every P × I cell present within each R. Choose the correct design explicitly. Duplicate design cells are rejected. A missing observation is represented by an absent row, not a blank score. All pilots retain negative variance estimates and do not compute D-study coefficients.

            For GENOVA, select facets and configure random/fixed and crossed/nested relationships. D-study level lists pair by position; one value is reused across scenarios. For two-or-more-level nesting chains, uncheck Run D-study to run the G-study only; the original jGENOVA D-study coefficients for this case did not pass independent QC. The object of measurement must be random and not nested. Missing or duplicate design cells are rejected rather than imputed or averaged.

            For a balanced D-study with two random facets, choose count ranges in D-study heatmap and generate the grid. The 3D plot uses the same calculated points: select G or Phi, drag to rotate and tilt, hover over a point for both exact coefficients, or export a PNG. Each axis needs at least two levels to form a surface; the grid accepts at most 30 points.

            For mGENOVA P × I, provide exactly four columns: person, fixed variable, item, score. For P × (I:R), add the shared R group as a fifth column, with items nested within R and variable. Both require at least two persons and two fixed variables, complete person-by-item cells, and at least two items per nesting group. The optional P × I D-study field accepts comma-separated item counts in the displayed variable order. For P × (I:R), enter one comma-separated R-group item-count row per variable, separated by semicolons, for example 3,6,3;4,3,3. Leave either field blank for observed counts. Universe weights w default to proportions of the requested total item counts; estimation weights a default to w. Optional comma-separated weights are applied as entered. When a differs from w, the composite row reports relative and absolute mean-squared errors instead of error variances, and composite G/Φ are not applicable. G-study estimates and D-study results are experimental outside the manual examples.

            Export results (Excel) saves typed numeric ANOVA and variance-component tables, plus metadata; GENOVA and mGENOVA include a D-study sheet, and mGENOVA includes a typed D-study setup sheet with item counts and w/a weights. Export table (CSV) saves the currently selected result table. Save report (TXT) preserves the detailed report for auditing. Data is processed locally without network upload.
            """);
        t.setEditable(false); t.setLineWrap(true); t.setWrapStyleWord(true); t.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14)); t.setMargin(new Insets(20,22,20,22)); return t;
    }
}
