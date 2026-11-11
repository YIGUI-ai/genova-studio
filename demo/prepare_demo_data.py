from pathlib import Path
import csv
from openpyxl import Workbook

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
DATA = HERE / "data"
DATA.mkdir(parents=True, exist_ok=True)
source = ROOT / "genova-gui" / "samples" / "official_nested.csv"
output = DATA / "genova_official_nested_demo.xlsx"

with source.open(newline="", encoding="utf-8") as stream:
    rows = list(csv.reader(stream))

book = Workbook()
sheet = book.active
sheet.title = "Nested G Theory"
for row in rows:
    converted = list(row)
    if converted and converted[-1] != "score":
        converted[-1] = int(converted[-1])
    sheet.append(converted)
sheet.freeze_panes = "A2"
sheet.auto_filter.ref = sheet.dimensions
book.properties.title = "GENOVA Studio demonstration fixture"
book.properties.subject = "Synthetic reconstruction of the supplied official jGENOVA nested example"
book.save(output)
print(output)
