package org.genova.gui;

import java.util.*;

/** Analogous ANOVA / Henderson Method 1 for complete random (P:C) x (I:R). */
public final class UrNestedCrossEngine {
    /** T order: mean,c,p:c,r,i:r,cr,ci:r,pr:c,pi:cr. VC order omits mean. */
    public record Analysis(Results results, double[] t, double[] ss, double[] ms,
                           double[] vc, double[][] et, double[][] ems) {}
    private static final int[][] AXES = {{0,0},{1,0},{2,0},{0,1},{0,2},{1,1},{1,2},{2,1},{2,2}};
    private static final int[][] CONTRASTS = {
        {-1,1,0,0,0,0,0,0,0}, {0,-1,1,0,0,0,0,0,0},
        {-1,0,0,1,0,0,0,0,0}, {0,0,0,-1,1,0,0,0,0},
        {1,-1,0,-1,0,1,0,0,0}, {0,0,0,1,-1,-1,1,0,0},
        {0,1,-1,0,0,-1,0,1,0}, {0,0,0,0,0,1,-1,-1,1}
    };
    private static String number(double value) { return String.format(Locale.ROOT,"%.8f",value); }
    private static String coefficient(double value) { return String.format(Locale.ROOT,"%.6f",value); }
    private static long group(UrNestedCrossDesign.Observation o, int[] axes) {
        int left = switch (axes[0]) { case 1 -> o.c(); case 2 -> o.p(); default -> 0; };
        int right = switch (axes[1]) { case 1 -> o.r(); case 2 -> o.i(); default -> 0; };
        return ((long)left << 32) | (right & 0xffffffffL);
    }
    /** E[T_g]/VC_v = sum_g sum_v n(g intersect v)^2 / n(g). */
    private static double trace(List<UrNestedCrossDesign.Observation> observations, int[] tAxes, int[] vcAxes) {
        Map<Long,Integer> sizes = new HashMap<>();
        Map<Long,Map<Long,Integer>> intersections = new HashMap<>();
        for (var o : observations) {
            long g = group(o,tAxes), v = group(o,vcAxes);
            sizes.merge(g,1,Integer::sum);
            intersections.computeIfAbsent(g,ignored -> new HashMap<>()).merge(v,1,Integer::sum);
        }
        double result = 0;
        for (var entry : intersections.entrySet()) {
            double numerator = 0;
            for (int count : entry.getValue().values()) numerator += (double)count*count;
            result += numerator/sizes.get(entry.getKey());
        }
        return result;
    }
    private static double[] solve(double[][] ems, double[] ms) {
        int n = ms.length;
        double[][] a = new double[n][n+1];
        for (int row = 0; row < n; row++) {
            System.arraycopy(ems[row],0,a[row],0,n); a[row][n] = ms[row];
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col+1; row < n; row++)
                if (Math.abs(a[row][col]) > Math.abs(a[pivot][col])) pivot = row;
            if (Math.abs(a[pivot][col]) < 1e-10)
                throw new IllegalArgumentException("Expected-mean-square equations are singular for this design.");
            double[] swap = a[col]; a[col] = a[pivot]; a[pivot] = swap;
            double scale = a[col][col];
            for (int j = col; j <= n; j++) a[col][j] /= scale;
            for (int row = 0; row < n; row++) if (row != col) {
                double factor = a[row][col];
                for (int j = col; j <= n; j++) a[row][j] -= factor*a[col][j];
            }
        }
        double[] result = new double[n];
        for (int row = 0; row < n; row++) {
            result[row] = a[row][n];
            if (!Double.isFinite(result[row])) throw new IllegalArgumentException("Variance-component equations yielded nonfinite estimates.");
        }
        return result;
    }
    private static String equation(double[] coefficients, String[] effects) {
        StringBuilder text = new StringBuilder();
        for (int col = effects.length-1; col >= 0; col--) {
            double value = coefficients[col];
            if (Math.abs(value) < 1e-10) continue;
            if (!text.isEmpty()) text.append(value < 0 ? " - " : " + ");
            else if (value < 0) text.append('-');
            text.append(coefficient(Math.abs(value))).append(" × VC(").append(effects[col]).append(')');
        }
        return text.toString();
    }
    public static Results run(UrNestedCrossDesign.Prepared design) { return calculate(design).results(); }
    public static Analysis calculate(UrNestedCrossDesign.Prepared design) {
        var observations = design.observations();
        int c = design.cLevels(), p = design.pLevels(), r = design.rLevels(), i = design.iLevels();
        int[] df = {c-1,p-c,r-1,i-r,(c-1)*(r-1),(c-1)*(i-r),(p-c)*(r-1),(p-c)*(i-r)};
        double[] t = new double[9];
        double[][] et = new double[9][8];
        for (int term = 0; term < AXES.length; term++) {
            Map<Long,double[]> groups = new HashMap<>();
            for (var o : observations) {
                double[] values = groups.computeIfAbsent(group(o,AXES[term]),ignored -> new double[2]);
                values[0] += o.score(); values[1]++;
            }
            for (double[] values : groups.values()) t[term] += values[0]*values[0]/values[1];
            for (int effect = 0; effect < 8; effect++) et[term][effect] = trace(observations,AXES[term],AXES[effect+1]);
        }
        double[] ss = new double[8], ms = new double[8];
        double[][] ems = new double[8][8];
        for (int effect = 0; effect < 8; effect++) {
            for (int term = 0; term < 9; term++) {
                ss[effect] += CONTRASTS[effect][term]*t[term];
                for (int component = 0; component < 8; component++)
                    ems[effect][component] += CONTRASTS[effect][term]*et[term][component];
            }
            ms[effect] = ss[effect]/df[effect];
            for (int component = 0; component < 8; component++) ems[effect][component] /= df[effect];
        }
        double[] vc = solve(ems,ms);
        String[] effects = {design.cColumn(),design.pColumn()+":"+design.cColumn(),design.rColumn(),
            design.iColumn()+":"+design.rColumn(), design.cColumn()+"×"+design.rColumn(),
            design.cColumn()+"×"+design.iColumn()+":"+design.rColumn(),
            design.pColumn()+"×"+design.rColumn()+":"+design.cColumn(),
            design.pColumn()+"×"+design.iColumn()+":"+design.cColumn()+design.rColumn()};
        List<List<String>> anovaRows = new ArrayList<>(), componentRows = new ArrayList<>();
        for (int effect = 0; effect < 8; effect++) {
            anovaRows.add(List.of(effects[effect],Integer.toString(df[effect]),number(t[effect+1]),
                number(ss[effect]),number(ms[effect]),number(vc[effect])));
            componentRows.add(List.of(effects[effect],Integer.toString(df[effect]),number(vc[effect]),
                equation(ems[effect],effects),"Random effect"));
        }
        Results.Table anova = new Results.Table(List.of("Effect","df","T","SS","MS","VC"),anovaRows);
        Results.Table components = new Results.Table(List.of("Effect","df","Estimate","EMS","Type"),componentRows);
        String notice = "Experimental complete random (P:C) × (I:R) G-study with unequal nested group sizes; no D-study. Benchmarked against urGENOVA manual Appendix A, not the original executable.";
        if (Arrays.stream(vc).anyMatch(value -> value < 0)) notice += " Negative variance estimates are retained, not truncated.";
        StringBuilder report = new StringBuilder("GENOVA Studio · urGENOVA nested-cross Java pilot\n\n")
            .append(design.description()).append("\nMethod: analogous ANOVA / Henderson Method 1.\n")
            .append("Levels: C=").append(c).append(", P=").append(p).append(", R=").append(r)
            .append(", I=").append(i).append("; observations: ").append(observations.size()).append("\n")
            .append("Grand mean: ").append(number(observations.stream().mapToDouble(UrNestedCrossDesign.Observation::score).sum()/observations.size()))
            .append("\nT(mean): ").append(number(t[0])).append("; total SS: ").append(number(t[8]-t[0]))
            .append("\n\n").append(anova.csv()).append("\nExpected mean square equations:\n");
        for (int effect = 0; effect < 8; effect++) report.append(effects[effect]).append(": ")
            .append(equation(ems[effect],effects)).append('\n');
        report.append("\n").append(notice).append('\n');
        return new Analysis(new Results(anova,components,new Results.Table(List.of(),List.of()),
            report.toString(),notice),t,ss,ms,vc,et,ems);
    }
}
