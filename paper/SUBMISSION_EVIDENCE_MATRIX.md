# GENOVA Studio paper: evidence and submission gates

Historical working companion to `GENOVA_Studio_SoftwareX_draft.md`. For the current 18 November 2026 submission comparison, use `softwarex_submission_staging_2026-11-18/REQUIREMENTS_AND_GAPS.md`. Check the source artifact and rerun validation against the tagged release before converting the manuscript to the current journal template.

| Manuscript claim | Current evidence | Status / next action |
| --- | --- | --- |
| Balanced computation uses original jGENOVA core | `src/org/genova/gui/Engine.java`; unchanged source in `../workspace/jGENOVA1.0/src/`; `validation/results.json` | Supported locally; preserve authorship and source notices. |
| Balanced official report agreement | `validation/results.json`; `validation/official_regression.txt`; `../jGENOVA1/GENout.txt` | Reported equal except date and newline style; rerun on final tagged version. |
| Six urGENOVA G-study pilots | `UNIFIED_DESIGN.md`; six engine/design pairs; `SelfTest.java` | Manual and independent cases supported; no original executable parity or D-study. |
| Two mGENOVA G/D pilots | `UNIFIED_DESIGN.md`; `MpXi*` and `MpIr*`; `SelfTest.java` | Manual Appendices B/C/D supported within restricted designs; no original executable parity. |
| 543 current self-test checks; 28 separate OSF comparisons | `validation/results.json`, `validation/packaged_zip_selftest.log`, `validation/release_candidate_manifest.json`, `validation/BUNDLED_RUNTIME_RELEASE_VALIDATION_2026-11-18.md`, `validation/osf_jiang_subdesigns.log`; categorized output produced by `SelfTest.java` | The full 543-check suite passed on 18 November 2026 with the Java runtime and JAR extracted from the hash-frozen candidate ZIP. The 28 Jiang comparisons passed separately. The candidate was subsequently committed and tagged locally; release-metadata revisions remain staged, so recreate the still-local tag after review and repeat validation if code or packaging changes. |
| D-study heatmap and 3D surface | `DStudyGrid.java`, `HeatmapPanel.java`, `SurfacePanel.java`, `validate.py` | State that only grid points are computed; intermediate surface is interpolation. |
| No demonstrated usability gain or external adoption | No study or external usage record in the reviewed project materials | Do not claim improvement in speed, error rate, or adoption until measured. |
| Public release and license | Authors' project site supplies jGENOVA source and states that the programs may be distributed freely; the source archive has no standalone standard license | BSD-3-Clause covers only original GENOVA Studio code and materials. `third_party/jGENOVA1.0/` is expressly excluded and documented in `THIRD_PARTY_NOTICES.md`; do not call jGENOVA BSD-licensed or claim unstated modification/relicensing rights. Review the multi-component notice before public upload. |
| Journal-ready citation and metadata | Draft reference list and metadata placeholders | Verify manual title pages, author/year, contributor list, release URL, license, DOI, and journal template. |

## Proposed submission package

1. Versioned public repository or legally separated source distribution, with license and attribution notices for each component.
2. Reproducible source build and tagged macOS release; validate any additional platform before naming it as supported.
3. De-identified example inputs, expected outputs, and one command to run the validation suite.
4. User manual covering column roles, nested/crossed factors, fixed/random facets, D-study assumptions, and pilot boundaries.
5. Figure 1 is drafted in `paper/figures/genova_studio_architecture.svg` with a PNG preview and editable Graphviz source. Figure 2 remains a screenshot of design settings and result tabs. Figure 3 remains the D-study heatmap and surface with a caption distinguishing computed points from interpolation.
6. Current SoftwareX template and disclosure statements, completed from verified release metadata.
