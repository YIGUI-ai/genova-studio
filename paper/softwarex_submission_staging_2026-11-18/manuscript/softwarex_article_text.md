# GENOVA Studio: A visual macOS application for generalizability theory

Author and corresponding-author details to be confirmed before submission.

## Abstract

GENOVA Studio is a native Apple Silicon macOS application for generalizability-theory (G-theory) analyses from tabular data. It provides visual design configuration, explicit input validation, structured results, export, and D-study exploration. Complete balanced univariate analyses invoke the unchanged jGENOVA Java core through a process-isolated adapter. Separate Java pilots cover six bounded unbalanced G-study designs and two multivariate G/D-study designs with stated limits. Validation combines official and manual examples, independent calculations, and published external data. A raw-score example verifies one balanced analysis end to end; other external checks have narrower, explicitly stated scopes. The application makes supported designs and verification boundaries visible while reducing reliance on hand-written control cards.

Keywords: generalizability theory; research software; variance components; unbalanced designs; multivariate measurement; macOS

## 1 Motivation and significance

Generalizability theory (G theory) separates variation associated with persons or other objects of measurement from variation associated with items, raters, occasions, and their interactions [1,2]. A G-study estimates the relevant components; a D-study uses them to evaluate candidate measurement designs. Software support matters because the statistical model depends on the actual crossing, nesting, balance, and random or fixed status of the facets. An incorrectly coded design can produce a plausible numerical report with a different meaning.

The GENOVA family offers established procedures for balanced, unbalanced, and multivariate analyses [3–6]. Its traditional workflow begins with control cards and numerical records rather than a researcher's spreadsheet. Existing tools cover parts of this space: G_String provides a guided interface that builds urGENOVA control scripts [11]; the gtheory R package [12] and the GeneralizIT Python package [13] estimate G-theory parameters, including unbalanced designs and D-studies, as library calls from a script; and EduG offers a graphical environment oriented toward balanced designs. GENOVA Studio differs in its specific combination: a GUI-first, native Apple Silicon macOS workflow that retains the original jGENOVA engine for the balanced pathway while exposing bounded unbalanced and multivariate pilots with explicit scope statements.

A user imports a long-format CSV, TSV, or Excel table, selects the score and design columns, and configures supported relationships in a graphical interface. The program validates the table against the selected statistical pathway before analysis, then displays ANOVA terms, variance components, D-study results where implemented, and a detailed text report. Results can be exported as typed Excel tables or CSV.

The software is intended for measurement researchers analyzing rater-mediated assessments, observational scores, and item-based instruments, and for instruction in G theory. Its contribution is a reproducible path from an ordinary data table to an explicitly named design and inspectable results. The present evidence evaluates computational outputs and rejection behavior; no controlled study has measured changes in user error, analysis time, or adoption. The statistical engine for balanced univariate designs remains jGENOVA 1.0 by Crick and Brennan. GENOVA Studio adds bounded implementations for selected urGENOVA-like and mGENOVA-like analyses and identifies them as separate pilot pathways, rather than representing them as the original programs.

## 2 Software description

### 2.1 Architecture and design contracts

The application has five layers (Fig. 1): table import, design configuration and validation, analysis dispatch, engine execution, and results/export. The import layer preserves identifiers and distinguishes absent observations from recorded zeroes. The design layer checks required columns, numeric scores, duplicate cells, balance, nesting, and pathway-specific limits. Invalid structures are rejected with an explanation instead of being silently averaged or imputed.

For supported complete balanced univariate designs, an adapter maps table identifiers to deterministic level indices and generates internal jGENOVA input. The unchanged jGENOVA core runs in a separate Java process; its detailed report remains available alongside parsed tables. Process isolation bounds an engine failure and supports comparison with an official reference report. The unbalanced and multivariate modes use separate new Java engines, validators, and output types. A balanced result is never relabeled as an unbalanced or multivariate estimate.

[FIGURE 1]

### 2.2 Supported analyses and outputs

Balanced GENOVA supports complete crossed and selected nested designs, including random and fixed facets, with G- and D-study output where independently checked. A deeper nesting-chain D-study is disabled because an independent comparison identified unreliable coefficients for that configuration; its G-study remains available. Six urGENOVA G-study pilots cover unequal nested replicates, crossed cells with unequal replicates, a nested-cross structure, crossed missing cells, nested missing cells, and matrix sampling. These pilots report ANOVA terms and raw variance components and currently have no unbalanced D-study. The two mGENOVA pilots handle complete multivariate random-effects P × I and P × (I:R) structures with supported item counts and universe and estimation weights. Several pilots — the nested-cross, nested missing-cell, and matrix-sampling unbalanced designs and both multivariate pilots — reject inputs above 5,000 observations. General multivariate designs and arbitrary weighting matrices are outside their present contracts.

The results view presents a data preview, ANOVA output, variance components, applicable D-study indices, and a text report. Excel sheets retain numeric cell types and design metadata. For a balanced D-study with two random facets, a grid calculates each requested count pair with jGENOVA and displays the G and Phi coefficients as a heatmap and rotatable surface. Only grid points are engine calculations; the connecting surface is visual interpolation. The program runs locally and does not upload imported observations.

[FIGURE 2]

## 3 Illustrative examples and verification

The bundled P × (I:R) example has 10 persons, three rater groups, four items per group, and 120 observations. The user imports its long table, assigns person and score, nests item within rater group, and runs a balanced G-study and D-study. With three groups and one item per group, the estimated universe-score variance is 0.47315, G is 0.32562, and Phi is 0.26622. At four items per group, G and Phi rise to 0.55144 and 0.46370. These values illustrate sensitivity to a proposed sampling design; the program does not decide whether a coefficient is adequate for a specific use. Figure 3 shows the calculated grid and its visualization. The official balanced regression case produces the supplied jGENOVA full report after normalizing run date and newline style.

[FIGURE 3]

The current suite passes 543 assertions across import, design validation, all three analytic pathways, outputs, and robustness checks. The complete suite also passed with the Java runtime and application JAR extracted from the hash-frozen macOS candidate ZIP (bundled OpenJDK 25.0.2 on Apple Silicon); the extracted JAR matched the development-build JAR by SHA-256. A separate script passes 28 comparisons on two subdesigns of the publicly archived Jiang et al. data [9]. Assertion counts describe test coverage, not independent validation studies. Table 1 identifies what each external source tests and what it does not test.

[TABLE 1]

The Gleser, Cronbach, and Rajaratnam Table 3 scores [7] were transcribed as 144 observations in a complete 12-person × 6-item × 2-judge design. Row and column totals are checked before import. The balanced engine reproduces seven ANOVA terms, seven variance components, and the reported G coefficients 0.657 and 0.768. The printed item component of 0.471 conflicts with approximately 0.46717 derived from that article's mean squares and Eq. 17; the test uses the derivation and records the discrepancy. This case tests raw-score-to-result behavior for one balanced univariate design.

For a published multivariate application, 51 assertions use the CiviConnections pretest, posttest, sum, and difference components to check derived univariate and multivariate D-study quantities [8]. The public supplements do not contain the row-level observations needed to verify import, ANOVA, or component estimation for that study. The Jiang et al. OSF archive [9,10] supplies two complete classical subdesigns: FRT task 1 (533 persons × two raters, 1,066 observations) and MCQ items 1–9 (533 persons × nine items, 4,797 observations). Independent two-way ANOVA calculations agree with the Studio table adapter and unchanged jGENOVA engine on sums of squares, mean squares, components, and D-study values within 0.00006 absolute tolerance. These checks do not reproduce the article's joint Bayesian mixed-format posterior results or validate the full joint design in the mGENOVA pilots.

The urGENOVA pilots reproduce selected printed ANOVA and component values in manual Appendices A and D–H; an Appendix C balanced example is cross-checked with jGENOVA. The mGENOVA pilots match selected manual Appendices B–D, including alternate D-study counts and unequal-weight cases. Additional synthetic, order-invariance, and invalid-input checks test the implemented contracts. Agreement on these cases does not establish equivalence to every historical program configuration or to the original Windows executables, which were not run as black-box references here.

## 4 Impact and limitations

GENOVA Studio allows a researcher to retain a tabular data source while explicitly stating design choices and reviewing analysis output in one interface. Three kinds of use follow. First, applied G-theory questions that previously required editing a new control card for each scenario can be posed directly from the data table: a rater study, for example, can compare G and Phi across planned numbers of raters and nested items per rater on one heatmap, with each grid point computed by the unchanged jGENOVA engine. Second, it could support teaching, because the pathway from a long-format spreadsheet to a design specification and then to ANOVA terms, variance components, and D-study indices is visible at each step, and invalid designs are rejected with a named reason rather than silently coerced. Third, the bounded new Java engines make selected unbalanced and multivariate structures reachable from the same workflow without introducing a second control-card format. The GUI names the selected engine and refuses unsupported designs instead of implying that all GENOVA-family methods share one implementation or one evidence base.

The current Apple Silicon macOS build is available as a public versioned release [14]. Balanced analyses require complete cells for the chosen design. urGENOVA pilots offer six G-study structures without D-study output; mGENOVA pilots offer two complete-cell structures. Several pilots reject inputs above 5,000 observations, limiting them to bundled-scale examples. External checks have different inferential reach: one balanced raw-data case, two balanced OSF subdesigns, and a published-components D-study oracle. No measured usability advantage, external adoption, or general equivalence to original unbalanced and multivariate executables is claimed. The authors publicly provide jGENOVA source and permit free distribution [3]; it remains unchanged and outside GENOVA Studio's BSD-3-Clause license. Author and support metadata remain to be finalized before journal submission.

## 5 Conclusions

GENOVA Studio combines table import, visual design specification, explicit validation, local analysis, structured exports, and D-study exploration for a defined set of G-theory designs. It retains the original jGENOVA computation for supported balanced work and provides separately implemented urGENOVA and mGENOVA pilots with stated design limits. Official examples, independent calculations, and external data provide complementary but bounded validation. Version 1.0.0 is publicly archived with a version DOI [14]; independent use in applied studies remains the next opportunity to test practical impact.

## Acknowledgements

The original jGENOVA statistical core was developed by Joe E. Crick and Robert L. Brennan.

## Declaration of generative AI use

OpenAI Codex and Anthropic Claude were used to assist with drafting and editing this manuscript. The human author is responsible for reviewing the final text, checking its sources and technical claims, and approving the submitted version.

## References

[1] Cronbach LJ, Gleser GC, Nanda H, Rajaratnam N. The dependability of behavioral measurements: Theory of generalizability for scores and profiles. New York: Wiley; 1972.
[2] Brennan RL. Generalizability theory. New York: Springer; 2001. https://doi.org/10.1007/978-1-4757-3456-0.
[3] Brennan–Crick GENOVA project. GENOVA Suite and jGENOVA downloads. https://brennancrickgenova.org/genova-suite/ [accessed 18 November 2026].
[4] Crick JE, Brennan RL. Manual for jGENOVA: A generalized analysis of variance system (Java version). Version 1.0. 2021.
[5] Brennan RL. Manual for urGENOVA. Version 2.1. Iowa Testing Programs Occasional Papers No. 49; 2001.
[6] Brennan RL. Manual for mGENOVA. Version 2.1. Iowa Testing Programs Occasional Papers No. 50; 2001.
[7] Gleser GC, Cronbach LJ, Rajaratnam N. Generalizability of scores influenced by multiple sources of variance. Psychometrika. 1965;30:395–418.
[8] Karatoprak Ersen R, Lee W-C, Yarbrough DB. Reliability of difference scores obtained from nested data within a multivariate generalizability theory framework. Educ Psychol Meas. 2026. https://doi.org/10.1177/00131644261451746.
[9] Jiang Z, Ouyang J, Shi D, Shi D, Zhang J, Xu L, et al. Customizing Bayesian multivariate generalizability theory to mixed-format tests. Behav Res Methods. 2024;56:8080–90. https://doi.org/10.3758/s13428-024-02472-7.
[10] Jiang Z et al. OSF project wud3x, mixed-format response data. https://osf.io/wud3x/ [accessed 18 November 2026].
[11] Bloch R, Norman G. Generalizability theory for the perplexed: A practical introduction and guide (AMEE Guide No. 68). Med Teach. 2012;34:960–92. https://doi.org/10.3109/0142159X.2012.703791.
[12] Moore CT. gtheory: Apply generalizability theory with R. R package; 2016.
[13] Smith TJ, Kline TJB, Kline AS. GeneralizIT: A Python solution for generalizability theory computations. arXiv:2411.17880; 2024.

[14] GENOVA Studio contributors. GENOVA Studio. Version 1.0.0. Zenodo; 2026. https://doi.org/10.5281/zenodo.24358906.
