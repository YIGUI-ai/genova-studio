package org.genova.gui;

import java.util.Arrays;

/**
 * Component-level multivariate D-study calculations.
 *
 * This adapter starts from published G-study variance components for two fixed
 * variables (pre and post) plus their sum.  It is deliberately independent of
 * raw-data ANOVA so published component tables can validate the covariance,
 * divisor, error-matrix, composite, and coefficient calculations.
 */
public final class MComponentDStudy {
    private MComponentDStudy() {}

    /** dComponents columns: pre variance, post variance, covariance, correlation. */
    public record Analysis(double[][] dComponents, double[][] universe,
                           double[][] relativeError, double[][] absoluteError,
                           double[][] metrics) {}

    /** metrics rows are pre, post, post-minus-pre; columns are U, rel, abs, G, Phi, S/N-rel, S/N-abs. */
    public static Analysis calculate(double[][] gPrePostSum, double[] divisors,
                                     boolean[] universeEffects, boolean[] relativeEffects,
                                     boolean[] absoluteEffects) {
        int n = gPrePostSum.length;
        if (n == 0 || divisors.length != n || universeEffects.length != n
                || relativeEffects.length != n || absoluteEffects.length != n)
            throw new IllegalArgumentException("Component arrays must have the same nonzero length.");
        double[][] d = new double[n][4];
        double[][] universe = new double[2][2], relative = new double[2][2], absolute = new double[2][2];
        for (int effect = 0; effect < n; effect++) {
            if (gPrePostSum[effect].length != 3 || !(divisors[effect] > 0) || !Double.isFinite(divisors[effect]))
                throw new IllegalArgumentException("Each effect needs finite pre/post/sum variances and a positive divisor.");
            double pre = gPrePostSum[effect][0], post = gPrePostSum[effect][1], sum = gPrePostSum[effect][2];
            if (!Double.isFinite(pre) || !Double.isFinite(post) || !Double.isFinite(sum))
                throw new IllegalArgumentException("G-study components must be finite.");
            double covariance = (sum - pre - post) / 2.0;
            d[effect][0] = pre / divisors[effect];
            d[effect][1] = post / divisors[effect];
            d[effect][2] = covariance / divisors[effect];
            d[effect][3] = correlation(d[effect][0], d[effect][1], d[effect][2]);
            double[][] component = matrix(d[effect][0], d[effect][1], d[effect][2]);
            if (universeEffects[effect]) add(universe, component);
            if (relativeEffects[effect]) add(relative, component);
            if (absoluteEffects[effect]) add(absolute, component);
        }
        double[][] metrics = new double[3][7];
        for (int variable = 0; variable < 2; variable++)
            fillMetrics(metrics[variable], universe[variable][variable], relative[variable][variable], absolute[variable][variable]);
        fillMetrics(metrics[2], contrast(universe), contrast(relative), contrast(absolute));
        return new Analysis(copy(d), copy(universe), copy(relative), copy(absolute), copy(metrics));
    }

    public static double[] calculateUnivariate(double[] gComponents, double[] divisors,
                                               boolean[] universeEffects, boolean[] relativeEffects,
                                               boolean[] absoluteEffects) {
        int n = gComponents.length;
        if (n == 0 || divisors.length != n || universeEffects.length != n
                || relativeEffects.length != n || absoluteEffects.length != n)
            throw new IllegalArgumentException("Component arrays must have the same nonzero length.");
        double universe = 0, relative = 0, absolute = 0;
        for (int effect = 0; effect < n; effect++) {
            if (!Double.isFinite(gComponents[effect]) || !(divisors[effect] > 0) || !Double.isFinite(divisors[effect]))
                throw new IllegalArgumentException("Components must be finite and divisors positive.");
            double value = gComponents[effect] / divisors[effect];
            if (universeEffects[effect]) universe += value;
            if (relativeEffects[effect]) relative += value;
            if (absoluteEffects[effect]) absolute += value;
        }
        double[] metrics = new double[7];
        fillMetrics(metrics, universe, relative, absolute);
        return metrics;
    }

    private static double[][] matrix(double pre, double post, double covariance) {
        return new double[][]{{pre, covariance}, {covariance, post}};
    }
    private static double correlation(double pre, double post, double covariance) {
        double denominator = Math.sqrt(pre * post);
        return denominator == 0 ? Double.NaN : covariance / denominator;
    }
    private static void add(double[][] target, double[][] value) {
        for (int row = 0; row < 2; row++) for (int column = 0; column < 2; column++)
            target[row][column] += value[row][column];
    }
    private static double contrast(double[][] matrix) {
        return matrix[0][0] + matrix[1][1] - 2 * matrix[0][1];
    }
    private static void fillMetrics(double[] target, double universe, double relative, double absolute) {
        target[0] = universe; target[1] = relative; target[2] = absolute;
        target[3] = universe / (universe + relative);
        target[4] = universe / (universe + absolute);
        target[5] = universe / relative;
        target[6] = universe / absolute;
    }
    private static double[][] copy(double[][] source) {
        return Arrays.stream(source).map(double[]::clone).toArray(double[][]::new);
    }
}
