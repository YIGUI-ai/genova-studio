from pathlib import Path
import re

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "GENOVA_Studio_SoftwareX_draft.md"
MATRIX = ROOT / "SUBMISSION_EVIDENCE_MATRIX.md"
OUTPUT = ROOT / "GENOVA_Studio_Software_Paper_Working_Draft.docx"
FIGURE = ROOT / "figures" / "genova_studio_architecture.png"

NAVY = "244A68"
PALE = "EEF4F8"
GRID = "D9D9D9"


def set_cell_shading(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=72, start=96, bottom=72, end=96):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for name, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = tc_mar.find(qn(f"w:{name}"))
        if node is None:
            node = OxmlElement(f"w:{name}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_table_borders(table):
    tbl_pr = table._tbl.tblPr
    borders = tbl_pr.find(qn("w:tblBorders"))
    if borders is None:
        borders = OxmlElement("w:tblBorders")
        tbl_pr.append(borders)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = qn(f"w:{edge}")
        el = borders.find(tag)
        if el is None:
            el = OxmlElement(f"w:{edge}")
            borders.append(el)
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), "4")
        el.set(qn("w:color"), GRID)


def repeat_table_header(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def add_hyperlink(paragraph, text, url):
    part = paragraph.part
    rel_id = part.relate_to(url, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink", is_external=True)
    hyperlink = OxmlElement("w:hyperlink")
    hyperlink.set(qn("r:id"), rel_id)
    run = OxmlElement("w:r")
    r_pr = OxmlElement("w:rPr")
    color = OxmlElement("w:color")
    color.set(qn("w:val"), "0563C1")
    underline = OxmlElement("w:u")
    underline.set(qn("w:val"), "single")
    r_pr.extend((color, underline))
    run.append(r_pr)
    text_el = OxmlElement("w:t")
    text_el.text = text
    run.append(text_el)
    hyperlink.append(run)
    paragraph._p.append(hyperlink)


INLINE = re.compile(r"(\*\*[^*]+\*\*|`[^`]+`|\*[^*]+\*|\[[^]]+\]\([^)]+\))")


def add_inline(paragraph, text):
    pos = 0
    for match in INLINE.finditer(text):
        if match.start() > pos:
            paragraph.add_run(text[pos:match.start()])
        token = match.group(0)
        if token.startswith("**"):
            paragraph.add_run(token[2:-2]).bold = True
        elif token.startswith("`"):
            run = paragraph.add_run(token[1:-1])
            run.font.name = "Menlo"
            run.font.size = Pt(9.5)
        elif token.startswith("*"):
            paragraph.add_run(token[1:-1]).italic = True
        else:
            label, url = re.match(r"\[([^]]+)\]\(([^)]+)\)", token).groups()
            add_hyperlink(paragraph, label, url)
        pos = match.end()
    if pos < len(text):
        paragraph.add_run(text[pos:])


def clean_cell(value):
    value = value.strip()
    value = re.sub(r"^\*\*(.*?)\*\*$", r"\1", value)
    return value.replace("`", "")


def set_repeat_and_keep(paragraph):
    paragraph.paragraph_format.keep_with_next = True


def remove_paragraph_borders(paragraph_or_style):
    element = paragraph_or_style._element
    p_pr = element.get_or_add_pPr()
    border = p_pr.find(qn("w:pBdr"))
    if border is not None:
        p_pr.remove(border)


def add_markdown_table(doc, rows, caption=None):
    data = [[clean_cell(cell) for cell in row] for row in rows]
    table = doc.add_table(rows=len(data), cols=len(data[0]))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = True
    set_table_borders(table)
    for r_idx, values in enumerate(data):
        row = table.rows[r_idx]
        if r_idx == 0:
            repeat_table_header(row)
        for c_idx, value in enumerate(values):
            cell = row.cells[c_idx]
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            set_cell_margins(cell)
            if r_idx == 0:
                set_cell_shading(cell, NAVY)
            elif r_idx % 2 == 0:
                set_cell_shading(cell, PALE)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            p.paragraph_format.line_spacing = 1.05
            if len(data) <= 5 and r_idx < len(data) - 1:
                p.paragraph_format.keep_with_next = True
            if c_idx > 0 and re.fullmatch(r"[-+]?\d+(?:\.\d+)?", value):
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            add_inline(p, value)
            for run in p.runs:
                run.font.size = Pt(8.2 if len(data[0]) >= 3 else 9.0)
                if r_idx == 0:
                    run.bold = True
                    run.font.color.rgb = RGBColor(255, 255, 255)
    doc.add_paragraph().paragraph_format.space_after = Pt(0)
    return table


def configure_styles(doc):
    sec = doc.sections[0]
    sec.page_width = Inches(8.5)
    sec.page_height = Inches(11)
    sec.top_margin = Inches(0.78)
    sec.bottom_margin = Inches(0.72)
    sec.left_margin = Inches(0.85)
    sec.right_margin = Inches(0.85)

    normal = doc.styles["Normal"]
    normal.font.name = "Aptos"
    normal.font.size = Pt(10.7)
    normal.font.color.rgb = RGBColor(0, 0, 0)
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.line_spacing = 1.12

    title = doc.styles["Title"]
    title.font.name = "Aptos Display"
    title.font.size = Pt(25)
    title.font.bold = True
    title.font.color.rgb = RGBColor(0, 0, 0)
    title.paragraph_format.space_after = Pt(12)
    remove_paragraph_borders(title)

    for name, size, before, after in (("Heading 1", 16, 16, 7), ("Heading 2", 13, 12, 5), ("Heading 3", 11.5, 9, 4)):
        style = doc.styles[name]
        style.font.name = "Aptos Display"
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = RGBColor(0, 0, 0)
        style.paragraph_format.space_before = Pt(before)
        style.paragraph_format.space_after = Pt(after)
        style.paragraph_format.keep_with_next = True


def add_front_matter(doc):
    p = doc.add_paragraph(style="Title")
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    remove_paragraph_borders(p)
    p.add_run("GENOVA Studio Software Paper Working Draft")
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("A visual and reproducible workflow for generalizability theory analyses")
    r.bold = True
    r.font.size = Pt(14)
    p.paragraph_format.space_after = Pt(18)
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("Prepared 10 November 2026")
    r.italic = True
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run("Authors  To be confirmed")
    p.paragraph_format.space_after = Pt(22)
    p = doc.add_paragraph()
    p.add_run("Purpose  ").bold = True
    p.add_run("This document consolidates the substantive manuscript draft, architecture figure, comparison tables, validation evidence, development boundaries, and submission preparation items for GENOVA Studio. It is designed for review and discussion before journal-specific formatting and public release decisions are finalized.")
    p = doc.add_paragraph()
    p.add_run("Current conclusion  ").bold = True
    p.add_run("GENOVA Studio makes a bounded and evidence-backed software contribution beyond a wrapper: it provides a GUI-first analytic workflow, explicit design validation, typed outputs, reproducible exports, and visual D-study exploration while preserving the original balanced engine and clearly separating the pilot implementations.")
    doc.add_page_break()


def parse_markdown(doc, text, skip_first_title=True):
    lines = text.splitlines()
    i = 0
    pending_caption = None
    while i < len(lines):
        line = lines[i].rstrip()
        stripped = line.strip()
        if not stripped:
            i += 1
            continue
        if skip_first_title and i == 0 and stripped.startswith("# "):
            i += 1
            continue
        if stripped.startswith("!["):
            if FIGURE.exists():
                p = doc.add_paragraph()
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                p.paragraph_format.keep_with_next = True
                p.add_run().add_picture(str(FIGURE), width=Inches(5.8))
            i += 1
            continue
        if stripped.startswith("#"):
            level = len(stripped) - len(stripped.lstrip("#"))
            heading = stripped[level:].strip()
            heading = re.sub(r"[*`]", "", heading)
            if heading == "References":
                doc.add_page_break()
            doc.add_heading(heading, level=min(level, 3))
            i += 1
            continue
        if stripped.startswith("|") and i + 1 < len(lines) and re.match(r"^\s*\|?\s*:?-+", lines[i + 1]):
            rows = []
            header = [c for c in stripped.strip("|").split("|")]
            rows.append(header)
            i += 2
            while i < len(lines) and lines[i].strip().startswith("|"):
                rows.append(lines[i].strip().strip("|").split("|"))
                i += 1
            add_markdown_table(doc, rows, pending_caption)
            pending_caption = None
            continue
        if stripped.startswith(">"):
            p = doc.add_paragraph()
            p.paragraph_format.left_indent = Inches(0.3)
            p.paragraph_format.right_indent = Inches(0.25)
            add_inline(p, stripped.lstrip("> "))
            for run in p.runs:
                run.italic = True
            i += 1
            continue
        if re.match(r"^\d+\.\s", stripped):
            number, item = re.match(r"^(\d+)\.\s+(.*)", stripped).groups()
            p = doc.add_paragraph()
            p.paragraph_format.left_indent = Inches(0.25)
            p.paragraph_format.first_line_indent = Inches(-0.25)
            p.paragraph_format.space_after = Pt(3)
            p.add_run(f"{number}.  ")
            add_inline(p, item)
            i += 1
            continue
        if stripped.startswith("- "):
            p = doc.add_paragraph(style="List Bullet")
            add_inline(p, stripped[2:])
            i += 1
            continue

        paragraph_lines = [stripped]
        i += 1
        while i < len(lines):
            nxt = lines[i].strip()
            if not nxt or nxt.startswith(("#", "|", "!", ">", "- ")) or re.match(r"^\d+\.\s", nxt):
                break
            paragraph_lines.append(nxt)
            i += 1
        text_value = " ".join(paragraph_lines)
        p = doc.add_paragraph()
        if re.match(r"^\*\*Table \d+\.", text_value):
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            p.paragraph_format.keep_with_next = True
        if re.match(r"^\*\*Figure \d+\.", text_value):
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.keep_with_next = True
        add_inline(p, text_value)


def add_appendix(doc):
    doc.add_page_break()
    doc.add_heading("Appendix A Submission Evidence and Preparation Checklist", level=1)
    p = doc.add_paragraph()
    p.add_run("Purpose  ").bold = True
    p.add_run("This appendix connects the manuscript's principal claims to current project evidence and records the remaining gates before submission. Each item should be checked against the immutable release used for the paper.")
    text = MATRIX.read_text(encoding="utf-8")
    lines = text.splitlines()
    # Omit the source title and its implementation-oriented opening sentence; the appendix has its own introduction.
    body = "\n".join(lines[4:])
    parse_markdown(doc, body, skip_first_title=False)


def add_footer(doc):
    for section in doc.sections:
        p = section.footer.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run("GENOVA Studio software paper working draft")
        run.font.size = Pt(8)
        run.font.color.rgb = RGBColor(90, 90, 90)


def main():
    doc = Document()
    configure_styles(doc)
    add_front_matter(doc)
    parse_markdown(doc, SOURCE.read_text(encoding="utf-8"))
    add_appendix(doc)
    add_footer(doc)
    props = doc.core_properties
    props.title = "GENOVA Studio Software Paper Working Draft"
    props.subject = "Working manuscript and submission evidence"
    props.author = "To be confirmed"
    props.keywords = "GENOVA Studio, generalizability theory, software paper"
    doc.save(OUTPUT)
    print(OUTPUT)


if __name__ == "__main__":
    main()
