package org.genova.gui;

import java.util.*;

/** Random P x (I:H), one score per present P-I cell and optional missing cells. */
public final class UrNestedMissingDesign {
    public record Observation(int p, int h, int i, double score) {}
    public record Prepared(String pColumn, String hColumn, String iColumn, String scoreColumn,
                           List<Observation> observations, int pLevels, int hLevels, int iLevels,
                           int missingCells) {
        public Prepared { observations = List.copyOf(observations); }
        public String description() {
            return "Unbalanced random G-study: " + pColumn + " × (" + iColumn + ":" + hColumn + ") with missing cells";
        }
    }
    public static Prepared prepare(DataTable data, String score, String p, String h, String i) {
        if (data == null) throw new IllegalArgumentException("Import a table before running the analysis.");
        List<String> selected = Arrays.asList(score,p,h,i);
        if (selected.contains(null) || !data.headers().containsAll(selected) || new HashSet<>(selected).size() != 4)
            throw new IllegalArgumentException("Choose four different existing columns for score, object P, group H, and facet I nested in H.");
        if (data.headers().size() != 4)
            throw new IllegalArgumentException("The P × (I:H) pilot accepts exactly four columns.");
        if (data.rows().size() > 5000)
            throw new IllegalArgumentException("The P × (I:H) pilot supports at most 5,000 observations.");
        int si=data.headers().indexOf(score), pi=data.headers().indexOf(p), hi=data.headers().indexOf(h), ii=data.headers().indexOf(i);
        Map<String,Integer> ps=new LinkedHashMap<>(), hs=new LinkedHashMap<>();
        Map<List<String>,Integer> is=new LinkedHashMap<>();
        Map<Integer,Set<Integer>> iByH=new HashMap<>();
        Set<Long> seen=new HashSet<>();
        List<Observation> observations=new ArrayList<>();
        for (int row=0; row<data.rows().size(); row++) {
            List<String> record=data.rows().get(row);
            if (record.get(pi).isBlank() || record.get(hi).isBlank() || record.get(ii).isBlank())
                throw new IllegalArgumentException("Row " +(row+2)+" has a blank object or facet ID.");
            double value;
            try { value=Double.parseDouble(record.get(si)); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Row " +(row+2)+" has a nonnumeric score."); }
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Row " +(row+2)+" must have a finite score.");
            int pv=ps.computeIfAbsent(record.get(pi),ignored->ps.size());
            int hv=hs.computeIfAbsent(record.get(hi),ignored->hs.size());
            int iv=is.computeIfAbsent(List.of(record.get(hi),record.get(ii)),ignored->is.size());
            iByH.computeIfAbsent(hv,ignored->new HashSet<>()).add(iv);
            if (!seen.add(((long)pv<<32)|(iv&0xffffffffL)))
                throw new IllegalArgumentException("Row " +(row+2)+" duplicates a P × I:H design cell.");
            observations.add(new Observation(pv,hv,iv,value));
        }
        int pCount=ps.size(), hCount=hs.size(), iCount=is.size(), n=observations.size();
        if (pCount<2 || hCount<2 || iByH.values().stream().anyMatch(levels->levels.size()<2))
            throw new IllegalArgumentException("At least two P and H levels, with two nested I levels per H group, are required.");
        if ((long)pCount*iCount>10_000)
            throw new IllegalArgumentException("The P × (I:H) pilot supports at most 10,000 possible cells.");
        int[][] phCounts=new int[pCount][hCount];
        for (Observation o:observations) phCounts[o.p()][o.h()]++;
        for (int[] row:phCounts) for (int count:row) if (count<2)
            throw new IllegalArgumentException("Every P × H block must contain at least two observed I levels in this pilot.");
        if (n-pCount*hCount-iCount+hCount<=0)
            throw new IllegalArgumentException("Positive P × I:H interaction degrees of freedom are required.");
        return new Prepared(p,h,i,score,observations,pCount,hCount,iCount,pCount*iCount-n);
    }
}
