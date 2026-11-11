package org.genova.gui;

import java.util.*;

/** One observation in each present P x I cell; absent rows denote missing cells. */
public final class UrCrossedMissingDesign {
    public record Observation(int p, int i, double score) {}
    public record Prepared(String pColumn, String iColumn, String scoreColumn,
                           List<Observation> observations, int pLevels, int iLevels, int missingCells) {
        public Prepared { observations = List.copyOf(observations); }
        public String description() { return "Unbalanced random G-study: " + pColumn + " × " + iColumn + " with missing cells"; }
    }
    public static Prepared prepare(DataTable data, String score, String p, String i) {
        if (data == null) throw new IllegalArgumentException("Import a table before running the analysis.");
        List<String> selected = Arrays.asList(score,p,i);
        if (selected.contains(null) || !data.headers().containsAll(selected) || new HashSet<>(selected).size() != 3)
            throw new IllegalArgumentException("Choose three different existing columns for score, object P, and crossed facet I.");
        if (data.headers().size() != 3)
            throw new IllegalArgumentException("The crossed missing-cell pilot accepts exactly three columns.");
        int si = data.headers().indexOf(score), pi = data.headers().indexOf(p), ii = data.headers().indexOf(i);
        Map<String,Integer> ps = new LinkedHashMap<>(), is = new LinkedHashMap<>();
        Set<Long> seen = new HashSet<>();
        List<Observation> observations = new ArrayList<>();
        for (int row = 0; row < data.rows().size(); row++) {
            List<String> record = data.rows().get(row);
            if (record.get(pi).isBlank() || record.get(ii).isBlank())
                throw new IllegalArgumentException("Row " + (row+2) + " has a blank object or facet ID.");
            double value;
            try { value = Double.parseDouble(record.get(si)); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Row " + (row+2) + " has a nonnumeric score."); }
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Row " + (row+2) + " must have a finite score.");
            int pv = ps.computeIfAbsent(record.get(pi),ignored -> ps.size());
            int iv = is.computeIfAbsent(record.get(ii),ignored -> is.size());
            long cell = ((long)pv << 32) | (iv & 0xffffffffL);
            if (!seen.add(cell)) throw new IllegalArgumentException("Row " + (row+2) + " duplicates a P × I design cell.");
            observations.add(new Observation(pv,iv,value));
        }
        int pCount = ps.size(), iCount = is.size(), n = observations.size();
        if (pCount < 2 || iCount < 2)
            throw new IllegalArgumentException("At least two levels of P and I are required.");
        if ((long)pCount*iCount > 10_000)
            throw new IllegalArgumentException("The crossed missing-cell pilot supports at most 10,000 possible cells.");
        if (n <= pCount+iCount-1)
            throw new IllegalArgumentException("Positive interaction degrees of freedom are required; too many cells are missing.");
        // A disconnected P-I incidence graph confounds object and facet groups.
        int[] parent = new int[pCount+iCount];
        for (int j = 0; j < parent.length; j++) parent[j] = j;
        for (Observation observation : observations) {
            int left = observation.p(), right = pCount+observation.i();
            while (parent[left] != left) left = parent[left];
            while (parent[right] != right) right = parent[right];
            parent[right] = left;
        }
        for (int j = 0; j < parent.length; j++) {
            int root = j;
            while (parent[root] != root) root = parent[root];
            if (root != 0) {
                int first = 0;
                while (parent[first] != first) first = parent[first];
                if (root != first)
                    throw new IllegalArgumentException("The observed P × I incidence graph must be connected; disconnected groups confound the effects.");
            }
        }
        return new Prepared(p,i,score,observations,pCount,iCount,pCount*iCount-n);
    }
}
