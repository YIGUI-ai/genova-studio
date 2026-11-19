# GENOVA Studio 1.0.0 — draft release notes

**Review status:** local SoftwareX submission candidate only. These notes have not been published. Repository visibility, final author metadata, public release, archival deposit, and DOI are still human-gated.

## Candidate artifact

- Frozen candidate: `dist/20261118-214156/GENOVA Studio.zip`
- ZIP SHA-256: `71e931521e6555e45d97f88691f84c2304bca27d6406b74325f0c758e5cce8ce`
- Validated bundled JAR SHA-256: `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`
- Bundled runtime: OpenJDK 25.0.2
- Validation platform: macOS 27.2.1 on Apple Silicon (`arm64`)
- Version requested by the build: 1.0.0

## Validation summary

The complete conventional `SelfTest` suite passed **543/543** using the Java runtime and application JAR extracted from the frozen candidate ZIP. Its 14 reported categories are:

| Category | Checks passed |
| --- | ---: |
| Balanced GENOVA reference and D-study visualization | 48 |
| Tabular import | 5 |
| Balanced GENOVA independent and robustness checks | 12 |
| urGENOVA one-way pilot | 11 |
| urGENOVA two-way pilot (manual D/E) | 46 |
| urGENOVA nested-cross pilot (manual A) | 50 |
| urGENOVA crossed missing-cell pilot (manual F) | 24 |
| urGENOVA nested missing-cell pilot (manual G) | 32 |
| urGENOVA matrix-sampling pilot (manual H) | 27 |
| mGENOVA `P × I` pilot (manual B/D) | 79 |
| mGENOVA `P × (I:R)` pilot (manual C/D) | 67 |
| Published CiviConnections EPM component oracle | 51 |
| Gleser, Cronbach, and Rajaratnam (1965), Table 3 | 76 |
| Excel import and typed exports | 15 |
| **Total** | **543** |

The Jiang et al. OSF subdesign script passed **28 additional comparisons**. Those comparisons are separate from the 543 conventional assertions and are not combined into a single validation count.

Detailed evidence and its boundaries are recorded in:

- `validation/BUNDLED_RUNTIME_RELEASE_VALIDATION_2026-11-18.md`
- `validation/EXTERNAL_DATA_VALIDATION_PROGRESS_2026-11-18.md`
- `validation/release_candidate_manifest.json`
- `validation/packaged_zip_selftest.log`
- `validation/osf_jiang_subdesigns.log`

## Source rebuild check

Local tag `v1.0.0` identifies the committed submission-candidate source. Rebuilding that tree produced a JAR whose 138 entries had the same names, order, and byte content as the validated JAR, but the JAR container SHA-256 was `4d56b8ffaaff8ee63c3d24d29b04bc2317657165b0889af3f85ca28a0916552f` rather than the validated value above. All 138 ZIP-entry timestamps differed. The current JAR creation process is therefore content-reproducible but not byte-for-byte reproducible at the archive-container level. The hash-frozen candidate ZIP remains the authoritative validated artifact.

## Boundaries and claims not made

- No DOI has been minted or claimed.
- The GitHub repository remains private; this is not a public GitHub or Zenodo release.
- Original GENOVA Studio code and materials use the BSD 3-Clause License. That license excludes the unchanged jGENOVA core. The authors' website publicly supplies the source and states that the programs may be distributed freely; `THIRD_PARTY_NOTICES.md` records that redistribution basis. No new license is assigned to jGENOVA.
- The 543 count is a conventional assertion count, not 543 independent validation studies. Jiang's 28 comparisons remain separate.
- The Gleser case is one balanced univariate raw-data-to-result case. It does not validate the unbalanced or multivariate pilots.
- The CiviConnections checks start from published components and validate downstream D-study calculations; public row-level data were not available for import or source-ANOVA validation.
- The two Jiang checks are classical balanced subdesign comparisons. They do not reproduce the paper's joint Bayesian mixed-format model or validate the full joint design.
- The bounded urGENOVA and mGENOVA pilot paths are not claimed to be generally equivalent to the historical Windows executables.
- No measured usability advantage, external adoption, notarization, or Windows support is claimed.

## Credits

The incorporated jGENOVA balanced statistical core was developed by Joe E. Crick and Robert L. Brennan. This credit does not assign their code a new license. GENOVA Studio's other software contributions and final author list remain to be confirmed before public release.
