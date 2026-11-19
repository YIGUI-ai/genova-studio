# GENOVA Studio manuscript handoff for Claude

Date: 18 November 2026. This file accompanies `manuscript/GENOVA_Studio_SoftwareX_OSP_template_draft.docx`. Please revise the manuscript as a SoftwareX Original Software Publication while preserving the evidence boundaries below. Return a revised `.docx` and a short change log identifying substantive claim changes and unresolved author or release fields.

## Project and manuscript

- Project root: `/Users/yigui/Documents/GGG/genova-gui/`.
- Private GitHub repository: `https://github.com/YIGUI-ai/genova-studio`. It is already uploaded but currently private. Do not describe it as public or cite a release DOI that does not exist.
- Current journal-template manuscript: `paper/softwarex_submission_staging_2026-11-18/manuscript/GENOVA_Studio_SoftwareX_OSP_template_draft.docx`.
- Editable manuscript text: `paper/softwarex_submission_staging_2026-11-18/manuscript/softwarex_article_text.md`. Builder: `paper/softwarex_submission_staging_2026-11-18/build_submission_manuscript.py`.
- Official template retained as `paper/softwarex_submission_staging_2026-11-18/softwarex-osp-template-v6-2026-03.docx` (Version 6, March 2026). The current SoftwareX guide requires its template, five main sections, no more than 4,000 words under the guide's exclusions, and at most six figures. The draft has three embedded figures and one validation table in addition to the required software metadata table. Guide: https://www.sciencedirect.com/journal/softwarex/publish/guide-for-authors . Recheck it before final submission.
- Earlier full working draft: `paper/GENOVA_Studio_Software_Paper_Working_Draft_v4_external_validation.docx`. It contains more detail and may supply wording, but its layout is not the journal template.

## What the software actually does

GENOVA Studio is a GUI-first Apple Silicon macOS application. Users import long-format CSV, TSV, or Excel data, select score/object/facet columns, configure a supported design visually, inspect structured ANOVA and variance-component results, and export tables and reports. For supported complete balanced univariate designs, an adapter generates internal input and runs the unchanged jGENOVA Java statistical core in a separate process. Six restricted unbalanced G-study pilots and two restricted multivariate G/D-study pilots are separate substantive Java implementations. Do not call them the original urGENOVA or mGENOVA executables. The unbalanced pilots have no D-study. The multivariate pilots cover complete-cell `P × I` and `P × (I:R)` structures, not arbitrary joint designs.

The balanced D-study grid computes requested points with jGENOVA; the heatmap and 3D surface display them. The surface between computed points is interpolation. A deeper nesting-chain D-study is blocked after an independent QC discrepancy; the corresponding balanced G-study remains available. The app is a local preview, not a notarized public release. Windows support is outside current scope.

## Verified evidence and its limits

The primary status records are `validation/EXTERNAL_DATA_VALIDATION_PROGRESS_2026-11-18.md` and `validation/BUNDLED_RUNTIME_RELEASE_VALIDATION_2026-11-18.md`. Latest `validation/results.json` reports **543 conventional self-test checks passed**, including 76 Gleser and 51 CiviConnections checks. The complete 543 suite also passed using the Java runtime and application JAR extracted from the hash-frozen candidate ZIP (`dist/20261118-214156/GENOVA Studio.zip`); artifact hashes, bundled OpenJDK 25.0.2, Apple Silicon macOS 27.2.1 platform details, signature verification, and logs are recorded in `validation/release_candidate_manifest.json`. Jiang OSF subdesign validation reports **28 separate comparisons passed** in the same validation session; it is not part of the 543 bundled-runtime count. Do not combine these into one unexplained count. The original downloaded distributions had 95 hashes checked unchanged. The official balanced jGENOVA full report matches after normalizing date and newline style. The candidate source worktree was dirty and is not a tagged public release, so do not describe it as one.

1. **Gleser et al. 1965, Table 3:** 144 transcribed scores, 12 persons × 6 items × 2 judges, checked from CSV import through the unchanged balanced GENOVA engine. Tests cover source totals, seven ANOVA terms, seven variance components, and published G values 0.657 and 0.768. The article prints an item component 0.471, while its own mean squares and Eq. 17 imply about 0.46717; the test uses the derived value and documents the discrepancy. This is one balanced univariate raw-data-to-result case.
2. **CiviConnections published supplements:** 51 checks derive univariate and multivariate D-study quantities from published pretest/posttest/sum/difference variance and covariance components. Public row-level observations are not available, so this is **components-to-D-study**, not import-to-ANOVA or end-to-end validation.
3. **Jiang et al. 2024 OSF data:** two deterministic complete classical subdesigns were independently computed and compared with the Studio adapter plus unchanged jGENOVA engine: FRT task 1 (`P × R`, 533 × 2 = 1,066 observations) and MCQ items 1–9 (`P × I`, 533 × 9 = 4,797 observations). The 28 comparisons cover SS, MS, variance components, and D-study values at absolute tolerance 0.00006. They do **not** reproduce the paper's joint Bayesian posterior Table 4 or validate the full `P × [I_MCQ ∪ (R:I)_FRT]` design in the current mGENOVA pilots.
4. **Pilot engines:** manual examples and independent synthetic cases support the named urGENOVA and mGENOVA configurations. They have not been compared directly with the historical Windows executables. Matching examples do not prove general equivalence.

Detailed sources: `validation/gleser1965/README.md`, `validation/osf_jiang_2024/README.md`, `validation/results.json`, `validation/selftest.log`, and the CiviConnections source manifest under `validation/external_sources/`. The raw OSF archive and user-provided scan are source evidence; do not silently place them in a public submission archive.

## Authorship, rights, and release facts

The user reports oral permission from both original jGENOVA authors for project use, but cannot currently reach them for written confirmation. The original jGENOVA code is identified in `THIRD_PARTY_NOTES.md`; do not assign it a new license in prose. The user may want to credit the original developers or invite them as manuscript co-authors. Do not put unreachable people on the submitted author list without their review and approval of the final manuscript. The title page, affiliations, corresponding-author email, CRediT roles, final license, public release version, and stable software identifier remain unresolved; retain explicit placeholders rather than inventing them. The manuscript should credit Joe E. Crick and Robert L. Brennan for jGENOVA accurately.

## Requested revision focus

- Improve the scientific argument and SoftwareX fit, especially the Impact section, without claiming measured usability gain, external adoption, or untested statistical coverage.
- Keep the five required SoftwareX sections, required C1–C8 metadata table, three embedded figures, and concise external-validation table. Check citations, figure references, captions, terminology, and the word limit under the live guide's counting rule.
- Review whether the current validation section is proportionate for a software article. Move detailed assertion inventories into a supplement or repository documentation rather than weakening the evidence boundaries.
- Preserve the native Apple Silicon macOS scope and distinguish unchanged jGENOVA, new urGENOVA pilots, and new mGENOVA pilots.
- Verify reference details against original manuals and cited publications. Do not fabricate release metadata or claims of paper/software DOI.
- The draft includes a generative-AI-use declaration; retain and refine it in line with the journal's current guide and the human author's actual review.

## Files to return

1. Revised official-template `.docx` with all journal-template instructions removed and figures embedded.
2. Brief change log with any substantive claim changes, evidence concerns, and fields the user must supply before submission.

Do not upload to Editorial Manager, change the GitHub repository's visibility, publish a release, or send messages on the user's behalf.
