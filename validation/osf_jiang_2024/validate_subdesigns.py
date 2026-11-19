"""Independent two-way ANOVA checks against GENOVA Studio on Jiang et al. OSF data."""

from collections import defaultdict
import csv
import hashlib
import json
import math
from pathlib import Path
import subprocess
import sys
import tempfile


GUI = Path(__file__).resolve().parents[2]
ROOT = GUI.parent
SOURCE = ROOT / "wud3x-osfstorage-archive"
SOURCE_HASHES = {
    "resp_FRT.csv": "08be4ccf3ea0afc70b6205868e5cec0aec3cb09b5d8f677c58602595bea23bdf",
    "resp_MCQ.csv": "b8c4a8316d9639f5251e8645beabda26cac9b01c29a8a476b49df3f6e6de2b02",
}


def source_rows(name):
    path = SOURCE / name
    if hashlib.sha256(path.read_bytes()).hexdigest() != SOURCE_HASHES[name]:
        raise AssertionError(f"Source changed or is the wrong OSF export: {name}")
    with path.open(newline="", encoding="utf-8-sig") as stream:
        yield from csv.DictReader(stream)


def cases():
    frt = [(row["person"], row["rater"], float(row["score"]))
           for row in source_rows("resp_FRT.csv") if row["item"] == "1"]
    mcq = [(row["person"], row["item"], float(row["score"]))
           for row in source_rows("resp_MCQ.csv") if row["item"] in {f"i{i}" for i in range(1, 10)}]
    return {"FRT task 1 (P × R)": ("rater", frt),
            "MCQ items 1–9 (P × I)": ("item", mcq)}


def independent_anova(rows):
    cells = {}
    persons, facets = set(), set()
    for person, facet, value in rows:
        key = person, facet
        if key in cells:
            raise AssertionError(f"Duplicate person/facet cell: {key}")
        cells[key] = value
        persons.add(person)
        facets.add(facet)
    p, i = len(persons), len(facets)
    if len(cells) != p * i or p < 2 or i < 2:
        raise AssertionError("Selected subdesign is not a complete two-way table")
    if not all(math.isfinite(v) for v in cells.values()):
        raise AssertionError("Scores must be finite")
    grand = math.fsum(cells.values()) / (p * i)
    pmean = {person: math.fsum(cells[person, facet] for facet in facets) / i for person in persons}
    imean = {facet: math.fsum(cells[person, facet] for person in persons) / p for facet in facets}
    ss_person = i * math.fsum((v - grand) ** 2 for v in pmean.values())
    ss_facet = p * math.fsum((v - grand) ** 2 for v in imean.values())
    ss_interaction = math.fsum((value - pmean[person] - imean[facet] + grand) ** 2
                               for (person, facet), value in cells.items())
    ms_person, ms_facet, ms_interaction = (
        ss_person / (p - 1), ss_facet / (i - 1), ss_interaction / ((p - 1) * (i - 1)))
    vc_person = (ms_person - ms_interaction) / i
    vc_facet = (ms_facet - ms_interaction) / p
    vc_interaction = ms_interaction
    return {
        "persons": p, "facet_levels": i, "observations": len(cells),
        "ss": [ss_person, ss_facet, ss_interaction],
        "ms": [ms_person, ms_facet, ms_interaction],
        "vc": [vc_person, vc_facet, vc_interaction],
        "d": [vc_person, vc_interaction / i, (vc_facet + vc_interaction) / i,
              vc_person / (vc_person + vc_interaction / i),
              vc_person / (vc_person + (vc_facet + vc_interaction) / i)],
    }


def run_studio(fixture, facet):
    build = json.loads((GUI / "build/latest.json").read_text())
    java = Path("/opt/homebrew/opt/openjdk/bin/java")
    completed = subprocess.run([str(java), "-Djava.awt.headless=true", "-cp", build["jar"],
                                "org.genova.gui.OsfSubdesignRunner", str(fixture), facet],
                               capture_output=True, text=True, timeout=130, check=True)
    result = defaultdict(list)
    for line in completed.stdout.splitlines():
        kind, *fields = line.split("\t")
        result[kind].append(fields)
    return result


def near(actual, expected, label):
    # jGENOVA report rounds most values to five decimal places.
    if not math.isclose(float(actual), expected, rel_tol=0, abs_tol=0.00006):
        raise AssertionError(f"{label}: Studio={actual}, independent={expected:.10f}")


def validate(name, facet, rows, directory):
    expected = independent_anova(rows)
    fixture = directory / ("frt_task1.csv" if facet == "rater" else "mcq_items1to9.csv")
    with fixture.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.writer(stream)
        writer.writerow(["person", facet, "score"])
        writer.writerows(rows)
    studio = run_studio(fixture, facet)
    if [len(studio[k]) for k in ("ANOVA", "COMPONENT", "DECISION")] != [3, 3, 1]:
        raise AssertionError(f"Unexpected Studio result shape for {name}: {studio}")
    labels = ["person", facet, f"person × {facet}"]
    for index, label in enumerate(labels):
        row = studio["ANOVA"][index]
        if row[0] != label:
            raise AssertionError(f"Unexpected effect order: {row[0]} versus {label}")
        near(row[2], expected["ss"][index], f"{name} {label} SS")
        near(row[3], expected["ms"][index], f"{name} {label} MS")
        near(studio["COMPONENT"][index][2], expected["vc"][index], f"{name} {label} VC")
    for index, label in enumerate(("universe", "relative error", "absolute error", "G", "Phi")):
        near(studio["DECISION"][0][index + 2], expected["d"][index], f"{name} {label}")
    return {"case": name, **expected, "checks": 14}


def main():
    with tempfile.TemporaryDirectory(prefix="genova-osf-jiang-") as temporary:
        reports = [validate(name, facet, rows, Path(temporary))
                   for name, (facet, rows) in cases().items()]
    print(json.dumps({"status": "PASS", "total_checks": sum(r["checks"] for r in reports),
                      "cases": reports}, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, FileNotFoundError, subprocess.CalledProcessError) as exc:
        print(f"OSF subdesign validation failed: {exc}", file=sys.stderr)
        raise
