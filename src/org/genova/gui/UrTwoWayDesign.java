package org.genova.gui;

import java.util.*;

/** Complete random-effects P x H model with observations nested in each P-H cell. */
public final class UrTwoWayDesign {
    public record Prepared(String pColumn, String hColumn, String replicateColumn,
                           String scoreColumn, List<String> pLevels, List<String> hLevels,
                           int[][] counts, double[][] sums, double total, double rawSquares,
                           int observations, int emptyCells) {
        public Prepared {
            pLevels = List.copyOf(pLevels);
            hLevels = List.copyOf(hLevels);
        }
        public String description() {
            return "Unbalanced random G-study: " + pColumn + " × " + hColumn
                + " with " + replicateColumn + " : (" + pColumn + " × " + hColumn + ")";
        }
    }

    public static Prepared prepare(DataTable data, String score, String h, String p, String replicate) {
        if (data == null) throw new IllegalArgumentException("Import a table before running the analysis.");
        if (score == null || h == null || p == null || replicate == null ||
            !data.headers().containsAll(List.of(score, h, p, replicate)) ||
            new HashSet<>(List.of(score, h, p, replicate)).size() != 4)
            throw new IllegalArgumentException("Choose four different existing columns for score, object, crossed facet, and nested replicate.");
        if (data.headers().size() != 4)
            throw new IllegalArgumentException("The two-way pilot accepts exactly four columns: crossed facet, object, nested replicate, and score.");
        int si = data.headers().indexOf(score), hi = data.headers().indexOf(h), pi = data.headers().indexOf(p), ri = data.headers().indexOf(replicate);
        LinkedHashMap<String,Integer> pIndex = new LinkedHashMap<>(), hIndex = new LinkedHashMap<>();
        Set<List<String>> seen = new HashSet<>();
        List<Double> values = new ArrayList<>(data.rows().size());
        for (int row = 0; row < data.rows().size(); row++) {
            List<String> record = data.rows().get(row);
            String pv = record.get(pi), hv = record.get(hi), rv = record.get(ri);
            if (pv.isBlank() || hv.isBlank() || rv.isBlank())
                throw new IllegalArgumentException("Row " + (row + 2) + " has a blank facet, object, or replicate ID.");
            if (!seen.add(List.of(pv, hv, rv)))
                throw new IllegalArgumentException("Row " + (row + 2) + " duplicates a crossed-facet/object/replicate cell.");
            double value;
            try { value = Double.parseDouble(record.get(si)); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Row " + (row + 2) + " has a nonnumeric score."); }
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Row " + (row + 2) + " must have a finite score.");
            pIndex.computeIfAbsent(pv, ignored -> pIndex.size());
            hIndex.computeIfAbsent(hv, ignored -> hIndex.size());
            values.add(value);
        }
        int a = pIndex.size(), b = hIndex.size(), n = data.rows().size();
        if (a < 2 || b < 2) throw new IllegalArgumentException("At least two levels of each crossed factor are required.");
        if ((long)a * b > 10_000) throw new IllegalArgumentException("The two-way pilot supports at most 10,000 possible crossed cells.");
        if (n <= a * b)
            throw new IllegalArgumentException("At least one residual degree of freedom is required: observations must exceed the number of possible crossed cells.");
        int[][] counts = new int[a][b];
        double[][] sums = new double[a][b];
        double total = 0, rawSquares = 0;
        for (int row = 0; row < n; row++) {
            List<String> record = data.rows().get(row);
            int i = pIndex.get(record.get(pi)), j = hIndex.get(record.get(hi));
            double value = values.get(row);
            counts[i][j]++;
            sums[i][j] += value;
            total += value;
            rawSquares += value * value;
        }
        if (!Double.isFinite(total) || !Double.isFinite(rawSquares))
            throw new IllegalArgumentException("The score magnitudes are too large for finite sums of squares.");
        int empty = 0;
        for (int[] row : counts) for (int count : row) if (count == 0) empty++;
        if (empty > 1)
            throw new IllegalArgumentException("The two-way pilot currently supports at most one empty crossed cell.");
        return new Prepared(p, h, replicate, score, new ArrayList<>(pIndex.keySet()),
            new ArrayList<>(hIndex.keySet()), counts, sums, total, rawSquares, n, empty);
    }
}
