# GENOVA Studio: A Visual and Reproducible Workflow for Generalizability Theory Analyses

**Working manuscript — 10 November 2026.** Intended article type: *SoftwareX* original software publication. This is a substantive draft, not a submission-ready claim of public release or original-program equivalence. Author names, affiliations, license, repository, DOI, and release version remain to be supplied after review.

**Authors:** [To be confirmed]

## Abstract

Generalizability theory (G theory) studies the contribution of multiple measurement facets to score variation and supports decisions about alternative measurement designs. Established programs in the GENOVA family implement important analyses, but their input conventions and output formats can make routine work difficult for users who begin with tabular data. GENOVA Studio is a desktop application that imports long-format tables, guides the user through design specification, validates the resulting design, presents analysis results in structured views, and exports machine-readable tables and reports. For complete balanced univariate designs, it invokes the original jGENOVA Java statistical core without changing its estimation code. Separate, explicitly restricted Java pilots implement six unbalanced G-study designs and two multivariate G/D-study designs. Regression tests compare the balanced pathway with the supplied jGENOVA output and compare the pilot pathways with printed manual examples and independent calculations. A D-study grid, heatmap, and rotatable surface display exact engine results at calculated design points. This paper describes the software architecture, supported designs, an illustrative workflow, validation evidence, and boundaries of the current preview.

**Keywords:** generalizability theory; G-study; D-study; measurement design; statistical software; visualization

## Code and software metadata (to complete before submission)

| Field | Current working value |
| --- | --- |
| Software name | GENOVA Studio |
| Version described | [Tag an immutable release] |
| Source repository | [Public, browsable repository after rights review] |
| Archived release / DOI | [Deposit released version] |
| License | [Resolve rights for the original jGENOVA core and separately state licenses for new code] |
| Language and interface | Java; Swing desktop GUI |
| Supported systems | Current packaged preview: macOS on Apple Silicon; a Windows release has not been validated |
| Dependencies | Bundled Java runtime in the macOS app; development build requires a JDK |
| Documentation | `genova-gui/README.md`, application help, and design notes; publication-facing user manual pending |
| Data and replication materials | Example tables, manual-example fixtures, validation script, and expected outputs in the project workspace; public package pending rights and privacy review |

## 1. Motivation and significance

Generalizability theory extends reliability analysis to measurement procedures with multiple sources of variation. A G-study estimates variance components associated with persons, items, raters, and their interactions; a D-study applies those estimates to contemplated measurement designs [1,2]. The GENOVA family provides established implementations for balanced univariate designs, selected unbalanced random-effects designs, and selected multivariate designs [3]. The original distribution uses program-specific input conventions and largely text-oriented output. Researchers who hold observations in a spreadsheet must therefore translate column roles and facet relationships into a control-card representation before examining the results.

GENOVA Studio addresses this workflow. It connects tabular import, explicit design choices, validation, analysis, export, and D-study visualization in one desktop interface. The statistical provenance of each pathway is visible to the user. The balanced GENOVA pathway uses the existing jGENOVA Java engine by Crick and Brennan. The urGENOVA and mGENOVA pathways are limited new Java implementations based on the published program manuals and formulas, and are labeled as pilots. The software does not present these pilots as the original programs or as implementations of every design in their respective families.

The contribution is primarily one of usable, inspectable research software: preserving an established statistical core where available, exposing design assumptions before execution, preventing unsupported configurations, and making outputs easier to audit and reuse. No new variance-component estimator is claimed here.

Figure 1 summarizes the architecture. A shared import and design layer dispatches an analysis only after the user has selected a statistical family. Each family then applies its own validation rules and engine. This separation is consequential: the convenience of one interface does not imply that the three engines share the same assumptions or validation status.

![Figure 1. GENOVA Studio architecture and the verification boundary for its three analysis pathways.](figures/genova_studio_architecture.png)

**Figure 1.** GENOVA Studio architecture. Solid arrows show the data and result flow. Dashed lines show that the validation suite checks all three pathways while retaining separate evidence boundaries. The original jGENOVA core is used only for the supported balanced pathway; urGENOVA and mGENOVA analyses are performed by bounded new Java engines.

Table 1 makes the workflow contribution more concrete. The comparison concerns the supplied jGENOVA control-card workflow, rather than claiming that every existing G-theory package has the same limitations.

**Table 1. Traditional supplied jGENOVA workflow and GENOVA Studio workflow**

| Task | Supplied control-card workflow | GENOVA Studio |
| --- | --- | --- |
| Starting data | Numeric records arranged to match the declared design | Long-format CSV, TSV, `.xlsx`, or pasted table with preserved identifiers |
| Column roles | Encoded indirectly in the input preparation and control cards | Selected explicitly as score, object of measurement, and facets |
| Crossed and nested structure | Written with effect notation such as `P × (I:R)` | Chosen through facet controls and “nested within” relationships |
| Random/fixed status and D-study counts | Entered in effect and D-study control statements | Chosen in the GUI; unsupported combinations are rejected before execution |
| Input checking | Errors may emerge after the program reads the control cards or records | Column, score, balance, duplicate-cell, nesting, and scenario checks occur before engine execution |
| Output | Detailed monospaced text report | Preserved report plus typed ANOVA, variance-component, and D-study tables |
| Design exploration | Rerun separately prepared D-study statements | Define scenarios in the interface; eligible two-facet studies also produce a grid, heatmap, and 3D view |
| Reuse | Values are commonly read or transcribed from text | Excel, CSV, TXT, grid CSV, and PNG exports retain design metadata |

## 2. Software description

### 2.1 Data and design workflow

The application accepts CSV, TSV, and `.xlsx` tables or pasted long-format data. Each observation occupies one row. The user selects the numeric score column and object of measurement, then chooses an analysis family. In balanced GENOVA mode, facets can be included or excluded, declared random or fixed, and specified as crossed or nested. D-study levels can be entered as scenario lists. The preview and validation controls help the user identify incorrect columns, duplicate design cells, missing values, and unsupported structures before invoking the engine.

The interface applies distinct validation rules to each statistical family. An unbalanced table is never silently routed through the balanced design adapter. The six urGENOVA pilots distinguish unequal nested replicates, unequal cell counts, complete nested-cross structures, missing crossed cells, missing cells within a nested structure, and matrix sampling. A missing observation is represented by an absent row, while a numeric zero remains an observed score. The two mGENOVA pilots require a fixed-variable column and complete person-by-item cells within their supported `P × I` or `P × (I:R)` structures. Users can provide D-study item counts and, where appropriate, universe and estimation weights.

### 2.2 Calculation and result presentation

For complete balanced univariate analyses, the GUI constructs the required internal input and runs the unchanged jGENOVA core in a separate Java process. The application parses its output into tables while preserving the detailed text report. Process isolation keeps an analysis failure separate from the desktop interface and gives the GUI a defined input/output boundary. The urGENOVA and mGENOVA pilot modes use separate Java implementations and their own design validators; they do not reuse a balanced result as an unbalanced or multivariate estimate.

The result views include a data preview, ANOVA output, variance components, applicable D-study results, and a detailed report. Exports include typed Excel tables with design metadata, the currently selected table as CSV, and the detailed report as text. These exports support inspection beyond the GUI and reduce the need to transcribe numerical results from a text report.

### 2.3 D-study visualization

For balanced GENOVA analyses with two random facets beyond the measurement object, the user can specify two ranges of D-study counts and calculate a grid of up to 30 scenarios. Each grid point is an engine-computed G or Phi coefficient. A heatmap displays these points with a color scale; a rotatable 3D surface uses the same calculated grid. The surface between observed grid points is a visual interpolation and does not represent additional computed scenarios. Hover information and CSV export retain the exact values at calculated points.

### 2.4 Design decisions, safeguards, and reproducibility

Several implementation decisions address errors that are easy to make when statistical design, data layout, and software syntax are handled at the same time. First, the application maintains a strict boundary between the table seen by the user and the engine-specific representation. For balanced GENOVA analyses, the adapter deterministically maps identifiers to internal level indices and generates the control-card and numeric-record stream. The original engine runs as a child process with a fixed English locale. It receives a temporary input and returns a report that is parsed into typed results. The temporary process boundary limits the consequences of an engine error and makes the original report available for comparison.

Second, analysis-family dispatch is explicit. A table with missing balanced cells is rejected by the balanced validator rather than averaged, imputed, or passed to an unbalanced pilot. Every pilot also has a structural contract. Examples include exactly one score per observed `P × I` cell in the crossed missing-cell model, complete `P × I` tables within each matrix-sampling group, and complete person-by-item cells in the two multivariate pilots. Duplicate cells are rejected because silently averaging them would change the measurement model.

Third, the application retains statistically meaningful awkward results. Negative estimated variance components are reported with an explanation instead of being truncated automatically. A missing observation is an absent row rather than a score of zero. Undefined coefficients remain undefined when their denominator is zero. When multivariate estimation weights differ from universe weights, the composite result is labeled as a mean-squared error and inapplicable composite coefficients are suppressed. These behaviors keep presentation choices from silently changing the fitted analysis.

Fourth, import and export are treated as part of the analytic chain. Spreadsheet formulas are rejected because a cached workbook value can be stale or depend on an unavailable calculation engine; users must supply values. Excel result sheets contain numeric cells rather than formatted numeric strings, filters and frozen headers, and metadata describing the design and engine. The program performs analyses locally and does not upload the imported data. Together, these decisions support a record that can be checked outside the GUI.

## 3. Illustrative example

The bundled balanced example is a `P × (I:R)` random-effects design with 10 persons, three rater groups, four items nested within each group, and 120 observations. In the GUI, the user imports the long table, selects score and person columns, includes rater and item as random facets, nests item within rater, and runs the G-study and D-study. The application displays the ANOVA table, component estimates, D-study coefficients, and a detailed report. For the corresponding official regression case, the validation record reports equality with the supplied jGENOVA full report after normalizing the run date and newline style. The nested D-study example can then be explored as a count grid, heatmap, and 3D surface. These displays show how the coefficients change across the explicitly calculated designs; they do not turn an exploratory display into an optimization claim.

Table 2 reports selected values from this example. The estimated universe-score variance is 0.47315. With three rater groups and one item per group, the G coefficient is 0.32562 and Phi is 0.26622. Holding the number of groups at three while increasing items per group from one to four raises the corresponding coefficients to 0.55144 and 0.46370. The example illustrates the purpose of a D-study display: it exposes the dependence of the decision indices on the proposed sampling design. It does not establish that this design is adequate for a particular operational use; that judgment requires an application-specific standard and validity argument.

**Table 2. Selected D-study scenarios from the bundled `P × (I:R)` example**

| Rater groups | Items per group | Universe variance | Relative error variance | Absolute error variance | G coefficient | Phi |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 3 | 1 | 0.47315 | 0.97994 | 1.30417 | 0.32562 | 0.26622 |
| 3 | 2 | 0.47315 | 0.58323 | 0.79954 | 0.44790 | 0.37177 |
| 3 | 3 | 0.47315 | 0.45099 | 0.63133 | 0.51199 | 0.42839 |
| 3 | 4 | 0.47315 | 0.38488 | 0.54722 | 0.55144 | 0.46370 |

> **Planned Figure 2:** a publication screenshot showing imported columns, score/object selections, random facets, and item nested within rater. **Planned Figure 3:** the corresponding heatmap and 3D result view. These figures require final screenshot selection and privacy review and are intentionally deferred in this working draft.

The same interface also exposes the limits of the pilot families. A user selecting an urGENOVA design sees only its supported G-study output and variance components; no unbalanced D-study coefficient is offered. In mGENOVA mode, the user specifies the fixed variable and applicable nested item/group columns before calculating one of the supported multivariate G/D studies. Alternative item counts and weights are reported alongside the corresponding results. When estimation weights differ from universe weights, the composite outputs are mean-squared errors; composite G and Phi coefficients are not reported.

## 4. Verification and quality control

The current local validation record reports 416 automated checks. These cover the balanced GENOVA pathway, import and design rejection cases, Excel export types, the D-study grid and PNG surface, six urGENOVA pilots, two mGENOVA pilots, independent ANOVA cases, and locale behavior. The record also reports equality of the balanced official full report except for date and newline differences, successful execution with the bundled Java runtime, and unchanged hashes for 95 files in the original downloaded distributions. The validation script and machine-readable result record are retained with the project.

The test harness now records the distribution of the 416 checks rather than reporting only the total (Table 3). A “check” is one executed assertion. Some assertions compare a scalar numerical target, while others test structure, a rejection rule, row-order invariance, or a written export. The counts therefore describe executed verification coverage, not 416 independent validation studies.

**Table 3. Categorized assertions in the current validation run**

| Validation area | Checks |
| --- | ---: |
| Balanced GENOVA reference and D-study visualization | 48 |
| Tabular import | 5 |
| Balanced GENOVA independent and robustness checks | 12 |
| urGENOVA one-way pilot | 11 |
| urGENOVA two-way pilot, manual Appendices D/E | 46 |
| urGENOVA nested-cross pilot, manual Appendix A | 50 |
| urGENOVA crossed missing-cell pilot, manual Appendix F | 24 |
| urGENOVA nested missing-cell pilot, manual Appendix G | 32 |
| urGENOVA matrix-sampling pilot, manual Appendix H | 27 |
| mGENOVA `P × I` pilot, manual Appendices B/D | 79 |
| mGENOVA `P × (I:R)` pilot, manual Appendices C/D | 67 |
| Excel import and typed exports | 15 |
| **Total** | **416** |

For urGENOVA, selected ANOVA terms and variance components reproduce the displayed precision of manual Appendices A, D, E, F, G, and H; a balanced Appendix C example is cross-checked against jGENOVA. For mGENOVA, the two supported models are compared with manual Appendices B, C, and D, including selected alternative D-study counts and unequal-weight examples. Independent synthetic cases, row-order permutations, and invalid-input tests add checks that are not merely copies of the printed examples. These checks support the stated configurations. They do not establish numerical equivalence to the original urGENOVA and mGENOVA Windows executables, which have not yet been run as black-box references in this project.

The validation evidence is therefore reported by pathway and design, rather than summarized as a single claim that the entire GENOVA family has been reproduced. A known discrepancy in a deeper nesting-chain D-study led to a specific guard: balanced G-studies remain available for those chains, while D-studies with two or more nesting levels are blocked pending independent validation.

Table 4 connects the supported design families to their strongest present evidence and remaining boundary. This table is intended to prevent a feature list from being mistaken for a uniform validation claim.

**Table 4. Supported designs, present evidence, and boundaries**

| Pathway | Supported design scope | Present evidence | Current boundary |
| --- | --- | --- | --- |
| GENOVA | Complete balanced univariate designs; crossed facets; single-parent nesting; G-studies for deeper chains | Supplied full-report regression; Appendix C cross-check; independent crossed case; repeated-run, row/column-order, locale, grid, and export checks | D-study blocked for nesting chains of two or more levels; incomplete cells rejected |
| urGENOVA | One-way nested replicates; two-way crossed factors with cell-nested replicates; `(P:C) × (I:R)`; crossed `P × I` with missing cells; `P × (I:H)` with missing cells; matrix sampling `(P × I):R` | Printed manual Appendices A and D–H; independent balanced and continuous-score cases; row-order and rejection checks | Six G-study pilots only; no direct original-executable comparison; other unbalanced designs unsupported |
| mGENOVA | Complete multivariate random `P × I` and `P × (I:R)` with counts and supported `w/a` weights | Printed manual Appendices B–D; alternative counts; unequal weights; independent additive cases; row-order and rejection checks | Two design families; complete cells required; no direct original-executable comparison or general weight matrices |

## 5. Impact, limitations, and reuse

### 5.1 Intended research use

GENOVA Studio makes G-theory analyses more accessible to researchers whose starting point is a table and whose analytic decisions are naturally expressed as column roles, facet relationships, and candidate sample sizes. Its structured outputs and local example fixtures make calculations easier to inspect and teach. The visual D-study grid supports exploration of the sensitivity of G and Phi coefficients to stated facet counts. These are design goals and observed software capabilities; a reduction in user error or analysis time has not yet been measured in a controlled user study, and no external uptake is claimed in this draft.

Potential applications include performance assessments scored by multiple raters, observational coding systems, assessments in which items are nested within forms or occasions, and multivariate score composites. In each case, the application can connect the data structure to the stated facet design and preserve the resulting tables for review. It may also support instruction in G theory because students can change facet counts and see how relative and absolute decision indices respond. These uses remain conditional on the supported design and on the substantive assumptions of the measurement procedure; the interface does not decide which facets belong in a study or which coefficient is acceptable.

The combination of configuration and explicit refusal is part of the intended impact. A useful statistical interface should make valid work easier while preventing unsupported work from looking routine. GENOVA Studio therefore reports why an imported structure cannot be analyzed under a selected family, and it distinguishes production-ready balanced functionality from pilot functionality in the interface and exports.

### 5.2 Limits of the present release

The present macOS application is a local preview. It has not been notarized for public macOS distribution, and a Windows package has not been validated. Balanced analyses require complete designs within the stated jGENOVA scope. The urGENOVA pilots cover six restricted G-study structures and have no D-study implementation. The mGENOVA pilots cover two complete-cell multivariate random-effects structures; other canonical structures and general weighting matrices are outside the present implementation. None of the new pilot engines has undergone direct black-box comparison with the original Windows executables. A matching manual example is evidence for that case, not proof of all possible inputs.

Public reuse also requires a resolved software license and release plan. The original GENOVA project site permits distribution of its programs, but that statement should not be treated as an explicit open-source license for relicensing the jGENOVA source incorporated here [3]. Before submission, the rights and notices for the original core and the newly written code must be documented separately, and a versioned source release, executable package, manual, and replication fixtures must be made available under appropriate terms.

### 5.3 Development and research agenda

The first priority is release integrity rather than additional surface features: resolve the license and attribution of each component, create a public versioned repository, run the validation suite against the immutable release, archive that release with a persistent identifier, and provide one documented command that reproduces the checks. Signing and notarizing the macOS package and validating a Windows package would then broaden access without changing the statistical claims.

The next statistical priority is black-box comparison of each pilot with the original Windows program on a ladder of documented and systematically varied inputs. Agreement should be reported separately for each design and output family. Additional mGENOVA structures can be added only after their inputs, covariance components, D-study rules, and reference outputs are defined. Unsupported designs should continue to be rejected explicitly during this expansion.

A later usability study could compare GENOVA Studio with the supplied control-card workflow. Suitable outcomes include completion rate, configuration errors, time to a valid result, agreement of reported coefficients, and the ability to identify the estimand and proposed D-study design. Such a study would test the accessibility claims that this manuscript currently treats as design goals. External replication by measurement researchers and documented use in an applied study would provide stronger evidence of research impact than download counts alone.

## 6. Conclusion

GENOVA Studio integrates data import, visual design configuration, explicit validation, analysis, structured results, export, and D-study exploration for a bounded set of G-theory applications. Its balanced pathway preserves the original jGENOVA computation, while its unbalanced and multivariate pathways are separately labeled, limited pilots with documented example-based checks. The next validation priorities are direct executable comparisons for those pilots, extension to additional documented designs, and independent evaluation of usability in authentic research workflows.

## References

1. Cronbach, L. J., Gleser, G. C., Nanda, H., & Rajaratnam, N. (1972). *The dependability of behavioral measurements: Theory of generalizability for scores and profiles*. Wiley. [Bibliographic record](https://books.google.com/books/about/The_Dependability_of_Behavioral_Measurem.html?id=EUJqAAAAMAAJ).
2. Brennan, R. L. (2001). *Generalizability theory*. Springer. https://doi.org/10.1007/978-1-4757-3456-0.
3. Brennan–Crick GENOVA project. *GENOVA Suite* and jGENOVA downloads. https://brennancrickgenova.org/genova-suite/ (accessed 10 November 2026).
4. Crick, J. E., & Brennan, R. L. *jGENOVA User Manual*, Version 1.0. [Full bibliographic year and stable URL to verify from the supplied PDF before submission.]
5. Brennan, R. L. *urGENOVA User Manual*, Version 2.1. [Full bibliographic year and stable URL to verify from the supplied PDF before submission.]
6. Brennan, R. L. *mGENOVA User Manual*, Version 2.1. [Full bibliographic year and stable URL to verify from the supplied PDF before submission.]

## Working evidence pointers (remove or move to supplement before submission)

- Functional boundaries: `genova-gui/README.md` and `genova-gui/UNIFIED_DESIGN.md`.
- Automated validation: `genova-gui/validate.py`, `genova-gui/validation/results.json`, and `genova-gui/validation/selftest.log`.
- Balanced reference report: `genova-gui/validation/official_regression.txt` and `jGENOVA1/GENout.txt`.
- Original-file integrity: `analysis/genova_preflight_2026-10-08/original_sha256.json`.
- GUI/engine implementation: `genova-gui/src/org/genova/gui/`.
