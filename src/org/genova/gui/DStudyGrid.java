package org.genova.gui;

import java.util.*;

/** A small, auditable grid of balanced GENOVA D-study scenarios. */
public record DStudyGrid(String xFacet, String yFacet, int[] xLevels, int[] yLevels,
                         double[][] g, double[][] phi) {
    public static DStudyGrid run(DataTable data, String score, String object,
                                 Design.Prepared base, int xFrom, int xTo,
                                 int yFrom, int yTo) throws Exception {
        if (base.scenarios().isEmpty()) throw new IllegalArgumentException("Run a balanced GENOVA D-study first.");
        List<Design.Facet> random = base.facets().stream().skip(1).filter(f -> !f.fixed()).toList();
        if (random.size() != 2) throw new IllegalArgumentException("The heatmap requires exactly two random facets besides the object of measurement.");
        if (xFrom < 1 || yFrom < 1 || xTo < xFrom || yTo < yFrom)
            throw new IllegalArgumentException("Each facet range must contain positive counts in ascending order.");
        int nx = xTo - xFrom + 1, ny = yTo - yFrom + 1;
        if ((long) nx * ny > 30) throw new IllegalArgumentException("The heatmap can contain at most 30 cells. Reduce either range.");
        StringBuilder xs = new StringBuilder(), ys = new StringBuilder();
        int[] xv = new int[nx], yv = new int[ny];
        for (int x = 0; x < nx; x++) xv[x] = xFrom + x;
        for (int y = 0; y < ny; y++) yv[y] = yFrom + y;
        for (int y : yv) for (int x : xv) {
            if (!xs.isEmpty()) { xs.append(','); ys.append(','); }
            xs.append(x); ys.append(y);
        }
        List<Design.Facet> requested = new ArrayList<>();
        for (Design.Facet f : base.facets().subList(1, base.facets().size())) {
            String sizes = f.column().equals(random.get(0).column()) ? xs.toString()
                : f.column().equals(random.get(1).column()) ? ys.toString() : "";
            requested.add(new Design.Facet(f.column(), f.parent(), f.fixed(), sizes));
        }
        Design.Prepared plan = Design.prepare(data, score, object, requested, true);
        Results result = Engine.run(plan);
        if (result.decisions().rows().size() != nx * ny)
            throw new IllegalArgumentException("The engine returned an incomplete heatmap grid.");
        double[][] g = new double[ny][nx], phi = new double[ny][nx];
        for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++) {
            List<String> row = result.decisions().rows().get(y * nx + x);
            try {
                g[y][x] = Double.parseDouble(row.get(5));
                phi[y][x] = Double.parseDouble(row.get(6));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("A D-study coefficient is undefined; inspect the full report before plotting.");
            }
            if (!Double.isFinite(g[y][x]) || !Double.isFinite(phi[y][x]))
                throw new IllegalArgumentException("A D-study coefficient is undefined; inspect the full report before plotting.");
        }
        return new DStudyGrid(random.get(0).column(), random.get(1).column(), xv, yv, g, phi);
    }

    public String csv() {
        StringBuilder out = new StringBuilder(DataTable.csvRow(List.of(xFacet + " levels", yFacet + " levels", "G coefficient", "Phi")));
        for (int y = 0; y < yLevels.length; y++) for (int x = 0; x < xLevels.length; x++)
            out.append(DataTable.csvRow(List.of(Integer.toString(xLevels[x]), Integer.toString(yLevels[y]),
                Double.toString(g[y][x]), Double.toString(phi[y][x]))));
        return out.toString();
    }
}
