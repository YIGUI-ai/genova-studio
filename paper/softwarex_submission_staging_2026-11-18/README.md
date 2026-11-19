# GENOVA Studio SoftwareX submission staging

Prepared 18 November 2026. This is a **local working package**, not a submission-ready release. It keeps the current manuscript and three figures together while tracking each remaining journal and release gate. Do not upload this directory as a journal submission or make the repository public merely because these files exist.

## Current contents

| Item | Local file or source | Status |
| --- | --- | --- |
| Manuscript | `manuscript/GENOVA_Studio_SoftwareX_OSP_template_draft.docx` | Condensed five-section article in official template; author identity, public release metadata, and DOI remain pending. The multi-component license description is populated for review. |
| Figures | `figures/` | Three existing figures staged; verify final captions, resolution, and journal format. |
| Reproducibility example | `../reproducibility_case/` | Existing case package and tutorial; review content and redistribution rights before submission. |
| Validation evidence | `../../validation/results.json`, `../../validation/EXTERNAL_DATA_VALIDATION_PROGRESS_2026-11-18.md` | Frozen candidate passed 543 checks in the ZIP-extracted bundled runtime; 28 OSF comparisons remain separate. Repeat if final code or packaging changes. |
| Source code and build | `../../src/`, `../../build.py`, `../../validate.py` | Validated candidate committed and tagged locally as `v1.0.0`; license and release-metadata revisions remain staged for human review. |
| Submission gap matrix | `REQUIREMENTS_AND_GAPS.md` | Working comparison against publisher material and project evidence. |

## Authoritative publisher entry points

- SoftwareX Guide for Authors: https://www.sciencedirect.com/journal/softwarex/publish/guide-for-authors
- Elsevier Research Elements page linking the SoftwareX guide and template: https://www.elsevier.com/researcher/author/tools-and-resources/research-elements-journals
- Publisher-hosted Original Software Publication Word template linked there: https://legacyfileshare.elsevier.com/promis_misc/softwarex-osp-template.docx

The live guide was read through the in-app browser on 18 November 2026, and the official March 2026 Word template was downloaded and retained here. The current guide's 4,000-word limit corrects an earlier 3,000-word assumption. Recheck the live guide immediately before submission because requirements can change.

## Next preparation sequence

1. Review the current official-template draft and the live-guide comparison in `REQUIREMENTS_AND_GAPS.md`.
2. Review `LICENSE.txt` and `THIRD_PARTY_NOTICES.md`: BSD-3-Clause covers original GENOVA Studio work only; unchanged jGENOVA is excluded and documented under the authors' published redistribution statement. Continue excluding external article PDFs, the OSF archive, and the user-supplied scan.
3. Confirm authors, affiliations, corresponding author, contributions, funding, conflicts, and any required AI-use disclosure with the author team.
4. Complete the release-dependent metadata in the already-condensed official-template article and keep detailed validation evidence in the repository/supplement.
5. Freeze a source release, rerun all validation with its bundled runtime, and record version, commit, hashes, supported macOS environment, and a stable software citation/identifier.
6. Assemble final manuscript, figures, supplement, software link or code archive, cover letter, and submission-system fields. Recheck the live guide immediately before submission.

The existing private repository remains private through submission unless the authors explicitly change that plan. A public software location or code archive is a release decision to complete before submission, subject to rights review.
