# Jiang et al. (2024) OSF subdesign checks

The public source is [OSF wud3x](https://osf.io/wud3x/) for Jiang et al.,
“Customizing Bayesian multivariate generalizability theory to mixed-format tests,”
*Behavior Research Methods* 56, 8080–8090, DOI: 10.3758/s13428-024-02472-7.
The original download lives at `../wud3x-osfstorage-archive/` relative to
`genova-gui/`; the validation script reads it without modifying it and checks
SHA-256 hashes for both source CSV files.

Run from the repository root after `python3 genova-gui/build.py`:

```sh
python3 genova-gui/validation/osf_jiang_2024/validate_subdesigns.py
```

The regular `python3 genova-gui/validate.py` also runs these checks when the
OSF archive directory is present, and records the result in
`validation/results.json` and `validation/osf_jiang_subdesigns.log`.

Two complete balanced slices are selected deterministically from the original
records. The unnamed source row-index column is discarded in temporary files;
person IDs, facet IDs, and scores are retained.

| Case | Source selection | Design | Observations |
| --- | --- | --- | ---: |
| FRT task 1 | `resp_FRT.csv`, `item == 1` | person × rater (two raters) | 1,066 |
| MCQ items 1–9 | `resp_MCQ.csv`, `item` in `i1` … `i9` | person × item | 4,797 |

The Python check computes two-way random-effects ANOVA directly from cell
means, sums of squared residuals, and the balanced expected-mean-square
formulas. A separate Java command invokes GENOVA Studio's public tabular
adapter and unchanged jGENOVA engine on the same selected observations.
The check compares three SS, three MS, three variance components, and five
D-study quantities (universe, relative and absolute error, G, Phi) per case.
The tolerance is `0.00006`, accounting for printed precision in jGENOVA.

These are independently computed reference values for selected subdesigns.
They do not reproduce Table 4 of the publication: that table concerns a
joint Bayesian mixed-format model with binary MCQ responses and raters nested
in FRT items. This check treats MCQ scores as numeric 0/1 in a classical
ANOVA and does not test the full multivariate design or Stan estimator.

## Pending publication replication

The full Jiang et al. Bayesian mixed-format analysis and its Table 4 estimates
have **not** been reproduced. Before attempting that replication, reconcile the
archived scripts: `runstan.R` names `mergecodebymanual1025.stan`, which is not in
this download, and `generate_stan_data.R` refers to `_new` objects not defined
in that script. Identify the intended Stan version and preprocessing steps,
then run the joint model in an isolated copy and compare posterior summaries.
See `../EXTERNAL_DATA_VALIDATION_PROGRESS_2026-11-18.md` for the full handoff.
