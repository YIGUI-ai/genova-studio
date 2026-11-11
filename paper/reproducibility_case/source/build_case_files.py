from pathlib import Path
import csv
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.section import WD_SECTION

ROOT = Path(__file__).resolve().parents[1]
PROJECT = ROOT.parents[2]

anova = [
    ["P",9,2800.16667,92.66667,10.29630,2.22935,"9","18"],
    ["R",2,2755.70000,48.20000,24.10000,2.17238,"2 QF","12 QF"],
    ["I:R",9,2835.40000,79.70000,8.85556,3.72044,"9","81"],
    ["PR",18,2931.50000,83.13333,4.61852,1.94035,"18","81"],
    ["PI:R",81,3204.00000,192.80000,2.38025,"","",""]
]
vc = [
    ["P",9,0.4731481,0.4731481,0.3855758],
    ["R",2,0.3251543,0.3251543,0.4379875],
    ["I:R",9,0.6475309,0.6475309,0.3794056],
    ["PR",18,0.5595679,0.5595679,0.3766291],
    ["PI:R",81,2.3802469,2.3802469,0.3694860],
]
dstudy = [
    ["001-001",10,3,1,0.47315,1.45309,0.97994,1.30417,0.46954,0.32562,0.26622],
    ["001-002",10,3,2,0.47315,1.05638,0.58323,0.79954,0.32194,0.44790,0.37177],
    ["001-003",10,3,3,0.47315,0.92414,0.45099,0.63133,0.27275,0.51199,0.42839],
    ["001-004",10,3,4,0.47315,0.85802,0.38488,0.54722,0.24815,0.55144,0.46370],
]

def write_csv(name, header, rows):
    with (ROOT/name).open("w", newline="", encoding="utf-8") as f:
        w=csv.writer(f); w.writerow(header); w.writerows(rows)

write_csv("expected_anova.csv", ["effect","df","ss_mean_scores","ss_score_effects","mean_square","f_statistic","f_df_numerator","f_df_denominator"], anova)
write_csv("expected_variance_components.csv", ["effect","df","variance_algorithm","variance_ems","standard_error"], vc)
write_csv("expected_dstudy.csv", ["design","persons","raters","items_per_rater","universe_score_variance","expected_observed_variance","relative_error_variance","absolute_error_variance","mean_variance","g_coefficient","phi"], dstudy)

(ROOT/"analysis_configuration.txt").write_text("""GENOVA Studio reproducibility case\n\nDesign: P x (I:R), random model\nObject of measurement: person (P)\nFacets: rater (R), item nested within rater (I:R)\nG-study sample sizes: P=10, R=3, I:R=4\nInput columns: person, rater, item, score\nColumn mapping: score=score; object=person; rater=rater; item=item\nFacet status: rater=random; item=random\nNesting: item nested within rater\nD-study: R=3; I:R=1,2,3,4\nExpected observations: 10 x 3 x 4 = 120\nReference: official jGENOVA regression output retained with the project.\n""", encoding="utf-8")

(ROOT/"README.txt").write_text("""GENOVA Studio Reproducibility Case\n\nThis package reproduces the balanced P x (I:R) official example using anonymous identifiers.\n\nFiles\n- anonymous_p_ir_example.csv: 120 input observations\n- GENOVA_Studio_Reproducibility_Case.xlsx: input, configuration, expected results, and checks\n- expected_anova.csv: expected ANOVA table\n- expected_variance_components.csv: expected G-study variance components\n- expected_dstudy.csv: expected D-study summary\n- analysis_configuration.txt: GUI settings\n- GENOVA_Studio_Reproducibility_Tutorial.docx: step-by-step tutorial and interpretation\n- official_jgenova_reference_output.txt: full reference engine output\n\nVerification scope\nThe numerical targets reproduce this official balanced example. They do not establish equivalence for every possible design.\n""", encoding="utf-8")

doc=Document()
sec=doc.sections[0]; sec.top_margin=Inches(.65); sec.bottom_margin=Inches(.65); sec.left_margin=Inches(.75); sec.right_margin=Inches(.75)
styles=doc.styles
styles['Normal'].font.name='Aptos'; styles['Normal'].font.size=Pt(10)
for s,z,c in [('Title',24,'17365D'),('Heading 1',16,'17365D'),('Heading 2',12,'2F5597')]:
    styles[s].font.name='Aptos Display'; styles[s].font.size=Pt(z); styles[s].font.color.rgb=RGBColor.from_string(c)
p=doc.add_paragraph(); p.style='Title'; p.alignment=WD_ALIGN_PARAGRAPH.CENTER; p.add_run('GENOVA Studio Reproducibility Case')
p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; p.add_run('Balanced P × (I:R) design').italic=True
doc.add_paragraph('This tutorial provides a complete, anonymous example for reproducing a G-study and four D-study conditions in GENOVA Studio. The expected values come from the retained official jGENOVA regression output.')
doc.add_heading('1  Case overview', level=1)
tbl=doc.add_table(rows=0, cols=2); tbl.alignment=WD_TABLE_ALIGNMENT.CENTER; tbl.style='Light Shading Accent 1'
for a,b in [('Design','P × (I:R), random model'),('Object of measurement','Person'),('Facets','Rater; item nested within rater'),('Input size','120 observations'),('Balance','10 persons × 3 raters × 4 items'),('D-study conditions','3 raters; 1, 2, 3, or 4 items per rater')]:
    cells=tbl.add_row().cells; cells[0].text=a; cells[1].text=b
doc.add_heading('2  Import and configure', level=1)
steps=[
 'Open GENOVA Studio and select Import CSV. Choose anonymous_p_ir_example.csv.',
 'Set Score column to score and Object of measurement to person.',
 'Add rater and item as facets. Set both facets to Random.',
 'Set item as nested within rater. The resulting design is P × (I:R).',
 'Confirm that the preview contains 120 rows, 10 persons, 3 raters, and 4 items within each rater.',
 'Run the G-study. Then set the D-study to 3 raters and evaluate 1, 2, 3, and 4 items per rater.'
]
for i,s in enumerate(steps,1): doc.add_paragraph(f'{i}. {s}')
fig2=PROJECT/'paper/figures/figure2_design_workflow.png'
if fig2.exists():
    doc.add_picture(str(fig2), width=Inches(7.0)); doc.paragraphs[-1].alignment=WD_ALIGN_PARAGRAPH.CENTER
    p=doc.add_paragraph('Figure 1. Import, mapping, facet, and nesting workflow.'); p.alignment=WD_ALIGN_PARAGRAPH.CENTER
doc.add_heading('3  Expected G-study results', level=1)
doc.add_paragraph('The variance component for persons is 0.4731481. The remaining components quantify rater, item-within-rater, and interaction contributions for this example.')
t=doc.add_table(rows=1, cols=5); t.style='Light Shading Accent 1'; t.alignment=WD_TABLE_ALIGNMENT.CENTER
for j,h in enumerate(['Effect','df','Variance','EMS variance','SE']): t.rows[0].cells[j].text=h
for r in vc:
    c=t.add_row().cells
    for j,v in enumerate(r): c[j].text=str(v if j<2 else f'{v:.7f}')
doc.add_heading('4  Expected D-study results', level=1)
doc.add_paragraph('Increasing the number of items per rater reduces both relative and absolute error. With three raters and four items per rater, the expected G coefficient is 0.55144 and Phi is 0.46370.')
t=doc.add_table(rows=1, cols=6); t.style='Light Shading Accent 1'; t.alignment=WD_TABLE_ALIGNMENT.CENTER
for j,h in enumerate(['Items/rater','Relative error','Absolute error','G coefficient','Phi','Universe variance']): t.rows[0].cells[j].text=h
for r in dstudy:
    vals=[r[3],r[6],r[7],r[9],r[10],r[4]]; c=t.add_row().cells
    for j,v in enumerate(vals): c[j].text=str(v if j==0 else f'{v:.5f}')
fig3=PROJECT/'paper/figures/figure3_dstudy_visualizations.png'
if fig3.exists():
    doc.add_picture(str(fig3), width=Inches(7.0)); doc.paragraphs[-1].alignment=WD_ALIGN_PARAGRAPH.CENTER
    p=doc.add_paragraph('Figure 2. D-study heatmap and three-dimensional design-space view.'); p.alignment=WD_ALIGN_PARAGRAPH.CENTER
doc.add_heading('5  Verification procedure', level=1)
for s in ['Compare the ANOVA table with expected_anova.csv.', 'Compare the variance components with expected_variance_components.csv.', 'Compare the four D-study rows with expected_dstudy.csv.', 'Confirm that the workbook Check sheet reports 120 rows, 10 persons, 3 raters, and 4 items per rater.', 'Treat agreement as evidence for this balanced example only.']:
    doc.add_paragraph(s, style='List Bullet')
doc.add_heading('6  Interpretation', level=1)
doc.add_paragraph('The person component represents universe-score variance. PR and PI:R contribute to relative error, while rater and item-within-rater also contribute to absolute error. Adding items reduces the error terms that are divided by the item sampling frequency. In this example, reliability improves monotonically from one to four items per rater, but the values remain below common high-stakes targets. The case therefore illustrates both the mechanics of a D-study and the need to interpret design adequacy in context.')
doc.add_heading('7  Reproducibility statement', level=1)
doc.add_paragraph('All identifiers are synthetic labels. The package contains no personal paths, unrelated filenames, or private data. Numerical targets were transcribed from the project’s official jGENOVA regression artifact and are provided at full reported precision. The package is designed for public review, teaching, and manuscript supplementation.')
doc.save(ROOT/'GENOVA_Studio_Reproducibility_Tutorial.docx')

