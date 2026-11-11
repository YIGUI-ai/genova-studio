package org.genova.gui;

/** Analysis families shown by the unified application shell. */
public enum AnalysisMode {
    GENOVA("GENOVA · balanced univariate",
        "Complete, balanced univariate G- and D-studies. Powered by the verified jGENOVA engine.", true, true),
    UR_GENOVA("urGENOVA · unbalanced univariate",
        "Six Java G-study pilots: one-way nested replicates; crossed factors with cell-nested replicates; complete (P:C) × (I:R); crossed P × I with missing cells; P × (I:H) with missing cells; and matrix sampling (P × I):R. The latter five match printed urGENOVA manual examples. No D-study; broader designs and original-executable parity are not yet established.", true, false),
    M_GENOVA("mGENOVA · multivariate",
        "Two experimental multivariate random-effects G/D pilots: P × I and P × (I:R). Complete cells are required. Proportional or custom w and a weights; mGENOVA manual Appendices B/C/D benchmarked, not original-executable parity.", true, true);

    private final String label;
    private final String description;
    private final boolean available;
    private final boolean dStudy;

    AnalysisMode(String label, String description, boolean available, boolean dStudy) {
        this.label = label;
        this.description = description;
        this.available = available;
        this.dStudy = dStudy;
    }

    public String description() { return description; }
    public boolean available() { return available; }
    public boolean supportsDStudy() { return dStudy; }
    @Override public String toString() { return label; }
}
