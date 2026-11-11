# One application, three analysis families

GENOVA Studio presents one data-import and results-export workflow. The analysis
type determines which design rules, statistical engine, and result views apply.
The existing jGENOVA implementation remains the engine for complete, balanced
univariate G- and D-studies. A two-level nesting-chain QC case exposed
inconsistent jGENOVA D-study coefficients, so the GUI now blocks D-studies
for chains of two or more nesting levels while retaining their G-studies.
Neither of the other programs is present in that Java source tree.

| Analysis type | Scope | G-study | D-study | Current state |
| --- | --- | --- | --- | --- |
| GENOVA | Complete, balanced univariate designs | Yes | Yes, except two-or-more-level nesting chains | Working within the stated limits; chain D-study blocked after independent QC |
| urGENOVA | Complete random-effects designs with unequal nesting, cell counts, or empty cells | Six bounded G-study pilots: one-way nested-replicate, two-way crossed-cell, `(p:c) × (i:r)`, crossed missing-cell `p × i`, nested missing-cell `p × (i:h)`, and matrix sampling `(p × i):r` | No | Java pilots with ANOVA and variance components; broader designs planned |
| mGENOVA | Complete multivariate random `P × I` and `P × (I:R)`, with persons shared across fixed variables | Yes, two limited pilots | Yes, proportional or supplied `w` and `a` weights | Java pilots benchmarked against manual Appendices B/C/D; other families planned |

The source material for the new engines is in `../urGENOVA for PC/` and
`../mGENOVA for PC/`. Each folder contains a Version 2.1 manual, a sample
control-card file, sample data, and a Windows executable. Neither folder
contains C source. The manual documents urGENOVA's analogous ANOVA / Henderson
Method 1 procedure. The mGENOVA manual specifies five canonical structures:
`p × i`, `i : p`, `p × i × h`, `p × (i : h)`, and `(p : c) × i`.

## Implementation order

1. Define a shared long-table import model and preserve raw identifiers and
   numeric scores. Give each analysis type its own design validation. An
   unbalanced table must not pass through the current balanced-design adapter.
2. Implement urGENOVA's G-study core with its T terms, expected mean squares,
   and variance-component estimates. Start with one documented unbalanced
   design, then add unequal nesting, unequal observations per cell, and empty
   cells. Label unsupported designs explicitly. Do not provide a D-study action.
3. Implement mGENOVA's canonical families, variance/covariance components,
   weights, matrices, and D-study outputs one family at a time. Its fixed
   multivariate variable is part of the design, not just an extra score column.
4. Add result tables and typed Excel sheets appropriate to each engine. Keep
   method and design metadata with every export.

Before marking a family available in the UI, compare its documented examples
against the manual's printed outputs, check independent calculations, and test
edge cases. If a Windows reference environment is available, compare the
original executable's numeric output too. A matching sample is evidence for
that sample, not a blanket equivalence claim.

The mode picker identifies all three families. GENOVA uses the verified,
unchanged jGENOVA engine. urGENOVA routes six explicitly restricted designs to
independent Java cores. The one-way model handles unequal numbers of replicates
nested within objects; its effective count is
`n0 = (N - sum(n_j^2)/N)/(a - 1)` and its raw object variance estimate is
`(MS_object - MS_nested)/n0`. The two-way random-effects model handles a
crossed `p × h` design with `r:(p × h)` observations. It calculates T terms,
analogous sums of squares, ET/EMS coefficient matrices, and solves the EMS
system for raw variance components. It allows unequal cell counts and at most
one empty crossed cell when residual degrees of freedom remain positive.

The nested-cross model accepts a complete `P × I` table with one observation
per cell, where P belongs to C and I belongs to R. It permits unequal P-per-C
and I-per-R counts. Its T terms, ANOVA, EMS matrix, and eight variance
components reproduce the urGENOVA 2.1 manual's Appendix A dummy-data output.
An absent P × I cell is rejected; this is not the missing-data model.
The separate crossed missing-cell model accepts one score per present P × I
cell; absent rows represent missing observations and zeros remain genuine
scores. It rejects disconnected P-I incidence graphs. It reproduces the
manual's Appendix F T, SS, MS, VC, and key EMS coefficients. The binary manual
data are not a binary-score restriction: a continuous-score fixture is tested.
The separate nested missing-cell model crosses P with I nested in H, requires
each P × H block to retain at least two I observations, and reproduces manual
Appendix G T, SS, MS, VC, and key EMS coefficients.
The matrix-sampling model has P and I each nested within R, with a complete
P × I table per R and unequal group sizes. It reproduces Appendix H T, SS,
MS, VC, and key EMS coefficients.
The existing balanced jGENOVA engine is also cross-checked on the same
synthetic `p × (i:r)` data printed in urGENOVA Appendix C: its SS, MS, and
variance components agree at the displayed precision. The manual's standard
errors and confidence intervals are not part of this cross-check.

The two-way implementation reproduces the urGENOVA 2.1 manual's Appendix D
and E T, SS, MS, VC, and key ET/EMS values, including its nominal-cell
treatment of the empty-cell case. An independently calculated balanced 2 × 2
case, row-order permutations, validation rejections, and typed Excel exports
are also covered. Those tests do **not** demonstrate equivalence for all
designs, and none of the pilots has been compared with the original executable.
Other unbalanced multifacet structures remain unsupported.

The mGENOVA `P × I` pilot accepts exactly four long-table columns and a complete
person-by-item table within every fixed variable. It estimates separate P, I,
and P×I variance components, the cross-variable P covariance matrix, and a
D-study with item counts supplied per variable or drawn from the observed
design. Its ANOVA mean squares, components, cross-variable covariance,
per-variable G/Φ, and default and custom composite G/Φ match printed Appendix B
and D examples at displayed precision.

The `P × (I:R)` pilot accepts five long-table columns. P and R are shared across
fixed variables; I is nested within both R and variable, with a complete
person-by-item table and at least two items in every variable-by-R group. It
estimates P, R, I:R, P×R, and P×I:R variance components, cross-variable P/R/P×R
covariances, and D-study coefficients. The D-study accepts observed counts or
one R-group item-count row per variable, including a different number of R
groups from the G-study. Its ANOVA mean squares, components, covariances, and
default and alternative D-study results match printed Appendices C and D at
displayed precision. An independent additive synthetic case is also tested.
Both pilots default to `w` weights proportional to requested total item counts;
`a` defaults to `w`. Each weight list can be supplied separately and is used as
entered. With `a≠w`, the composite relative and absolute entries are
mean-squared errors, including squared mean bias in the absolute MSE; composite
G/Φ are not reported. The `P × I` pilot matches the manual's unequal-weight
Appendix D scenarios 1.3–1.5, and the `P × (I:R)` formula has an independent
synthetic check. Excel includes typed numeric D-study results and a separate
typed setup sheet for item counts and w/a weights. General weight matrices,
missing cells, and other mGENOVA canonical families remain unsupported. The
original executable has not been
run for comparison.
No balanced-engine result is passed off as an unbalanced or multivariate analysis.

Next validation milestone: if a Windows reference environment is available,
compare the original urGENOVA and mGENOVA executables directly. Then expand
mGENOVA to another canonical family, while retaining strict per-design limits.

## Reference cases already in the supplied materials

The installed `urGENOVA for PC/cc.manual` contains multiple runs, not just a
single demo. Its dummy-data run is paired with manual Appendix A; the balanced
`p × (i : r)` run with Appendix C; the unequal-observations-per-cell
`r : (p × h)` run with Appendix D; the same design with one empty cell with
Appendix E; and the `p × i` missing-observation run with Appendix F. These give
an ordered regression ladder from ordinary balanced output to empty cells.

The installed `mGENOVA for PC/cc.manual` likewise includes the Rajaratnam,
Cronbach, and Gleser `p × i` example using `RCG Data`, matched to manual
Appendix B, and a multivariate nested dummy-data example matched to Appendix
C. Its additional D-study configurations are printed in Appendix D. Both
examples, alternative item counts, equal-weight and unequal-weight Appendix D
scenarios now have regression tests. Other canonical families remain future
work.

These are benchmark inputs and printed expected outputs, not source code. Both
manuals explicitly tie the detailed statistical procedures to Brennan (2001a),
which is not in the supplied folders. Exact feature parity therefore remains
an engineering and validation goal, not something already demonstrated by the
working GENOVA engine or these few examples. Original C source, that book, or
a runnable Windows reference environment would improve coverage, but the
absence of those materials does not prevent building the next limited module.
