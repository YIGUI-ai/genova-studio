package org.genova.gui;

import java.util.*;

/** Complete random (P:C) x (I:R) G-study with unequal nested group sizes. */
public final class UrNestedCrossDesign {
    public record Observation(int c, int p, int r, int i, double score) {}
    public record Prepared(String scoreColumn, String pColumn, String cColumn,
                           String rColumn, String iColumn, List<Observation> observations,
                           int cLevels, int pLevels, int rLevels, int iLevels) {
        public Prepared { observations = List.copyOf(observations); }
        public String description() {
            return "Unbalanced random G-study: (" + pColumn + ":" + cColumn + ") × ("
                + iColumn + ":" + rColumn + ")";
        }
    }

    public static Prepared prepare(DataTable data, String score, String p, String c, String r, String i) {
        if (data == null) throw new IllegalArgumentException("Import a table before running the analysis.");
        List<String> selected = Arrays.asList(score, p, c, r, i);
        if (selected.contains(null) || !data.headers().containsAll(selected) || new HashSet<>(selected).size() != 5)
            throw new IllegalArgumentException("Choose five different existing columns for score, object P, grouping C, crossed group R, and nested facet I.");
        if (data.headers().size() != 5)
            throw new IllegalArgumentException("The (P:C) × (I:R) pilot accepts exactly five columns.");
        if (data.rows().size() > 5000)
            throw new IllegalArgumentException("The (P:C) × (I:R) pilot supports at most 5,000 observations.");
        int si = data.headers().indexOf(score), pi = data.headers().indexOf(p), ci = data.headers().indexOf(c);
        int ri = data.headers().indexOf(r), ii = data.headers().indexOf(i);
        Map<String,Integer> cs = new LinkedHashMap<>(), ps = new LinkedHashMap<>(), rs = new LinkedHashMap<>(), is = new LinkedHashMap<>();
        Map<Integer,Set<Integer>> pByC = new HashMap<>(), iByR = new HashMap<>();
        Set<Long> seen = new HashSet<>();
        List<Observation> observations = new ArrayList<>();
        for (int row = 0; row < data.rows().size(); row++) {
            List<String> record = data.rows().get(row);
            if (record.get(pi).isBlank() || record.get(ci).isBlank() || record.get(ri).isBlank() || record.get(ii).isBlank())
                throw new IllegalArgumentException("Row " + (row+2) + " has a blank facet or object ID.");
            double value;
            try { value = Double.parseDouble(record.get(si)); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Row " + (row+2) + " has a nonnumeric score."); }
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Row " + (row+2) + " must have a finite score.");
            int cv = cs.computeIfAbsent(record.get(ci), ignored -> cs.size());
            int pv = ps.computeIfAbsent(record.get(ci) + "\u0000" + record.get(pi), ignored -> ps.size());
            int rv = rs.computeIfAbsent(record.get(ri), ignored -> rs.size());
            int iv = is.computeIfAbsent(record.get(ri) + "\u0000" + record.get(ii), ignored -> is.size());
            pByC.computeIfAbsent(cv, ignored -> new HashSet<>()).add(pv);
            iByR.computeIfAbsent(rv, ignored -> new HashSet<>()).add(iv);
            long cell = ((long)pv << 32) | (iv & 0xffffffffL);
            if (!seen.add(cell)) throw new IllegalArgumentException("Row " + (row+2) + " duplicates a P × I design cell.");
            observations.add(new Observation(cv,pv,rv,iv,value));
        }
        if (cs.size() < 2 || rs.size() < 2 || pByC.values().stream().anyMatch(levels -> levels.size() < 2)
            || iByR.values().stream().anyMatch(levels -> levels.size() < 2))
            throw new IllegalArgumentException("At least two C and R groups, with two nested P or I levels per group, are required.");
        if ((long)ps.size() * is.size() != observations.size())
            throw new IllegalArgumentException("The (P:C) × (I:R) pilot requires every P × I cell exactly once; missing cells are not supported.");
        return new Prepared(score,p,c,r,i,observations,cs.size(),ps.size(),rs.size(),is.size());
    }
}
