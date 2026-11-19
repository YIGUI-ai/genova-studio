# CiviConnections EPM validation sources

Downloaded on 2026-11-18 for use as published numerical validation material for GENOVA Studio.

## Article

- Karatoprak Ersen, R., Lee, W.-C., & Yarbrough, D. B. (2026). *Reliability of Difference Scores Obtained From Nested Data Within a Multivariate Generalizability Theory Framework*. Educational and Psychological Measurement. https://doi.org/10.1177/00131644261451746
- Publisher page: https://journals.sagepub.com/doi/10.1177/00131644261451746
- PubMed Central record: https://pmc.ncbi.nlm.nih.gov/articles/PMC13346105/
- License shown in the article: CC BY 4.0.

## Local files and SHA-256

- `main_article.pdf` (27 pages): `e6ccfb347a7b38a9dd9d076d32c7a0a2a115c0b0c53ef5bf35ed4ab1d96aeb14`
- `supplement_1_multivariate_g_theory.pdf` (10 pages): `75549b8720ef9423282ac55edb12a4533fa4ef19f5b2544a7457887516bba048`
- `supplement_2_univariate_g_theory.pdf` (5 pages): `a83e41d786a39e0e52ce1fcb4a7d21b056486f8e09f5a5584ece5cc9b3cdf5b2`

The main article was downloaded from the official SAGE PDF endpoint. The two supplementary PDFs were extracted unchanged from the official Europe PMC supplementary-files archive for PMC13346105.

## Validation scope

These documents provide published intermediate urGENOVA quantities, variance/covariance components, D-study calculations, coefficients, and R code for multivariate and univariate analyses. They can serve as a numerical oracle for those reported cases.

They do **not** include the original row-level CiviConnections observations or the referenced urGENOVA input/output files (for example, `xi_pre.in.out`). Therefore, they do not by themselves validate the complete raw-data-to-result pipeline.

Both supplements report the corrected divisor/result value `485.985733`. The separate CASMA report with the known erroneous `206.924` value is intentionally excluded from this validation-source set.
