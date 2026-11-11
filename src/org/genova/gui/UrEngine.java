package org.genova.gui;

import java.util.*;

/** Henderson Method 1 / analogous ANOVA for the unbalanced one-way random model only. */
public final class UrEngine {
    private record Summary(int n, double mean, double withinSS) {}

    private static Summary summarize(List<Double> values) {
        int n = 0;
        double mean = 0, m2 = 0;
        for (double x : values) {
            n++;
            double delta = x - mean;
            mean += delta / n;
            m2 += delta * (x - mean);
        }
        return new Summary(n, mean, m2);
    }
    private static String number(double x) { return String.format(Locale.ROOT, "%.8f", x); }

    public static Results run(UrDesign.Prepared design) {
        List<Summary> summaries = design.groups().stream().map(g -> summarize(g.scores())).toList();
        int a = summaries.size(), n = design.observations();
        double weightedTotal = 0, withinSS = 0, sumSquaresN = 0;
        for (Summary group : summaries) {
            weightedTotal += group.n() * group.mean();
            withinSS += group.withinSS();
            sumSquaresN += (double) group.n() * group.n();
        }
        double grand = weightedTotal / n, betweenSS = 0;
        for (Summary group : summaries) betweenSS += group.n() * Math.pow(group.mean() - grand, 2);
        int betweenDf = a - 1, withinDf = n - a;
        double betweenMS = betweenSS / betweenDf, withinMS = withinSS / withinDf;
        double effectiveN = (n - sumSquaresN / n) / betweenDf;
        if (!(effectiveN > 0) || !Double.isFinite(betweenMS) || !Double.isFinite(withinMS))
            throw new IllegalArgumentException("This data set cannot produce finite one-way ANOVA estimates.");
        double objectVariance = (betweenMS - withinMS) / effectiveN;
        String nested = design.replicateColumn() + " : " + design.objectColumn();
        Results.Table anova = new Results.Table(List.of("Effect", "df", "SS", "MS"), List.of(
            List.of(design.objectColumn(), Integer.toString(betweenDf), number(betweenSS), number(betweenMS)),
            List.of(nested, Integer.toString(withinDf), number(withinSS), number(withinMS))));
        Results.Table components = new Results.Table(List.of("Effect", "df", "Estimate", "EMS", "Type"), List.of(
            List.of(design.objectColumn(), Integer.toString(betweenDf), number(objectVariance),
                "σ²_nested + " + number(effectiveN) + " × σ²_object", "Random effect"),
            List.of(nested, Integer.toString(withinDf), number(withinMS), "σ²_nested", "Random effect")));
        String notice = "Pilot scope: unbalanced one-way random-effects G-study only; no D-study or crossed/multifacet model.";
        if (objectVariance < 0) notice += " The object variance estimate is negative and has not been truncated.";
        String report = "GENOVA Studio · urGENOVA pilot\n\n" + design.description() + "\n"
            + "Method: one-way random-effects analogous ANOVA (Henderson Method 1).\n"
            + "Objects: " + a + "; observations: " + n + "; grand mean: " + number(grand) + "\n"
            + "Effective replicate count n0 = (N − Σ n_j²/N)/(a − 1) = " + number(effectiveN) + "\n"
            + "σ²_object = (MS_object − MS_nested)/n0; σ²_nested = MS_nested.\n\n"
            + anova.csv() + "\n" + components.csv() + "\n" + notice + "\n"
            + "This pilot has not been validated against the original urGENOVA executable.\n";
        return new Results(anova, components, new Results.Table(List.of(), List.of()), report, notice);
    }
}
