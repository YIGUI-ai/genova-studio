# GENOVA Studio progress log — 26 November 2026

## External validation

- Completed the Vispoel BFI-2 external-data validation for the supplied
  one-occasion dataset.
- Tested case: 346 persons, three fixed variables, four items per variable,
  and 4,152 complete observations in a multivariate `P × I` design.
- All 47 checks passed against the development build.
- Repeated all 47 checks against the frozen v1.0.0 packaged application using
  its bundled OpenJDK 25.0.2 runtime on Apple Silicon macOS.
- The development and packaged Java results were numerically identical.
- Evidence remains deliberately bounded to this complete one-occasion `P × I`
  case. It does not claim validation of `P × I × O`, `P × (I:R)`, missing-cell
  behavior, alternative estimators, or unrestricted equivalence to the
  original mGENOVA executable.

## Reproducibility and artifacts

- Frozen candidate ZIP SHA-256:
  `71e931521e6555e45d97f88691f84c2304bca27d6406b74325f0c758e5cce8ce`
- Validated JAR SHA-256:
  `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`
- Machine-readable packaged result:
  `validation/vispoel_bfi2_2023/packaged_v1.0.0_results.json`
- Raw Vispoel source files remain outside the repository. The repository keeps
  only `SOURCE_MANIFEST.md`, validation code, and derived result records.

## Manuscript and documentation

- Updated the SoftwareX manuscript Markdown and regenerated the official
  template DOCX.
- Added the Vispoel external-validation paragraph and reference.
- Preserved the existing evidence boundaries and the separate Jiang OSF
  28-comparison statement.
- Updated the external-validation progress and bundled-runtime release
  validation summaries.
- Rendered and visually checked the final eight-page DOCX; no table rows or
  page content were truncated.

## Version-control and publication state

- Committed the changes locally as:
  `b8acff3162a2088165aa7f7764f8d7cb0ba71742`
- Pushed that commit to GitHub `main`.
- The `v1.0.0` tag, frozen release ZIP, JAR, Zenodo archive, and DOI were not
  changed.
- Zenodo does not need an update for this documentation-and-validation commit.
  A future v1.0.1 archive would be optional and would require a separate human
  release decision.

## Next status

The current v1.0.0 software artifact remains the authoritative released
version. The GitHub `main` branch now contains the additional Vispoel validation
evidence and manuscript updates. No further validation round is required for
this dataset unless the software later implements and tests a new design such
as `P × I × O`.
