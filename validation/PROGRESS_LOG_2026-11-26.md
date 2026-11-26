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

## Submission preparation (SoftwareX OSP template v6, March 2026)

- Re-checked the manuscript against the journal's own article template, which
  is the binding format document; the Elsevier Guide for Authors web page
  returned HTTP 403 to both automated fetch and browser access.
- Restored the five mandatory templated section names verbatim, because the
  template warns that submissions deviating from its structure face desk
  rejection: `3 Illustrative examples and verification` became
  `3 Illustrative examples`, and `4 Impact and limitations` became `4 Impact`.
  Section content was not changed.
- Reduced metadata field C2 to the GitHub repository alone. The template
  accepts only GitHub repositories in the metadata table, and the field had
  also carried the Zenodo archive link; the version DOI now appears in C1 and
  in reference [15].
- Filled C8 with the corresponding-author support email and removed the
  provisional wording from C6.
- Added the declarations the journal expects at submission: CRediT authorship
  contribution statement, declaration of competing interest, declaration of
  generative AI use under Elsevier's current heading, funding, and data
  availability. The data availability statement separates the public GitHub
  and OSF sources from the BFI-2 item-level data, which were supplied for this
  validation and are not redistributed here.
- Replaced the author placeholder with the author block and affiliation, and
  replaced the PLACEHOLDER author in `CITATION.cff` and in reference [15] so
  that the manuscript, the citation file, and the archive record cite the same
  author.
- Confirmed template compliance: body of sections 1-5 is 1,751 words against a
  4,000-word limit, three figures against a limit of six, six keywords against
  a limit of six, abstract 113 words against a target of about 100, and a
  rendered length of nine pages including metadata, figures, Table 1, and
  references.
- Drafted the submission cover letter.

## Version-control and publication state

- Committed the validation and manuscript changes locally as:
  `b8acff3162a2088165aa7f7764f8d7cb0ba71742`, and pushed that commit to
  GitHub `main`.
- Committed the submission declarations and template alignment locally as
  `fac9724`. This commit has not been pushed yet.
- The `v1.0.0` tag, frozen release ZIP, JAR, Zenodo archive, and DOI were not
  changed.
- Zenodo does not need a new deposit for this documentation-and-validation
  work. A future v1.0.1 archive would be optional and would require a separate
  human release decision.
- The existing Zenodo record metadata does still need one correction before
  submission: its Creators field and its notes field still read
  `PLACEHOLDER - authors to be filled before public release`. Editing record
  metadata on Zenodo does not mint a new DOI.

## Next status

The current v1.0.0 software artifact remains the authoritative released
version. The GitHub `main` branch now contains the additional Vispoel validation
evidence and manuscript updates. No further validation round is required for
this dataset unless the software later implements and tests a new design such
as `P × I × O`.

The manuscript is otherwise submission-ready. The outstanding items are the
Zenodo Creators metadata correction described above and a final read-through of
the assembled DOCX before upload.
