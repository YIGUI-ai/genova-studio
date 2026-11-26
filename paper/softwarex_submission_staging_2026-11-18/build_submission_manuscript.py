from copy import deepcopy
from pathlib import Path
import re
from docx import Document
from docx.shared import Inches, Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parent
TEMPLATE = ROOT / 'softwarex-osp-template-v6-2026-03.docx'
SOURCE = ROOT / 'manuscript/softwarex_article_text.md'
OUTPUT = ROOT / 'manuscript/GENOVA_Studio_SoftwareX_OSP_template_draft.docx'
d = Document(TEMPLATE)
meta_xml = deepcopy(d.tables[0]._tbl)
body = d.element.body
for el in list(body):
    if not el.tag.endswith('sectPr'):
        body.remove(el)

def p(text='', style='Normal'):
    q=d.add_paragraph(style=style)
    q.add_run(text)
    return q

def fig(num, path, caption):
    q=d.add_paragraph()
    q.alignment=WD_ALIGN_PARAGRAPH.CENTER
    width = Inches(4.6) if num == 1 else Inches(6.1)  # Fig.1 is portrait; narrow it to fit a letter page
    q.add_run().add_picture(str(path), width=width)
    q=d.add_paragraph(style='Normal')
    q.paragraph_format.keep_with_next=False
    r=q.add_run(f'Fig. {num}. {caption}')
    r.italic=True

figures={
  1:(ROOT/'figures/genova_studio_architecture.png', 'GENOVA Studio architecture. The table and design layers dispatch to separate balanced, unbalanced, and multivariate engines; only the balanced pathway invokes the unchanged jGENOVA core.'),
  2:(ROOT/'figures/figure2_design_workflow.png', 'Visual design workflow. The interface assigns column roles and facet relationships before validating the selected structure and presenting results.'),
  3:(ROOT/'figures/figure3_dstudy_visualizations.png', 'D-study exploration. Heatmap cells represent exact engine calculations at requested design points; the surface between points is visual interpolation.')
}

lines=SOURCE.read_text().splitlines()
for line in lines:
    if not line.strip(): continue
    if line.startswith('[FIGURE '):
        n=int(re.search(r'\d+',line).group())
        fig(n,*figures[n]); continue
    if line=='[TABLE 1]':
        q=p('Table 1. External validation and its inferential boundary.')
        q.paragraph_format.keep_with_next=True
        t=d.add_table(rows=1, cols=3)
        t.alignment=WD_TABLE_ALIGNMENT.CENTER
        hdr=['Source and input','Verified output','Boundary']
        for c,v in zip(t.rows[0].cells,hdr): c.text=v
        rows=[
          ('Gleser et al. Table 3; 144 raw scores','Balanced P × I × J ANOVA, components, two G coefficients','One balanced univariate case'),
          ('CiviConnections published components','Multivariate/univariate D-study quantities; 51 assertions','No public row-level data or source ANOVA'),
          ('Jiang OSF; two complete subdesigns','Independent ANOVA, components, D-study; 28 comparisons','Not the joint Bayesian model or full mGENOVA design'),
          ('Vispoel et al. BFI-2; 4,152 raw scores','mGENOVA P × I ANOVA, components, D-study; 47 checks','One complete, one-occasion P × I case')
        ]
        for vals in rows:
            cells=t.add_row().cells
            for c,v in zip(cells,vals):c.text=v
        header_props=t.rows[0]._tr.get_or_add_trPr()
        header_repeat=OxmlElement('w:tblHeader')
        header_repeat.set(qn('w:val'),'true')
        header_props.append(header_repeat)
        for row in t.rows:
            row_props=row._tr.get_or_add_trPr()
            no_split=OxmlElement('w:cantSplit')
            row_props.append(no_split)
            for cell in row.cells:
                for x in cell.paragraphs:
                    for r in x.runs:r.font.size=Pt(8)
        continue
    if line.startswith('# '):p(line[2:],'Title')
    elif line.startswith('### '):p(line[4:],'Heading 2')
    elif line.startswith('## '):
        heading=line[3:]
        p(heading,'Heading 1')
        if heading=='Metadata':pass
        if heading=='Abstract':pass
    elif line.startswith('Keywords:'):
        p('Keywords','Heading 1');p(line[len('Keywords: '):])
        p('Metadata','Heading 1')
        body.insert(len(body)-1,deepcopy(meta_xml))
    else:
        q=p(line)
        if line.startswith('[') and re.match(r'^\[\d+\]',line):
            q.paragraph_format.space_after=Pt(0)
            q.paragraph_format.line_spacing=1.0
            for run in q.runs:run.font.size=Pt(8.5)

meta=d.tables[0]
values={
 'C1':'Version 1.0.0; version DOI: https://doi.org/10.5281/zenodo.24358906.',
 'C2':'https://github.com/YIGUI-ai/genova-studio (public source repository); archived version: https://doi.org/10.5281/zenodo.24358906.',
 'C3':'BSD-3-Clause for original GENOVA Studio code and materials. The unchanged jGENOVA core is excluded and documented separately in THIRD_PARTY_NOTICES.md under the authors’ published redistribution statement.',
 'C4':'Git',
 'C5':'Java; Python build and validation scripts; Swing GUI; Graphviz for architecture figure.',
 'C6':'Apple Silicon (arm64) macOS 27.2.1; packaged as a jpackage app-image with a bundled OpenJDK 25.0.2 runtime. Environment reflects the hash-frozen submission candidate; confirm against the final tagged release.',
 'C7':'https://github.com/YIGUI-ai/genova-studio#readme (public README and user documentation).',
 'C8':'Corresponding-author support email to be supplied.'}
for row in meta.rows[1:]:
    key=row.cells[0].text.strip()
    if key in values:row.cells[2].text=values[key]

# The journal template has single-column letter pages and its own style definitions.
# Preserve those rather than importing the prior working draft's page design.
d.save(OUTPUT)
body_text=' '.join(x.text for x in d.paragraphs if x.text and not x.style.name.startswith('Title'))
words=len(re.findall(r"\b[\w’-]+\b",body_text))
print(OUTPUT)
print('paragraph words including headings, author placeholder and references:',words)
print('figures:',len(d.inline_shapes),'tables:',len(d.tables))
