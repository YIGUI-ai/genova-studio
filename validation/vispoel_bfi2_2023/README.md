# Vispoel BFI-2 External Validation

This validation uses the BFI-2 Extraversion data supplied by Dr. John Vespa on
2026-11-26. The original files remain outside the repository and are read-only.
The script verifies all three source hashes before analysis and writes the
transformed long-format table only to a temporary directory.

## Tested case

- 346 persons
- Occasion 1 only
- three fixed variables: Assertiveness, Energy Level, and Sociability
- four items nested within each variable
- 4,152 complete observations in a multivariate `P x I` design
- observed D-study item counts of four per variable with proportional weights

Run after building GENOVA Studio:

```bash
python3 build.py
python3 validation/vispoel_bfi2_2023/validate_bfi2_pxi.py \
  "/path/to/RE__[External]_Data_Request_for_Validating_mGENOVA_in_GENOVA_Studio" \
  --output validation/vispoel_bfi2_2023/results.json
```

Use `--jar` and `--java` to run the same checks against an extracted packaged
application. The Java harness is compiled with the development JDK but executed
by the supplied target runtime against the supplied target JAR.

## Frozen v1.0.0 packaged-runtime result

The 47 checks also passed against the JAR and bundled OpenJDK 25.0.2 runtime
extracted from the hash-frozen v1.0.0 candidate ZIP:

- ZIP SHA-256: `71e931521e6555e45d97f88691f84c2304bca27d6406b74325f0c758e5cce8ce`
- JAR SHA-256: `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`
- result: 47/47 PASS
- machine-readable evidence: `packaged_v1.0.0_results.json`

The frozen artifact was not modified by this validation run.

The validator compares the production Java engine with an independent ANOVA
calculation at full precision. It also compares the resulting variance and
covariance components with the rounded mGENOVA values in the instructional
supplement. D-study G/Phi values are recomputed independently from the exact
components. The later lavaan ULS coefficient table is retained as context, not
treated as an exact mGENOVA target; its displayed Assertiveness Phi value is
0.757, whereas the exact ANOVA components give 0.757874 (0.758 to three
decimals).

## Evidence boundary

Passing this check supports the raw-data-to-result path for this one-occasion
BFI-2 `P x I` case. It does not validate the supplement's two-occasion
`P x I x O` analyses, the Studio `P x (I:R)` pilot, missing-cell behavior,
alternative estimators, or unrestricted parity with the original mGENOVA
executable. GENOVA Studio does not currently implement `P x I x O`.
