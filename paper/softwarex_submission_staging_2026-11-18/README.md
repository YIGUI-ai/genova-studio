# GENOVA Studio SoftwareX submission staging

Prepared 18 November 2026. This is a **local working package**, not a submission-ready release. It keeps the current manuscript and three figures together while tracking each remaining journal and release gate. Do not upload this directory as a journal submission or make the repository public merely because these files exist.

## Current contents

| Item | Local file or source | Status |
| --- | --- | --- |
| Manuscript | `manuscript/GENOVA_Studio_SoftwareX_OSP_template_draft.docx` | Condensed five-section article in official template; author identity, release metadata, and license remain pending. |
| Figures | `figures/` | Three existing figures staged; verify final captions, resolution, and journal format. |
| Reproducibility example | `../reproducibility_case/` | Existing case package and tutorial; review content and redistribution rights before submission. |
| Validation evidence | `../../validation/results.json`, `../../validation/EXTERNAL_DATA_VALIDATION_PROGRESS_2026-11-18.md` | 543 self-test checks and separate 28 OSF comparisons are recorded; repeat on frozen release. |
| Source code and build | `../../src/`, `../../build.py`, `../../validate.py` | Local development version; working tree has uncommitted changes and is not a frozen release. |
| Submission gap matrix | `REQUIREMENTS_AND_GAPS.md` | Working comparison against publisher material and project evidence. |

## Authoritative publisher entry points

- SoftwareX Guide for Authors: https://www.sciencedirect.com/journal/softwarex/publish/guide-for-authors
- Elsevier Research Elements page linking the SoftwareX guide and template: https://www.elsevier.com/researcher/author/tools-and-resources/research-elements-journals
- Publisher-hosted Original Software Publication Word template linked there: https://legacyfileshare.elsevier.com/promis_misc/softwarex-osp-template.docx

The live guide was read through the in-app browser on 18 November 2026, and the official March 2026 Word template was downloaded and retained here. The current guide's 4,000-word limit corrects an earlier 3,000-word assumption. Recheck the live guide immediately before submission because requirements can change.

## Next preparation sequence

1. Review the current official-template draft and the live-guide comparison in `REQUIREMENTS_AND_GAPS.md`.
2. Resolve jGENOVA source rights and separate licenses/notices for original, new, and third-party code. Audit which external data and scans may be redistributed.
3. Confirm authors, affiliations, corresponding author, contributions, funding, conflicts, and any required AI-use disclosure with the author team.
4. Complete the release-dependent metadata in the already-condensed official-template article and keep detailed validation evidence in the repository/supplement.
5. Freeze a source release, rerun all validation with its bundled runtime, and record version, commit, hashes, supported macOS environment, and a stable software citation/identifier.
6. Assemble final manuscript, figures, supplement, software link or code archive, cover letter, and submission-system fields. Recheck the live guide immediately before submission.

The existing private repository remains private through submission unless the authors explicitly change that plan. A public software location or code archive is a release decision to complete before submission, subject to rights review.
