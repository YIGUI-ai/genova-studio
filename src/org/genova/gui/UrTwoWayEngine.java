package org.genova.gui;

import java.util.*;

/** Analogous ANOVA / Henderson Method 1 for random P x H with R nested in each cell. */
public final class UrTwoWayEngine {
    /** T order: p, h, ph, r:ph, mean. VC order: p, h, ph, r:ph. */
    public record Analysis(Results results, double[] t, double[] ss, double[] ms,
                           double[] vc, double[][] et, double[][] ems) {}

    private static String number(double value) { return String.format(Locale.ROOT, "%.8f", value); }
    private static String coefficient(double value) { return String.format(Locale.ROOT, "%.6f", value); }
    private static double[] combine(double[] a, double[] b, double scale, int df) {
        double[] result = new double[a.length];
        for (int k = 0; k < a.length; k++) result[k] = (a[k] + scale * b[k]) / df;
        return result;
    }
    private static String equation(double[] coefficients, String p, String h, String ph, String residual) {
        String[] effects = {residual, ph, h, p};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < coefficients.length; i++) {
            double value = coefficients[i];
            if (i > 0) result.append(value < 0 ? " - " : " + ");
            else if (value < 0) result.append('-');
            result.append(coefficient(Math.abs(value))).append(" × VC(").append(effects[i]).append(')');
        }
        return result.toString();
    }
    private static double[] solve(double[][] ems, double[] ms) {
        double[][] matrix = new double[4][5];
        // Convert residual/ph/h/p columns to the user-facing p/h/ph/residual order.
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) matrix[row][col] = ems[row][3-col];
            matrix[row][4] = ms[row];
        }
        for (int col = 0; col < 4; col++) {
            int pivot = col;
            for (int row = col + 1; row < 4; row++)
                if (Math.abs(matrix[row][col]) > Math.abs(matrix[pivot][col])) pivot = row;
            if (Math.abs(matrix[pivot][col]) < 1e-10)
                throw new IllegalArgumentException("The expected-mean-square equations are singular for this design; variance components cannot be estimated.");
            double[] swap = matrix[col]; matrix[col] = matrix[pivot]; matrix[pivot] = swap;
            double scale = matrix[col][col];
            for (int k = col; k < 5; k++) matrix[col][k] /= scale;
            for (int row = 0; row < 4; row++) if (row != col) {
                double factor = matrix[row][col];
                for (int k = col; k < 5; k++) matrix[row][k] -= factor * matrix[col][k];
            }
        }
        double[] vc = new double[4];
        for (int row = 0; row < 4; row++) {
            vc[row] = matrix[row][4];
            if (!Double.isFinite(vc[row])) throw new IllegalArgumentException("Variance-component equations yielded nonfinite estimates.");
        }
        return vc;
    }

    public static Results run(UrTwoWayDesign.Prepared design) { return calculate(design).results(); }

    public static Analysis calculate(UrTwoWayDesign.Prepared design) {
        int a = design.pLevels().size(), b = design.hLevels().size(), n = design.observations();
        int[][] counts = design.counts(); double[][] sums = design.sums();
        int[] pN = new int[a], hN = new int[b];
        double[] pSum = new double[a], hSum = new double[b];
        double tPh = 0, pCross = 0, hCross = 0, phMean = 0;
        for (int i = 0; i < a; i++) for (int j = 0; j < b; j++) {
            pN[i] += counts[i][j]; hN[j] += counts[i][j];
            pSum[i] += sums[i][j]; hSum[j] += sums[i][j];
            if (counts[i][j] > 0) tPh += sums[i][j] * sums[i][j] / counts[i][j];
            phMean += (double)counts[i][j] * counts[i][j] / n;
        }
        for (int i = 0; i < a; i++) for (int j = 0; j < b; j++) {
            pCross += (double)counts[i][j] * counts[i][j] / pN[i];
            hCross += (double)counts[i][j] * counts[i][j] / hN[j];
        }
        double tP = 0, tH = 0, pMean = 0, hMean = 0;
        for (int i = 0; i < a; i++) {
            tP += pSum[i] * pSum[i] / pN[i];
            pMean += (double)pN[i] * pN[i] / n;
        }
        for (int j = 0; j < b; j++) {
            tH += hSum[j] * hSum[j] / hN[j];
            hMean += (double)hN[j] * hN[j] / n;
        }
        double tR = design.rawSquares(), tMean = design.total() * design.total() / n;
        double[] t = {tP, tH, tPh, tR, tMean};
        int[] df = {a-1, b-1, (a-1)*(b-1), n-a*b};
        double[] ss = {tP-tMean, tH-tMean, tPh-tP-tH+tMean, tR-tPh};
        double[] ms = new double[4];
        for (int row = 0; row < 4; row++) ms[row] = ss[row] / df[row];

        // VC column order: residual, ph, h, p. The nominal a*b in ET(ph)
        // reproduces urGENOVA's printed empty-cell example (Appendix E).
        double[][] et = {
            {a, pCross, pCross, n},
            {b, hCross, n, hCross},
            {a*b, n, n, n},
            {n, n, n, n},
            {1, phMean, hMean, pMean}
        };
        double[][] ems = new double[4][4];
        ems[0] = combine(et[0], et[4], -1, df[0]);
        ems[1] = combine(et[1], et[4], -1, df[1]);
        for (int k = 0; k < 4; k++) ems[2][k] = (et[2][k]-et[0][k]-et[1][k]+et[4][k])/df[2];
        ems[3] = combine(et[3], et[2], -1, df[3]);
        double[] vc = solve(ems, ms);
        String p = design.pColumn(), h = design.hColumn();
        String ph = p + " × " + h, residual = design.replicateColumn() + " : (" + ph + ")";
        String[] effects = {p, h, ph, residual};
        List<List<String>> anovaRows = new ArrayList<>(), componentRows = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            anovaRows.add(List.of(effects[row], Integer.toString(df[row]), number(t[row]),
                number(ss[row]), number(ms[row]), number(vc[row])));
            componentRows.add(List.of(effects[row], Integer.toString(df[row]), number(vc[row]),
                equation(ems[row], p, h, ph, residual), "Random effect"));
        }
        Results.Table anova = new Results.Table(List.of("Effect", "df", "T", "SS", "MS", "VC"), anovaRows);
        Results.Table components = new Results.Table(List.of("Effect", "df", "Estimate", "EMS", "Type"), componentRows);
        StringBuilder report = new StringBuilder("GENOVA Studio · urGENOVA two-way Java pilot\n\n")
            .append(design.description()).append("\nMethod: analogous ANOVA / Henderson Method 1.\n")
            .append("Levels: ").append(a).append(" × ").append(b).append("; observations: ").append(n)
            .append("; empty cells: ").append(design.emptyCells()).append("\n")
            .append("Grand mean: ").append(number(design.total()/n)).append("\n")
            .append("T(mean): ").append(number(tMean)).append("; total SS: ").append(number(tR-tMean)).append("\n\n")
            .append(anova.csv()).append("\nExpected T coefficients, in VC order [residual, ph, h, p]:\n");
        for (int row = 0; row < et.length; row++) {
            report.append(row == 4 ? "Mean" : effects[row]).append(": ");
            for (int col = 0; col < 4; col++) report.append(coefficient(et[row][col])).append(col == 3 ? "\n" : ", ");
        }
        report.append("\nExpected mean square equations:\n");
        for (int row = 0; row < 4; row++) report.append(effects[row]).append(": ")
            .append(equation(ems[row], p, h, ph, residual)).append('\n');
        String notice = "Experimental random two-way G-study; no D-study. Benchmarked against urGENOVA manual Appendices D and E, not the original executable.";
        if (design.emptyCells() > 0) notice += " Empty cells use the manual's nominal crossed-cell degrees of freedom.";
        if (Arrays.stream(vc).anyMatch(value -> value < 0)) notice += " Negative variance estimates are retained, not truncated.";
        report.append("\n").append(notice).append('\n');
        Results results = new Results(anova, components, new Results.Table(List.of(), List.of()),
            report.toString(), notice);
        return new Analysis(results, t, ss, ms, vc, et, ems);
    }
}
