package org.genova.gui;

import java.util.*;

/** Analogous ANOVA / Henderson Method 1 for random P x I with absent cells. */
public final class UrCrossedMissingEngine {
    /** T order: p,i,pi,mean. VC order: p,i,pi. */
    public record Analysis(Results results, double[] t, double[] ss, double[] ms,
                           double[] vc, double[][] et, double[][] ems) {}
    private static String number(double value) { return String.format(Locale.ROOT,"%.8f",value); }
    private static String coefficient(double value) { return String.format(Locale.ROOT,"%.6f",value); }
    private static double[] solve(double[][] ems, double[] ms) {
        double[][] a = new double[3][4];
        for (int row = 0; row < 3; row++) {
            System.arraycopy(ems[row],0,a[row],0,3); a[row][3] = ms[row];
        }
        for (int col = 0; col < 3; col++) {
            int pivot = col;
            for (int row = col+1; row < 3; row++)
                if (Math.abs(a[row][col]) > Math.abs(a[pivot][col])) pivot = row;
            if (Math.abs(a[pivot][col]) < 1e-10)
                throw new IllegalArgumentException("Expected-mean-square equations are singular for this missing-cell design.");
            double[] swap = a[col]; a[col] = a[pivot]; a[pivot] = swap;
            double scale = a[col][col];
            for (int j = col; j < 4; j++) a[col][j] /= scale;
            for (int row = 0; row < 3; row++) if (row != col) {
                double factor = a[row][col];
                for (int j = col; j < 4; j++) a[row][j] -= factor*a[col][j];
            }
        }
        double[] result = {a[0][3],a[1][3],a[2][3]};
        if (Arrays.stream(result).anyMatch(value -> !Double.isFinite(value)))
            throw new IllegalArgumentException("Variance-component equations yielded nonfinite estimates.");
        return result;
    }
    private static String equation(double[] row, String[] effects) {
        StringBuilder text = new StringBuilder();
        for (int col = 2; col >= 0; col--) {
            double value = row[col];
            if (Math.abs(value) < 1e-10) continue;
            if (!text.isEmpty()) text.append(value < 0 ? " - " : " + ");
            else if (value < 0) text.append('-');
            text.append(coefficient(Math.abs(value))).append(" × VC(").append(effects[col]).append(')');
        }
        return text.toString();
    }
    public static Results run(UrCrossedMissingDesign.Prepared design) { return calculate(design).results(); }
    public static Analysis calculate(UrCrossedMissingDesign.Prepared design) {
        int p = design.pLevels(), i = design.iLevels(), n = design.observations().size();
        int[] pN = new int[p], iN = new int[i];
        double[] pSum = new double[p], iSum = new double[i];
        double total = 0, rawSquares = 0;
        for (var o : design.observations()) {
            pN[o.p()]++; iN[o.i()]++;
            pSum[o.p()] += o.score(); iSum[o.i()] += o.score();
            total += o.score(); rawSquares += o.score()*o.score();
        }
        if (!Double.isFinite(total) || !Double.isFinite(rawSquares))
            throw new IllegalArgumentException("Score magnitudes are too large for finite sums of squares.");
        double tP = 0, tI = 0, pMean = 0, iMean = 0;
        for (int j = 0; j < p; j++) {
            tP += pSum[j]*pSum[j]/pN[j]; pMean += (double)pN[j]*pN[j]/n;
        }
        for (int j = 0; j < i; j++) {
            tI += iSum[j]*iSum[j]/iN[j]; iMean += (double)iN[j]*iN[j]/n;
        }
        double tMean = total*total/n;
        double[] t = {tP,tI,rawSquares,tMean};
        int[] df = {p-1,i-1,n-p-i+1};
        double[] ss = {tP-tMean,tI-tMean,rawSquares-tP-tI+tMean};
        double[] ms = {ss[0]/df[0],ss[1]/df[1],ss[2]/df[2]};
        // Each present P x I cell has one observation. Covariance kernels are
        // equality of P, equality of I, and identity for the interaction.
        double[][] et = {{n,p,p},{i,n,i},{n,n,n},{pMean,iMean,1}};
        double[][] ems = new double[3][3];
        for (int col = 0; col < 3; col++) {
            ems[0][col] = (et[0][col]-et[3][col])/df[0];
            ems[1][col] = (et[1][col]-et[3][col])/df[1];
            ems[2][col] = (et[2][col]-et[0][col]-et[1][col]+et[3][col])/df[2];
        }
        double[] vc = solve(ems,ms);
        String[] effects = {design.pColumn(),design.iColumn(),design.pColumn()+"×"+design.iColumn()};
        List<List<String>> anovaRows = new ArrayList<>(), componentRows = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            anovaRows.add(List.of(effects[row],Integer.toString(df[row]),number(t[row]),number(ss[row]),number(ms[row]),number(vc[row])));
            componentRows.add(List.of(effects[row],Integer.toString(df[row]),number(vc[row]),equation(ems[row],effects),"Random effect"));
        }
        Results.Table anova = new Results.Table(List.of("Effect","df","T","SS","MS","VC"),anovaRows);
        Results.Table components = new Results.Table(List.of("Effect","df","Estimate","EMS","Type"),componentRows);
        String notice = "Experimental random crossed G-study with missing P × I cells; no D-study. Benchmarked against urGENOVA manual Appendix F, not the original executable. Missing cells are absent rows, not zero scores.";
        if (Arrays.stream(vc).anyMatch(value -> value < 0)) notice += " Negative variance estimates are retained, not truncated.";
        StringBuilder report = new StringBuilder("GENOVA Studio · urGENOVA missing-cell Java pilot\n\n")
            .append(design.description()).append("\nMethod: analogous ANOVA / Henderson Method 1.\n")
            .append("Levels: P=").append(p).append(", I=").append(i).append("; observations: ").append(n)
            .append("; missing cells: ").append(design.missingCells()).append("\n")
            .append("Grand mean: ").append(number(total/n)).append("\nT(mean): ").append(number(tMean))
            .append("; total SS: ").append(number(rawSquares-tMean)).append("\n\n")
            .append(anova.csv()).append("\nExpected T coefficients, in VC order [P, I, P×I]:\n");
        for (int row = 0; row < 4; row++) {
            report.append(row==3 ? "Mean" : effects[row]).append(": ");
            for (int col = 0; col < 3; col++) report.append(coefficient(et[row][col])).append(col==2?'\n':',');
        }
        report.append("\nExpected mean square equations:\n");
        for (int row = 0; row < 3; row++) report.append(effects[row]).append(": ")
            .append(equation(ems[row],effects)).append('\n');
        report.append("\n").append(notice).append('\n');
        return new Analysis(new Results(anova,components,new Results.Table(List.of(),List.of()),report.toString(),notice),
            t,ss,ms,vc,et,ems);
    }
}
