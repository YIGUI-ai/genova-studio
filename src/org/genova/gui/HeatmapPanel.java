package org.genova.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.*;

/** Swing view for a D-study grid; all values come from the GENOVA engine. */
public final class HeatmapPanel extends JPanel {
    private final JSpinner xFrom = spinner(1), xTo = spinner(5);
    private final JSpinner yFrom = spinner(1), yTo = spinner(5);
    private final JLabel xLabel = new JLabel("X from"), yLabel = new JLabel("Y from");
    private final JComboBox<String> metric = new JComboBox<>(new String[]{"Phi", "G coefficient"});
    private final JButton generate = new JButton("Generate heatmap"), export = new JButton("Export grid (CSV)");
    private final JLabel note = new JLabel("Run a balanced GENOVA D-study with two random facets to enable the heatmap.");
    private final Canvas canvas = new Canvas();
    private final Consumer<DStudyGrid> onGridChange;
    private DStudyGrid grid;
    private boolean available;

    public HeatmapPanel(Runnable onGenerate, Runnable onExport, Consumer<DStudyGrid> onGridChange) {
        super(new BorderLayout(8, 8));
        this.onGridChange = onGridChange;
        JPanel bar = new JPanel(new GridLayout(2, 1));
        JPanel ranges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        ranges.add(xLabel); ranges.add(xFrom); ranges.add(new JLabel("to")); ranges.add(xTo);
        ranges.add(yLabel); ranges.add(yFrom); ranges.add(new JLabel("to")); ranges.add(yTo);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.add(new JLabel("Color")); actions.add(metric); actions.add(generate); actions.add(export);
        bar.add(ranges); bar.add(actions);
        add(bar, BorderLayout.NORTH);
        note.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));
        add(note, BorderLayout.SOUTH);
        canvas.setBackground(Color.WHITE); canvas.setOpaque(true); add(canvas, BorderLayout.CENTER);
        generate.addActionListener(e -> onGenerate.run()); export.addActionListener(e -> onExport.run());
        metric.addActionListener(e -> canvas.repaint());
        for (JSpinner range : new JSpinner[]{xFrom,xTo,yFrom,yTo})
            range.addChangeListener(e -> { if (grid != null) setGrid(null); });
        setAvailable(false);
    }

    private static JSpinner spinner(int value) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, 1, 100, 1));
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setColumns(2);
        return s;
    }
    public int xFrom() { return (int)xFrom.getValue(); }
    public int xTo() { return (int)xTo.getValue(); }
    public int yFrom() { return (int)yFrom.getValue(); }
    public int yTo() { return (int)yTo.getValue(); }
    public DStudyGrid grid() { return grid; }
    public void setFacets(String x, String y) {
        xLabel.setText("X (" + x + ") from"); yLabel.setText("Y (" + y + ") from");
    }
    public void setAvailable(boolean available) {
        this.available = available;
        generate.setEnabled(available); export.setEnabled(available && grid != null);
        for (JSpinner range : new JSpinner[]{xFrom,xTo,yFrom,yTo}) range.setEnabled(true);
        if (!available) setGrid(null);
        if (available && grid == null) note.setText("Choose X and Y count ranges, then generate the heatmap. At most 30 cells.");
        if (!available) note.setText("Run a balanced GENOVA D-study with two random facets to enable the heatmap.");
    }
    public void setBusy(boolean busy) {
        generate.setEnabled(available && !busy); export.setEnabled(available && !busy && grid != null);
        for (JSpinner range : new JSpinner[]{xFrom,xTo,yFrom,yTo}) range.setEnabled(!busy);
    }
    public void setGrid(DStudyGrid value) {
        grid = value; export.setEnabled(available && value != null);
        note.setText(value == null ? (available ? "Choose count ranges, then generate the heatmap. At most 30 cells."
            : "Run a balanced GENOVA D-study with two random facets to enable the heatmap.")
            : "X: " + value.xFacet() + " levels  ·  Y: " + value.yFacet() + " levels  ·  Hover over a cell for exact G and Phi values.");
        canvas.repaint();
        onGridChange.accept(value);
    }

    private final class Canvas extends JComponent {
        private int left, top, cellW, cellH;
        Canvas() { setToolTipText(" "); }
        @Override public String getToolTipText(MouseEvent e) {
            if (grid == null || cellW <= 0 || cellH <= 0) return null;
            int x = (e.getX() - left) / cellW;
            int displayY = (e.getY() - top) / cellH;
            int y = grid.yLevels().length - 1 - displayY;
            if (e.getX() < left || e.getY() < top || x < 0 || x >= grid.xLevels().length || y < 0 || y >= grid.yLevels().length) return null;
            return String.format("%s=%d, %s=%d  ·  G=%.5f  ·  Phi=%.5f", grid.xFacet(), grid.xLevels()[x],
                grid.yFacet(), grid.yLevels()[y], grid.g()[y][x], grid.phi()[y][x]);
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (grid == null) return;
            Graphics2D g = (Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int nx = grid.xLevels().length, ny = grid.yLevels().length;
            int maxWidth = Math.max(180, getWidth() - 120), maxHeight = Math.max(180, getHeight() - 110);
            cellW = Math.max(34, Math.min(100, maxWidth / nx)); cellH = Math.max(34, Math.min(90, maxHeight / ny));
            left = 70 + Math.max(0, (maxWidth - nx * cellW) / 2);
            top = 28 + Math.max(0, (maxHeight - ny * cellH) / 2);
            double[][] values = metric.getSelectedIndex() == 0 ? grid.phi() : grid.g();
            g.setFont(getFont().deriveFont(13f));
            for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++) {
                int displayY = ny - 1 - y, px = left + x * cellW, py = top + displayY * cellH;
                double v = values[y][x], t = Math.max(0, Math.min(1, v));
                g.setColor(new Color((int)(239 - 175*t), (int)(245 - 80*t), (int)(248 - 77*t)));
                g.fillRect(px, py, cellW, cellH);
                g.setColor(new Color(220, 229, 233)); g.drawRect(px, py, cellW, cellH);
                String label = String.format("%.3f", v);
                g.setColor(t > 0.64 ? Color.WHITE : new Color(27,45,62));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(label, px + (cellW-fm.stringWidth(label))/2, py + (cellH+fm.getAscent())/2 - 3);
            }
            g.setColor(new Color(50,65,80));
            for (int x = 0; x < nx; x++) {
                String label = Integer.toString(grid.xLevels()[x]);
                g.drawString(label, left + x*cellW + (cellW-g.getFontMetrics().stringWidth(label))/2, top + ny*cellH + 20);
            }
            for (int y = 0; y < ny; y++) {
                String label = Integer.toString(grid.yLevels()[y]);
                g.drawString(label, left - 20, top + (ny-1-y)*cellH + cellH/2 + 5);
            }
            g.drawString(grid.xFacet() + " levels", left + nx*cellW/2 - 35, top + ny*cellH + 48);
            g.drawString(grid.yFacet() + " levels", 9, top - 8);
            g.dispose();
        }
    }
}
