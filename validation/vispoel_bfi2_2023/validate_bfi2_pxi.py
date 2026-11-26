"""Validate GENOVA Studio's mGENOVA P x I pilot with Dr. Vespa's BFI-2 data.

The supplied files remain read-only and outside the repository. This script
checks their hashes, transforms Occasion 1 to a temporary long-format table,
runs the production Java engine, and compares its output with both an
independent ANOVA calculation and the rounded values in the instructional
supplement.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import math
import os
from pathlib import Path
import statistics
import subprocess
import tempfile


HERE = Path(__file__).resolve().parent
REPO = HERE.parents[1]

FILES = {
    "BFI-2 Extraversion Dataset.csv":
        "50aba10ebecd0c41db5730311b787f78943a7326f5cc2dbf43ed4c941365ce8d",
    "Extraversion_Codebook.xlsx":
        "bc31047ad11039efa7e253163e7d418d787083f1873809f48da4e73d402dcc17",
    "Instructional Online Supplement to Vispoel et al. (2023) Applying Multivariate G-theory to Psychological Assessments Rechecked 07232026.docx":
        "58aec0b4aba5d8a959341ce8ddf7a96345af13c41680d36181ac14f92e3f5470",
}

GROUPS = {
    "Assertiveness": [6, 21, 36, 51],
    "Energy Level": [11, 26, 41, 56],
    "Sociability": [1, 16, 31, 46],
}

# Rounded mGENOVA values printed on page 4 of the supplied supplement.
PUBLISHED_COMPONENTS = {
    "Assertiveness": {"p": 0.572, "i": 0.037, "pi": 0.694},
    "Energy Level": {"p": 0.411, "i": 0.100, "pi": 0.797},
    "Sociability": {"p": 0.808, "i": 0.218, "pi": 0.824},
}
PUBLISHED_COVARIANCES = {
    ("Assertiveness", "Energy Level"): 0.289,
    ("Assertiveness", "Sociability"): 0.503,
    ("Energy Level", "Sociability"): 0.414,
}
# Rounded coefficients printed on pages 15-16 by the separate lavaan ULS path.
# They are recorded for context, not used as a hard mGENOVA oracle.
REPORTED_LAVAAN_ULS_COEFFICIENTS = {
    "Assertiveness": {"g": 0.767, "phi": 0.757},
    "Energy Level": {"g": 0.674, "phi": 0.647},
    "Sociability": {"g": 0.797, "phi": 0.756},
    "Composite": {"g": 0.879, "phi": 0.863},
}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def independent_components(rows: list[dict[str, str]]) -> tuple[dict, dict]:
    components: dict[str, dict[str, float]] = {}
    person_means: dict[str, list[float]] = {}
    for variable, items in GROUPS.items():
        matrix = [[float(row[f"T1_{item}"]) for item in items] for row in rows]
        persons = len(matrix)
        item_count = len(items)
        total = sum(sum(row) for row in matrix)
        raw_squares = sum(value * value for row in matrix for value in row)
        person_sums = [sum(row) for row in matrix]
        item_sums = [sum(row[index] for row in matrix) for index in range(item_count)]
        correction = total * total / (persons * item_count)
        person_total = sum(value * value / item_count for value in person_sums)
        item_total = sum(value * value / persons for value in item_sums)
        ss_p = person_total - correction
        ss_i = item_total - correction
        ss_pi = raw_squares - person_total - item_total + correction
        ms_p = ss_p / (persons - 1)
        ms_i = ss_i / (item_count - 1)
        ms_pi = ss_pi / ((persons - 1) * (item_count - 1))
        components[variable] = {
            "p": (ms_p - ms_pi) / item_count,
            "i": (ms_i - ms_pi) / persons,
            "pi": ms_pi,
        }
        person_means[variable] = [value / item_count for value in person_sums]

    covariances: dict[tuple[str, str], float] = {}
    names = list(GROUPS)
    for left_index, left in enumerate(names):
        for right in names[left_index + 1:]:
            left_mean = statistics.mean(person_means[left])
            right_mean = statistics.mean(person_means[right])
            covariances[(left, right)] = sum(
                (a - left_mean) * (b - right_mean)
                for a, b in zip(person_means[left], person_means[right])
            ) / (len(rows) - 1)
    return components, covariances


def parse_java_output(output: str) -> dict:
    parsed: dict = {"vc": {}, "d": {}, "cov": {}}
    for line in output.splitlines():
        fields = line.split("\t")
        if fields[0] == "META":
            parsed["meta"] = [int(value) for value in fields[1:]]
        elif fields[0] == "VC":
            parsed["vc"][fields[1]] = {
                key: float(value) for key, value in zip(("p", "i", "pi"), fields[2:])
            }
        elif fields[0] == "D":
            parsed["d"][fields[1]] = {
                key: float(value) for key, value in zip(
                    ("universe", "relative", "absolute", "g", "phi"), fields[2:]
                )
            }
        elif fields[0] == "COV":
            parsed["cov"][(fields[1], fields[2])] = float(fields[3])
        elif fields[0] == "COMPOSITE":
            parsed["composite"] = {
                key: float(value) for key, value in zip(
                    ("universe", "relative", "absolute", "g", "phi"), fields[1:]
                )
            }
    return parsed


def rounded_match(actual: float, expected: float) -> bool:
    return abs(actual - expected) <= 0.0005000001


def independent_decisions(components: dict, covariances: dict) -> tuple[dict, dict]:
    decisions: dict[str, dict[str, float]] = {}
    for variable, component in components.items():
        universe = component["p"]
        relative = component["pi"] / 4
        absolute = (component["i"] + component["pi"]) / 4
        decisions[variable] = {
            "universe": universe,
            "relative": relative,
            "absolute": absolute,
            "g": universe / (universe + relative),
            "phi": universe / (universe + absolute),
        }
    names = list(GROUPS)
    weight = 1 / len(names)
    universe = sum(components[name]["p"] * weight * weight for name in names)
    universe += 2 * sum(covariances[pair] * weight * weight for pair in covariances)
    relative = sum(decisions[name]["relative"] * weight * weight for name in names)
    absolute = sum(decisions[name]["absolute"] * weight * weight for name in names)
    composite = {
        "universe": universe,
        "relative": relative,
        "absolute": absolute,
        "g": universe / (universe + relative),
        "phi": universe / (universe + absolute),
    }
    return decisions, composite


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source_dir", type=Path)
    parser.add_argument("--jar", type=Path)
    parser.add_argument("--java", type=Path,
                        help="Java executable used to run the target JAR")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    for name, expected in FILES.items():
        path = args.source_dir / name
        if not path.is_file():
            raise SystemExit(f"Missing supplied file: {name}")
        actual = sha256(path)
        if actual != expected:
            raise SystemExit(f"SHA-256 mismatch for {name}: {actual}")

    csv_path = args.source_dir / "BFI-2 Extraversion Dataset.csv"
    with csv_path.open(newline="", encoding="utf-8-sig") as stream:
        reader = csv.DictReader(stream)
        rows = list(reader)
        headers = reader.fieldnames or []
    expected_headers = [
        f"{occasion}_{item}"
        for occasion in ("T1", "T2")
        for item in (1, 16, 31, 46, 6, 21, 36, 51, 11, 26, 41, 56)
    ]
    if headers != expected_headers or len(rows) != 346:
        raise SystemExit("The supplied CSV does not have the expected 346 x 24 structure.")
    if any(row[column] == "" for row in rows for column in headers):
        raise SystemExit("Unexpected missing value in the supplied CSV.")
    if any(float(row[column]) not in {1, 2, 3, 4, 5} for row in rows for column in headers):
        raise SystemExit("Unexpected score outside the documented 1-5 scale.")

    independent_vc, independent_cov = independent_components(rows)
    independent_d, independent_composite = independent_decisions(independent_vc, independent_cov)
    jar = args.jar
    if jar is None:
        latest = json.loads((REPO / "build/latest.json").read_text())
        jar = Path(latest["jar"])
    if not jar.is_file():
        raise SystemExit(f"Built GENOVA Studio JAR not found: {jar}")

    jdk = Path(os.environ.get("GENOVA_JDK", "/opt/homebrew/opt/openjdk"))
    target_java = args.java or (jdk / "bin/java")
    if not target_java.is_file():
        raise SystemExit(f"Java runtime not found: {target_java}")
    runtime_version = subprocess.run(
        [str(target_java), "-version"], check=True, capture_output=True, text=True, timeout=15
    )
    with tempfile.TemporaryDirectory(prefix="vispoel-pxi-", dir="/private/tmp") as temp_name:
        temp = Path(temp_name)
        long_path = temp / "bfi2_t1_pxi.csv"
        with long_path.open("w", newline="", encoding="utf-8") as stream:
            writer = csv.writer(stream)
            writer.writerow(["person", "variable", "item", "score"])
            for person_index, row in enumerate(rows, 1):
                for variable, items in GROUPS.items():
                    for item in items:
                        writer.writerow([
                            f"P{person_index:03d}", variable, f"T1_{item}", row[f"T1_{item}"]
                        ])
        classes = temp / "classes"
        classes.mkdir()
        subprocess.run([
            str(jdk / "bin/javac"), "--release", "17", "-encoding", "UTF-8",
            "-cp", str(jar), "-d", str(classes),
            str(HERE / "VispoelPxiExternalValidation.java"),
        ], check=True)
        run = subprocess.run([
            str(target_java), "-Djava.awt.headless=true",
            "-cp", f"{classes}{os.pathsep}{jar}",
            "org.genova.gui.VispoelPxiExternalValidation", str(long_path),
        ], check=True, capture_output=True, text=True, timeout=60)
    java = parse_java_output(run.stdout)

    checks: list[dict[str, object]] = []
    def check(label: str, passed: bool, actual: float | int | str, expected: float | int | str) -> None:
        checks.append({"label": label, "passed": passed, "actual": actual, "expected": expected})

    check("temporary long-format observations", java["meta"][0] == 4152, java["meta"][0], 4152)
    check("persons", java["meta"][1] == 346, java["meta"][1], 346)
    check("fixed variables", java["meta"][2] == 3, java["meta"][2], 3)
    for variable in GROUPS:
        for effect in ("p", "i", "pi"):
            actual = java["vc"][variable][effect]
            independent = independent_vc[variable][effect]
            published = PUBLISHED_COMPONENTS[variable][effect]
            check(f"{variable} {effect} matches independent ANOVA",
                  math.isclose(actual, independent, rel_tol=0, abs_tol=1e-11), actual, independent)
            check(f"{variable} {effect} matches published rounding",
                  rounded_match(actual, published), actual, published)
        for metric in ("universe", "relative", "absolute", "g", "phi"):
            actual = java["d"][variable][metric]
            independent = independent_d[variable][metric]
            check(f"{variable} D-study {metric} matches independent formula",
                  math.isclose(actual, independent, rel_tol=0, abs_tol=1e-11),
                  actual, independent)
    for pair, published in PUBLISHED_COVARIANCES.items():
        actual = java["cov"][pair]
        independent = independent_cov[pair]
        check(f"{pair[0]} x {pair[1]} covariance matches independent calculation",
              math.isclose(actual, independent, rel_tol=0, abs_tol=1e-11), actual, independent)
        check(f"{pair[0]} x {pair[1]} covariance matches published rounding",
              rounded_match(actual, published), actual, published)
    for metric in ("universe", "relative", "absolute", "g", "phi"):
        actual = java["composite"][metric]
        independent = independent_composite[metric]
        check(f"Composite D-study {metric} matches independent formula",
              math.isclose(actual, independent, rel_tol=0, abs_tol=1e-11),
              actual, independent)

    failed = [item for item in checks if not item["passed"]]
    serializable_java = dict(java)
    serializable_java["cov"] = {
        f"{left} x {right}": value for (left, right), value in java["cov"].items()
    }
    result = {
        "status": "PASS" if not failed else "FAIL",
        "evidence_boundary": (
            "One-occasion BFI-2 P x I case only. This does not validate P x I x O, "
            "P x (I:R), missing cells, other estimators, or unrestricted mGENOVA parity."
        ),
        "source_files": {name: digest for name, digest in FILES.items()},
        "source_structure": {
            "persons": 346, "wide_columns": 24, "occasions": 2,
            "subscales": 3, "items_per_subscale": 4, "missing_values": 0,
        },
        "tested_structure": {
            "occasion": "T1", "design": "P x I", "observations": 4152,
            "persons": 346, "fixed_variables": 3, "items_per_variable": 4,
        },
        "jar_sha256": sha256(jar),
        "java_runtime": (runtime_version.stderr or runtime_version.stdout).strip(),
        "reported_lavaan_uls_coefficients_context_only": {
            "values": REPORTED_LAVAAN_ULS_COEFFICIENTS,
            "note": (
                "These rounded ULS estimates are not treated as exact mGENOVA targets. "
                "For example, the supplement reports Assertiveness Phi as 0.757, while "
                "the exact ANOVA components give 0.757874 (0.758 to three decimals)."
            ),
        },
        "checks_passed": len(checks) - len(failed),
        "checks_total": len(checks),
        "java_results": serializable_java,
        "checks": checks,
    }
    rendered = json.dumps(result, indent=2, sort_keys=True) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered)
    print(rendered, end="")
    if failed:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
