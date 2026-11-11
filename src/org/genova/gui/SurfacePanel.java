package org.genova.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;

/** Rotatable 3D view of coefficients already calculated by the D-study grid. */
public final class SurfacePanel extends JPanel {
    private final JComboBox<String> metric = new JComboBox<>(new String[]{"Phi", "G coefficient"});
    private final JSlider azimuth = new JSlider(0, 360, 40);
    private final JSlider elevation = new JSlider(15, 75, 35);
    private final JButton generate = new JButton("Generate grid"), export = new JButton("Export PNG");
    private final JLabel note = new JLabel("Run a balanced GENOVA D-study with two random facets to enable the 3D plot.");
    private final Plot plot = new Plot();
    private DStudyGrid grid;
    private boolean available;

    public SurfacePanel(Runnable onGenerate, Runnable onExport) {
        super(new BorderLayout(8, 8));
        JPanel controls = new JPanel(new GridLayout(2, 1));
        JPanel first = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        first.add(new JLabel("Height and color")); first.add(metric); first.add(generate); first.add(export);
        JPanel second = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        azimuth.setPreferredSize(new Dimension(170, 24));
        elevation.setPreferredSize(new Dimension(130, 24));
        second.add(new JLabel("Rotate")); second.add(azimuth);
        second.add(new JLabel("Tilt")); second.add(elevation);
        JButton reset = new JButton("Reset view"); second.add(reset);
        controls.add(first); controls.add(second); add(controls, BorderLayout.NORTH);
        plot.setOpaque(true); plot.setBackground(Color.WHITE); add(plot, BorderLayout.CENTER);
        note.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12)); add(note, BorderLayout.SOUTH);
        generate.addActionListener(e -> onGenerate.run()); export.addActionListener(e -> onExport.run());
        metric.addActionListener(e -> plot.repaint());
        azimuth.addChangeListener(e -> plot.repaint()); elevation.addChangeListener(e -> plot.repaint());
        reset.addActionListener(e -> { azimuth.setValue(40); elevation.setValue(35); });
        setAvailable(false);
    }

    public DStudyGrid grid() { return grid; }
    public void setGrid(DStudyGrid value) {
        grid = value;
        export.setEnabled(available && value != null);
        note.setText(value == null
            ? available ? "Set count ranges in D-study heatmap, then generate the grid here or there. Use at least 2 levels on each axis for a surface."
                        : "Run a balanced GENOVA D-study with two random facets to enable the 3D plot."
            : "Drag to rotate and tilt; hover for exact G and Phi. Height is on the coefficient axis; color spans the displayed grid values.");
        plot.repaint();
    }
    public void setAvailable(boolean value) {
        available = value; generate.setEnabled(value);
        if (!value) setGrid(null);
        else { export.setEnabled(grid != null); if (grid == null) setGrid(null); }
    }
    public void setBusy(boolean value) {
        generate.setEnabled(available && !value);
        export.setEnabled(available && !value && grid != null);
    }
    public BufferedImage image(int width, int height) {
        if (grid == null) throw new IllegalStateException("Generate a D-study grid before exporting the plot.");
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        plot.draw(graphics, width, height, false);
        graphics.dispose();
        return image;
    }

    private record Vertex(double x, double y, double z, double depth, int pixelX, int pixelY) {}
    private record Tile(Polygon polygon, double depth, double value, double light) {}

    private final class Plot extends JComponent {
        private Vertex[][] vertices;
        private int lastX, lastY;

        Plot() {
            setToolTipText(" ");
            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { lastX = e.getX(); lastY = e.getY(); }
                @Override public void mouseDragged(MouseEvent e) {
                    azimuth.setValue((azimuth.getValue() + (e.getX() - lastX) + 360) % 360);
                    elevation.setValue(Math.max(15, Math.min(75, elevation.getValue() - (e.getY() - lastY))));
                    lastX = e.getX(); lastY = e.getY();
                }
            };
            addMouseListener(mouse); addMouseMotionListener(mouse);
        }
        @Override public String getToolTipText(MouseEvent e) {
            if (grid == null || vertices == null) return null;
            int nearestX = -1, nearestY = -1, distance = 16 * 16;
            for (int y = 0; y < vertices.length; y++) for (int x = 0; x < vertices[y].length; x++) {
                Vertex point = vertices[y][x];
                int d = (point.pixelX - e.getX()) * (point.pixelX - e.getX())
                    + (point.pixelY - e.getY()) * (point.pixelY - e.getY());
                if (d < distance) { distance = d; nearestX = x; nearestY = y; }
            }
            if (nearestX < 0) return null;
            return String.format("%s=%d, %s=%d  ·  G=%.5f  ·  Phi=%.5f", grid.xFacet(),
                grid.xLevels()[nearestX], grid.yFacet(), grid.yLevels()[nearestY],
                grid.g()[nearestY][nearestX], grid.phi()[nearestY][nearestX]);
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            draw(g, getWidth(), getHeight(), true);
            g.dispose();
        }
        private void draw(Graphics2D g, int width, int height, boolean updateTooltip) {
            g.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, height, new Color(249, 251, 253)));
            g.fillRect(0, 0, width, height);
            if (grid == null || width < 200 || height < 180) {
                if (updateTooltip) vertices = null;
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
            g.setColor(new Color(27, 45, 62));
            g.drawString("D-study performance surface", 30, 38);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(new Color(93, 110, 128));
            g.drawString(grid.xFacet() + " × " + grid.yFacet() + "  ·  " +
                grid.xLevels().length * grid.yLevels().length + " computed scenarios  ·  " +
                metric.getSelectedItem(), 31, 61);
            double angle = Math.toRadians(azimuth.getValue()), tilt = Math.toRadians(elevation.getValue());
            double scale = Math.min((width - 285.0) / 3.0, (height - 130.0) / 2.0);
            double centerX = width * 0.49, centerY = height * 0.55;
            int nx = grid.xLevels().length, ny = grid.yLevels().length;
            double[][] values = metric.getSelectedIndex() == 0 ? grid.phi() : grid.g();
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (double[] row : values) for (double value : row) {
                min = Math.min(min, value); max = Math.max(max, value);
            }
            double axisMin = Math.min(0, min), axisMax = Math.max(1, max), axisSpan = axisMax - axisMin;
            Vertex[][] points = new Vertex[ny][nx];
            for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++)
                points[y][x] = project(coordinate(x, nx), coordinate(y, ny),
                    (values[y][x]-axisMin)/axisSpan, angle, tilt, scale, centerX, centerY);
            if (updateTooltip) vertices = points;

            // Floor contours use the calculated grid; the colored surface is visual interpolation.
            Vertex a = project(-1, -1, 0, angle, tilt, scale, centerX, centerY);
            Vertex b = project( 1, -1, 0, angle, tilt, scale, centerX, centerY);
            Vertex c = project( 1,  1, 0, angle, tilt, scale, centerX, centerY);
            Vertex d = project(-1,  1, 0, angle, tilt, scale, centerX, centerY);
            g.setColor(new Color(237, 243, 247)); g.fillPolygon(polygon(a, b, c, d));
            g.setColor(new Color(215, 226, 233)); g.setStroke(new BasicStroke(1f));
            for (int x = 0; x < nx; x++) line(g,
                project(coordinate(x, nx), -1, 0, angle, tilt, scale, centerX, centerY),
                project(coordinate(x, nx), 1, 0, angle, tilt, scale, centerX, centerY));
            for (int y = 0; y < ny; y++) line(g,
                project(-1, coordinate(y, ny), 0, angle, tilt, scale, centerX, centerY),
                project(1, coordinate(y, ny), 0, angle, tilt, scale, centerX, centerY));
            drawContours(g, values, min, max, angle, tilt, scale, centerX, centerY);

            // Only the two sides facing the viewer get translucent skirts.
            for (int x = 0; x < nx - 1; x++) {
                drawSkirt(g, points[0][x], points[0][x+1], angle, tilt, scale, centerX, centerY);
                drawSkirt(g, points[ny-1][x], points[ny-1][x+1], angle, tilt, scale, centerX, centerY);
            }
            for (int y = 0; y < ny - 1; y++) {
                drawSkirt(g, points[y][0], points[y+1][0], angle, tilt, scale, centerX, centerY);
                drawSkirt(g, points[y][nx-1], points[y+1][nx-1], angle, tilt, scale, centerX, centerY);
            }

            List<Tile> tiles = new ArrayList<>();
            final int subdivisions = 6;
            for (int y = 0; y < ny - 1; y++) for (int x = 0; x < nx - 1; x++)
                for (int sy = 0; sy < subdivisions; sy++) for (int sx = 0; sx < subdivisions; sx++) {
                    double u = sx/(double)subdivisions, v = sy/(double)subdivisions;
                    double u1 = (sx+1)/(double)subdivisions, v1 = (sy+1)/(double)subdivisions;
                    Vertex p = sample(values, x, y, u, v, axisMin, axisSpan, angle, tilt, scale, centerX, centerY);
                    Vertex q = sample(values, x, y, u1, v, axisMin, axisSpan, angle, tilt, scale, centerX, centerY);
                    Vertex r = sample(values, x, y, u1, v1, axisMin, axisSpan, angle, tilt, scale, centerX, centerY);
                    Vertex s = sample(values, x, y, u, v1, axisMin, axisSpan, angle, tilt, scale, centerX, centerY);
                    double slopeX = ((q.z-p.z)+(r.z-s.z))*subdivisions/2;
                    double slopeY = ((s.z-p.z)+(r.z-q.z))*subdivisions/2;
                    double light = Math.max(.75, Math.min(1.08, .96-.22*slopeX-.30*slopeY));
                    tiles.add(new Tile(polygon(p, q, r, s), (p.depth+q.depth+r.depth+s.depth)/4,
                        (p.z+q.z+r.z+s.z)*axisSpan/4+axisMin, light));
                }
            tiles.sort(Comparator.comparingDouble(Tile::depth));
            for (Tile tile : tiles) {
                g.setColor(shade(color(position(tile.value, min, max)), tile.light));
                g.fillPolygon(tile.polygon);
            }
            if (tiles.isEmpty()) {
                g.setColor(color(.55)); g.setStroke(new BasicStroke(3f));
                for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++) {
                    if (x > 0) line(g, points[y][x-1], points[y][x]);
                    if (y > 0) line(g, points[y-1][x], points[y][x]);
                }
            }
            g.setColor(new Color(255, 255, 255, 190)); g.setStroke(new BasicStroke(1.3f));
            for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++) {
                if (x > 0) line(g, points[y][x-1], points[y][x]);
                if (y > 0) line(g, points[y-1][x], points[y][x]);
            }
            g.setColor(new Color(255, 255, 255, 225));
            for (Vertex[] row : points) for (Vertex point : row)
                g.fillOval(point.pixelX-3, point.pixelY-3, 6, 6);

            g.setStroke(new BasicStroke(1.8f)); g.setColor(new Color(59, 77, 93));
            line(g, d, c); line(g, b, c);
            Vertex top = project(-1, 1, 1, angle, tilt, scale, centerX, centerY);
            line(g, d, top);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            FontMetrics fm = g.getFontMetrics();
            for (int x = 0; x < nx; x++) {
                Vertex tick = project(coordinate(x, nx), 1, 0, angle, tilt, scale, centerX, centerY);
                String label = Integer.toString(grid.xLevels()[x]);
                g.drawString(label, tick.pixelX - fm.stringWidth(label)/2, tick.pixelY + 21);
            }
            for (int y = 0; y < ny; y++) {
                Vertex tick = project(1, coordinate(y, ny), 0, angle, tilt, scale, centerX, centerY);
                String label = Integer.toString(grid.yLevels()[y]);
                g.drawString(label, tick.pixelX + 8, tick.pixelY + 5);
            }
            for (int i = 0; i <= 4; i++) {
                Vertex tick = project(-1, 1, i/4.0, angle, tilt, scale, centerX, centerY);
                String label = String.format("%.2f", axisMin + axisSpan*i/4.0);
                g.drawString(label, tick.pixelX - fm.stringWidth(label) - 9, tick.pixelY + 5);
            }
            Vertex xMid = project(0, 1, 0, angle, tilt, scale, centerX, centerY);
            Vertex yMid = project(1, 0, 0, angle, tilt, scale, centerX, centerY);
            g.setFont(g.getFont().deriveFont(Font.BOLD));
            g.drawString(grid.xFacet() + " levels", xMid.pixelX-25, xMid.pixelY+48);
            g.drawString(grid.yFacet() + " levels", yMid.pixelX+16, yMid.pixelY+30);
            g.drawString((String)metric.getSelectedItem(), top.pixelX-5, top.pixelY-10);

            int legendX = width - 95, legendY = (int)(height*.27), legendH = (int)(height*.40);
            for (int i = 0; i < legendH; i++) {
                g.setColor(color(1-i/(double)Math.max(1, legendH-1)));
                g.fillRect(legendX, legendY+i, 16, 1);
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12)); g.setColor(new Color(69, 86, 102));
            g.drawString(metric.getSelectedItem() + " range", legendX-1, legendY-12);
            g.drawString(String.format("%.3f", max), legendX+23, legendY+5);
            g.drawString(String.format("%.3f", min), legendX+23, legendY+legendH+4);
        }
    }

    private static double coordinate(int index, int count) { return count == 1 ? 0 : -1 + 2.0*index/(count-1); }
    private static Vertex sample(double[][] values, int x, int y, double u, double v,
                                 double axisMin, double axisSpan, double angle, double tilt,
                                 double scale, double centerX, double centerY) {
        int nx = values[0].length, ny = values.length;
        double z = values[y][x]*(1-u)*(1-v) + values[y][x+1]*u*(1-v)
            + values[y+1][x+1]*u*v + values[y+1][x]*(1-u)*v;
        double worldX = coordinate(x, nx) + (coordinate(x+1, nx)-coordinate(x, nx))*u;
        double worldY = coordinate(y, ny) + (coordinate(y+1, ny)-coordinate(y, ny))*v;
        return project(worldX, worldY, (z-axisMin)/axisSpan, angle, tilt, scale, centerX, centerY);
    }
    private static void drawSkirt(Graphics2D g, Vertex p, Vertex q, double angle, double tilt,
                                  double scale, double centerX, double centerY) {
        Vertex baseP = project(p.x, p.y, 0, angle, tilt, scale, centerX, centerY);
        Vertex baseQ = project(q.x, q.y, 0, angle, tilt, scale, centerX, centerY);
        if (baseP.depth + baseQ.depth <= 0) return;
        g.setColor(new Color(57, 117, 146, 84));
        g.fillPolygon(polygon(p, q, baseQ, baseP));
    }
    private static void drawContours(Graphics2D g, double[][] values, double min, double max,
                                     double angle, double tilt, double scale,
                                     double centerX, double centerY) {
        if (max-min < 1e-10) return;
        int nx = values[0].length, ny = values.length;
        g.setStroke(new BasicStroke(1.4f));
        for (int level = 1; level <= 4; level++) {
            double threshold = min + (max-min)*level/5.0;
            Color tint = color(level/5.0);
            g.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 145));
            for (int y = 0; y < ny-1; y++) for (int x = 0; x < nx-1; x++) {
                Point2D.Double[] corners = {
                    new Point2D.Double(coordinate(x,nx), coordinate(y,ny)),
                    new Point2D.Double(coordinate(x+1,nx), coordinate(y,ny)),
                    new Point2D.Double(coordinate(x+1,nx), coordinate(y+1,ny)),
                    new Point2D.Double(coordinate(x,nx), coordinate(y+1,ny))};
                double[] heights = {values[y][x], values[y][x+1], values[y+1][x+1], values[y+1][x]};
                List<Vertex> hits = new ArrayList<>(4);
                for (int edge = 0; edge < 4; edge++) {
                    int next = (edge+1)%4;
                    double first = heights[edge], second = heights[next];
                    if (!((first <= threshold && second > threshold) ||
                          (first > threshold && second <= threshold))) continue;
                    double t = (threshold-first)/(second-first);
                    double wx = corners[edge].x + (corners[next].x-corners[edge].x)*t;
                    double wy = corners[edge].y + (corners[next].y-corners[edge].y)*t;
                    hits.add(project(wx, wy, 0, angle, tilt, scale, centerX, centerY));
                }
                for (int i = 0; i+1 < hits.size(); i+=2) line(g, hits.get(i), hits.get(i+1));
            }
        }
    }
    private static Vertex project(double x, double y, double z, double angle, double tilt,
                                  double scale, double centerX, double centerY) {
        double across = x*Math.cos(angle) - y*Math.sin(angle);
        double depth = x*Math.sin(angle) + y*Math.cos(angle);
        return new Vertex(x, y, z, depth*Math.cos(tilt)+z*Math.sin(tilt),
            (int)Math.round(centerX + scale*across),
            (int)Math.round(centerY + scale*(depth*Math.sin(tilt)-z*Math.cos(tilt))));
    }
    private static Polygon polygon(Vertex... vertices) {
        Polygon polygon = new Polygon();
        for (Vertex point : vertices) polygon.addPoint(point.pixelX, point.pixelY);
        return polygon;
    }
    private static void line(Graphics2D g, Vertex a, Vertex b) {
        g.drawLine(a.pixelX, a.pixelY, b.pixelX, b.pixelY);
    }
    private static double position(double value, double min, double max) {
        return max-min < 1e-10 ? .5 : (value-min)/(max-min);
    }
    private static Color color(double value) {
        Color[] stops = {new Color(68, 1, 84), new Color(59, 82, 139),
            new Color(33, 145, 140), new Color(94, 201, 98), new Color(253, 231, 37)};
        double scaled = Math.max(0, Math.min(1, value))*4;
        int index = Math.min(3, (int)scaled);
        double mix = scaled-index;
        Color from = stops[index], to = stops[index+1];
        return new Color((int)(from.getRed()+(to.getRed()-from.getRed())*mix),
            (int)(from.getGreen()+(to.getGreen()-from.getGreen())*mix),
            (int)(from.getBlue()+(to.getBlue()-from.getBlue())*mix));
    }
    private static Color shade(Color color, double light) {
        return new Color((int)Math.min(255, color.getRed()*light),
            (int)Math.min(255, color.getGreen()*light),
            (int)Math.min(255, color.getBlue()*light));
    }
}
