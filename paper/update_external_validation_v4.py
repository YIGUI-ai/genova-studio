from pathlib import Path
from docx import Document
from docx.oxml import OxmlElement
from docx.text.paragraph import Paragraph
from docx.shared import Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / 'GENOVA_Studio_Software_Paper_Working_Draft_v3_private_repo.docx'
OUTPUT = ROOT / 'GENOVA_Studio_Software_Paper_Working_Draft_v4_external_validation.docx'
d = Document(SOURCE)

def find(prefix):
    return next(p for p in d.paragraphs if p.text.startswith(prefix))

def set_text(p, s):
    p.clear()
    p.add_run(s)

def after(p, s):
    el = OxmlElement('w:p')
    p._p.addnext(el)
    q = Paragraph(el, p._parent)
    q.style = p.style
    q.add_run(s)
    return q

set_text(find('Prepared 10 November 2026'), 'Prepared 10 November 2026; external validation updated 18 November 2026')
set_text(find('Working manuscript — 10 November 2026.'),
    'Working manuscript — revised 18 November 2026. Intended article type: SoftwareX original software publication. This is a substantive draft, not a submission-ready claim of public release or original-program equivalence. Author names, affiliations, license, DOI, and release version remain to be supplied after review. The source repository is currently private during development.')
abstract = find('Generalizability theory (G theory) studies')
set_text(abstract, abstract.text.replace('A D-study grid, heatmap, and rotatable surface', 'External checks additionally cover a published balanced raw-score example, two balanced subdesigns from an OSF dataset, and published multivariate D-study components; these do not constitute complete replication of the source studies. A D-study grid, heatmap, and rotatable surface'))

set_text(find('The current local validation record reports 416'),
    'The current local validation record reports 543 automated self-test checks. These cover all three analytic pathways, import and design rejection cases, typed exports, D-study visualization, six urGENOVA pilots, two mGENOVA pilots, independent ANOVA cases, and locale behavior. The count includes 51 checks of CiviConnections published multivariate components and derived D-study quantities, and 76 checks of the Gleser et al. (1965) raw-score example. A separate validation script passed 28 comparisons on two deterministic subdesigns drawn from the Jiang et al. (2024) OSF data. The balanced official full report remains equal to the supplied jGENOVA report after date and newline normalization; hashes of 95 original distribution files remain unchanged. The validation script, logs, and machine-readable results are retained with the project.')
set_text(find('The test harness now records the distribution of the 416'),
    'Table 3 categorizes the 543 self-test assertions. A check is one executed assertion: it may compare a numerical target or test structure, rejection, invariance, or export behavior. The separate 28 Jiang comparisons are reported below rather than added to the self-test total. These counts describe verification coverage, not independent validation studies.')
t = next(t for t in d.tables if t.rows[0].cells[0].text == 'Validation area')
for label, count in [('Published CiviConnections component and D-study oracle', '51'), ('Gleser et al. 1965 Table 3 raw-score example', '76')]:
    row = t.add_row()
    row.cells[0].text = label
    row.cells[1].text = count
    for cell in row.cells:
        shd = OxmlElement('w:shd')
        shd.set(qn('w:fill'), 'EEF4F8')
        cell._tc.get_or_add_tcPr().append(shd)
        for run in cell.paragraphs[0].runs:
            run.font.size = Pt(8.2)
    row.cells[1].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
total = next(r for r in t.rows if r.cells[0].text == 'Total')
total.cells[1].text = '543'
for run in total.cells[1].paragraphs[0].runs:
    run.font.size = Pt(8.2)
total.cells[1].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
for row in list(t.rows):
    if row.cells[0].text in ('Published CiviConnections component and D-study oracle', 'Gleser et al. 1965 Table 3 raw-score example'):
        total._tr.addprevious(row._tr)

p = find('The validation evidence is therefore reported')
q = after(p, 'Published external evidence adds three distinct checks. First, the 144 scores transcribed from Table 3 of Gleser et al. (1965) pass through tabular import and the unchanged balanced GENOVA engine as a 12-person × 6-item × 2-judge design. The tests check row and column totals, seven ANOVA terms, seven variance components, and the reported G coefficients of 0.657 and 0.768. The paper prints an item component of 0.471, whereas its own mean squares and Eq. 17 imply about 0.46717; the test uses the derived value and records this discrepancy. This is raw-data-to-result evidence for one balanced univariate design, not for the unbalanced or multivariate pilots [7].')
q = after(q, 'Second, 51 assertions use published CiviConnections pretest, posttest, sum, and difference components to verify multivariate and univariate D-study calculations, including error terms and decision indices. The publicly available supplement does not contain the row-level observations needed to verify import, ANOVA, or component estimation for that study [8]. Third, OSF files from Jiang et al. (2024) supply two complete subdesigns: FRT task 1 (533 persons × 2 raters; 1,066 observations) and MCQ items 1–9 (533 persons × 9 items; 4,797 observations). An independent two-way ANOVA calculation and the Studio table adapter with unchanged jGENOVA engine agree on sums of squares, mean squares, components, and D-study values in 28 comparisons at an absolute tolerance of 0.00006. These classical subdesign checks do not reproduce the article’s joint Bayesian mixed-format posterior results and do not validate the present mGENOVA pilots for the full P × [I_MCQ ∪ (R:I)_FRT] design [9].')

rows = next(t for t in d.tables if t.rows[0].cells[0].text == 'Pathway')
rows.rows[1].cells[2].text += '; Gleser et al. raw-score P × I × J case; two Jiang OSF balanced subdesigns'
rows.rows[3].cells[2].text += '; CiviConnections published components-to-D-study oracle'
rows.rows[3].cells[3].text += '; CiviConnections row-level input and full Jiang joint design not verified'

appendix = next(t for t in d.tables if t.rows[0].cells[0].text == 'Manuscript claim')
for row in appendix.rows:
    if row.cells[0].text == '416 checks and bundled runtime':
        row.cells[0].text = '543 checks and bundled runtime'
        row.cells[2].text = 'The full 543-check suite passed on 18 November 2026 with the Java runtime and JAR extracted from the hash-frozen candidate ZIP. Preserve the hashes and logs, and repeat the procedure for the eventual immutable public release.'

set_text(find('The next statistical priority is to deepen validation'),
    'The next statistical priority is to deepen validation within each implemented urGENOVA and mGENOVA design through documented varied inputs, boundary cases, independent calculations, and external replication. In particular, row-level CiviConnections data would permit an end-to-end unbalanced or multivariate comparison if its provenance and analysis configuration can be established. The full Jiang joint mixed-format Bayesian analysis requires separate model and estimand work; its reported posterior summaries should not be treated as direct numeric targets for a classical mGENOVA engine. Additional unbalanced D-study functions and mGENOVA structures should be added only after their inputs, covariance components, D-study rules, and reference outputs are defined. Unsupported designs should continue to be rejected explicitly.')

p = find('6.  Brennan, R. L. mGENOVA')
q = after(p, '7.  Gleser, G. C., Cronbach, L. J., & Rajaratnam, N. (1965). Generalizability of scores influenced by multiple sources of variance. Psychometrika, 30, 395–418.')
q = after(q, '8.  Karatoprak Ersen, R., Lee, W.-C., & Yarbrough, T. (2026). Reliability of difference scores obtained from nested data within a multivariate generalizability theory framework. Educational and Psychological Measurement. https://doi.org/10.1177/00131644261451746.')
q = after(q, '9.  Jiang et al. (2024). Customizing Bayesian multivariate generalizability theory to mixed-format tests. Behavior Research Methods. https://doi.org/10.3758/s13428-024-02472-7. [Verify full author list and publication details before submission.]')

p = find('Automated validation:')
after(p, 'External-data validation record: genova-gui/validation/EXTERNAL_DATA_VALIDATION_PROGRESS_2026-11-18.md; Gleser and Jiang validation READMEs and logs; CiviConnections source manifest and published supplements.')
d.save(OUTPUT)
print(OUTPUT)
