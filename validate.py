"""Independent fixture generation, official regression, and end-to-end tests."""
from pathlib import Path
import csv
import hashlib
import json
import re
import subprocess
from openpyxl import Workbook, load_workbook

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
RUN = HERE / 'validation'
RUN.mkdir(exist_ok=True)
build = json.loads((HERE / 'build/latest.json').read_text())
jar = Path(build['jar'])
java = '/opt/homebrew/opt/openjdk/bin/java'
workbook = Workbook()
sheet = workbook.active
sheet.title = 'Nested'
with (HERE / 'samples/official_nested.csv').open() as stream:
    for row in csv.reader(stream):
        if row[0] != 'person': row[-1] = int(row[-1])
        sheet.append(row)
workbook.create_sheet('Other').append(['x', 'y'])
xlsx = RUN / '独立 Excel 样例.xlsx'
workbook.save(xlsx)
sheet['D2'] = '=1+4'
formula = RUN / 'formula.xlsx'
workbook.save(formula)
exported = RUN / 'independent_result_check.xlsx'
fixed_export = RUN / 'fixed_result_check.xlsx'
g_only_export = RUN / 'g_only_result_check.xlsx'
ur_export = RUN / 'ur_pilot_result_check.xlsx'
ur_two_export = RUN / 'ur_two_way_manual_d_result_check.xlsx'
ur_nested_export = RUN / 'ur_nested_cross_manual_a_result_check.xlsx'
ur_missing_export = RUN / 'ur_crossed_missing_manual_f_result_check.xlsx'
ur_nested_missing_export = RUN / 'ur_nested_missing_manual_g_result_check.xlsx'
ur_matrix_export = RUN / 'ur_matrix_sampling_manual_h_result_check.xlsx'
m_export = RUN / 'm_pxi_manual_b_result_check.xlsx'
m_nested_export = RUN / 'm_pir_manual_c_result_check.xlsx'
m_unequal_export = RUN / 'm_pxi_manual_d_unequal_weights_check.xlsx'
test_args = [str(xlsx), str(formula), str(exported), str(fixed_export), str(g_only_export), str(ur_export), str(ur_two_export), str(ur_nested_export), str(ur_missing_export), str(ur_nested_missing_export), str(ur_matrix_export), str(m_export), str(m_nested_export), str(m_unequal_export)]
test = subprocess.run([java, '-Djava.awt.headless=true', '-cp', str(jar), 'org.genova.gui.SelfTest', *test_args], capture_output=True, text=True, timeout=60)
(RUN / 'selftest.log').write_text(test.stdout + test.stderr)
print(test.stdout + test.stderr)
test.check_returncode()
result_book = load_workbook(exported, data_only=False)
assert result_book.sheetnames == ['D-study', 'Variance components', 'ANOVA', 'Metadata']
assert [result_book['D-study'].cell(1, c).value for c in range(1, 8)] == [
    'Scenario', 'Facet levels', 'Universe σ²', 'Relative error σ²δ', 'Absolute error σ²Δ', 'G coefficient', 'Φ']
assert result_book['D-study'].max_row == 5
assert abs(result_book['D-study']['F5'].value - 0.55144) < 0.000006
assert abs(result_book['D-study']['G5'].value - 0.46370) < 0.000006
assert result_book['D-study']['F5'].data_type == 'n'
assert result_book['Variance components']['C2'].data_type == 'n'
assert result_book['ANOVA']['B2'].data_type == 'n'
assert result_book['Metadata']['B3'].value == 120
assert all(cell.data_type != 'f' for sheet in result_book for row in sheet for cell in row)
assert all(sheet.freeze_panes == 'A2' and sheet.auto_filter.ref for sheet in result_book)
result_book.close()
fixed_book = load_workbook(fixed_export)
assert any('QFM' in str(cell.value) for row in fixed_book['Variance components'] for cell in row)
assert fixed_book['D-study'].max_row == 3
fixed_book.close()
g_only_book = load_workbook(g_only_export)
assert g_only_book['D-study'].max_row == 1
assert g_only_book['D-study']['A1'].value == 'Scenario'
g_only_book.close()
ur_book = load_workbook(ur_export)
assert ur_book.sheetnames == ['Variance components', 'ANOVA', 'Metadata']
assert abs(ur_book['Variance components']['C2'].value - 5.6796875) < 0.000006
assert ur_book['Variance components']['C2'].data_type == 'n'
assert ur_book['Metadata']['B7'].value == 'urGENOVA one-way Java pilot'
ur_book.close()
ur_two_book = load_workbook(ur_two_export)
assert ur_two_book.sheetnames == ['Variance components', 'ANOVA', 'Metadata']
assert abs(ur_two_book['ANOVA']['C2'].value - 400) < 0.000006
assert abs(ur_two_book['ANOVA']['E4'].value - 21) < 0.000006
assert abs(ur_two_book['ANOVA']['F4'].value - 13.30248) < 0.000006
assert all(ur_two_book['ANOVA'].cell(2, col).data_type == 'n' for col in range(2, 7))
assert ur_two_book['Metadata']['B7'].value == 'urGENOVA two-way Java pilot'
ur_two_book.close()
ur_nested_book = load_workbook(ur_nested_export)
assert ur_nested_book.sheetnames == ['Variance components', 'ANOVA', 'Metadata']
assert abs(ur_nested_book['ANOVA']['D2'].value - 17.42222) < 0.000006
assert abs(ur_nested_book['ANOVA']['F9'].value - 2.48032) < 0.000006
assert all(ur_nested_book['ANOVA'].cell(2, col).data_type == 'n' for col in range(2, 7))
assert ur_nested_book['Metadata']['B7'].value == 'urGENOVA nested-cross Java pilot'
ur_nested_book.close()
ur_missing_book = load_workbook(ur_missing_export)
assert ur_missing_book.sheetnames == ['Variance components', 'ANOVA', 'Metadata']
assert abs(ur_missing_book['ANOVA']['D2'].value - 4.59661) < 0.000006
assert abs(ur_missing_book['ANOVA']['F4'].value - 0.18401) < 0.000006
assert all(ur_missing_book['ANOVA'].cell(2, col).data_type == 'n' for col in range(2, 7))
assert ur_missing_book['Metadata']['B7'].value == 'urGENOVA missing-cross Java pilot'
ur_missing_book.close()
ur_nested_missing_book = load_workbook(ur_nested_missing_export)
assert ur_nested_missing_book.sheetnames == ['Variance components', 'ANOVA', 'Metadata']
assert abs(ur_nested_missing_book['ANOVA']['D2'].value - 82.03788) < 0.000006
assert abs(ur_nested_missing_book['ANOVA']['F6'].value - 2.67060) < 0.000006
assert all(ur_nested_missing_book['ANOVA'].cell(2, col).data_type == 'n' for col in range(2, 7))
assert ur_nested_missing_book['Metadata']['B7'].value == 'urGENOVA nested missing-cell Java pilot'
ur_nested_missing_book.close()
ur_matrix_book = load_workbook(ur_matrix_export)
assert ur_matrix_book.sheetnames == ['Variance components', 'ANOVA', 'Metadata']
assert abs(ur_matrix_book['ANOVA']['D2'].value - 17.84429) < 0.000006
assert abs(ur_matrix_book['ANOVA']['F5'].value - 2.81610) < 0.000006
assert all(ur_matrix_book['ANOVA'].cell(2, col).data_type == 'n' for col in range(2, 7))
assert ur_matrix_book['Metadata']['B7'].value == 'urGENOVA matrix-sampling Java pilot'
ur_matrix_book.close()
m_book = load_workbook(m_export)
assert m_book.sheetnames == ['D-study', 'D-study setup', 'Variance components', 'ANOVA', 'Metadata']
assert m_book['D-study'].max_row == 5
assert abs(m_book['D-study']['F5'].value - 0.94149) < 0.000006
assert abs(m_book['D-study']['G5'].value - 0.91968) < 0.000006
assert m_book['D-study']['F5'].data_type == 'n'
assert m_book['D-study setup']['C2'].value == 2
assert m_book['D-study setup']['D2'].value == 0.25
assert m_book['D-study setup']['E2'].value == 0.25
assert all(m_book['D-study setup'].cell(2, col).data_type == 'n' for col in (3,4,5))
assert m_book['Variance components']['D2'].data_type == 'n'
assert m_book['ANOVA']['E2'].data_type == 'n'
assert m_book['Metadata']['B6'].value == 1
assert m_book['Metadata']['B7'].value == 'mGENOVA P × I Java pilot'
m_book.close()
m_nested_book = load_workbook(m_nested_export)
assert m_nested_book.sheetnames == ['D-study', 'D-study setup', 'Variance components', 'ANOVA', 'Metadata']
assert m_nested_book['D-study'].max_row == 4
assert abs(m_nested_book['D-study']['F4'].value - 0.65198) < 0.000006
assert abs(m_nested_book['D-study']['G4'].value - 0.60188) < 0.000006
assert m_nested_book['D-study']['F4'].data_type == 'n'
assert m_nested_book['D-study setup'].max_row == 7
assert m_nested_book['D-study setup']['B2'].value == 'R1'
assert m_nested_book['D-study setup']['C2'].value == 3
assert m_nested_book['D-study setup']['D2'].data_type == 'n'
assert m_nested_book['Variance components']['D2'].data_type == 'n'
assert m_nested_book['ANOVA']['E2'].data_type == 'n'
assert m_nested_book['Metadata']['B3'].value == 220
assert m_nested_book['Metadata']['B6'].value == 1
assert m_nested_book['Metadata']['B7'].value == 'mGENOVA P × (I:R) Java pilot'
m_nested_book.close()
m_unequal_book = load_workbook(m_unequal_export)
assert m_unequal_book['D-study']['B5'].value == 'Composite (MSE)'
assert abs(m_unequal_book['D-study']['D5'].value - 0.13312) < 0.000006
assert abs(m_unequal_book['D-study']['E5'].value - 0.21426) < 0.000006
assert m_unequal_book['D-study']['D5'].data_type == 'n'
assert m_unequal_book['D-study']['E5'].data_type == 'n'
assert m_unequal_book['D-study']['F5'].value == 'N/A'
assert m_unequal_book['D-study']['G5'].value == 'N/A'
assert abs(m_unequal_book['D-study setup']['D2'].value - 0.33333) < 0.00000001
assert m_unequal_book['D-study setup']['E2'].value == 0.2
m_unequal_book.close()
report = RUN / 'official_regression.txt'
baseline_run = subprocess.run([java, '-Duser.language=en', '-Duser.country=US', '-cp', str(jar), 'initGen.GENOVA',
                             str(ROOT / 'jGENOVA1/GENinp.txt'), str(report)], capture_output=True, text=True, timeout=30)
baseline_run.check_returncode()
normalize = lambda s: re.sub(r'(jGENOVA 1\.0 )[A-Za-z]+ \d{1,2} \d{4}(?= +Page)',r'\1<DATE>',s)
assert normalize(report.read_text()) == normalize((ROOT / 'jGENOVA1/GENout.txt').read_text())
hashes = json.loads((ROOT / 'analysis/genova_preflight_2026-10-08/original_sha256.json').read_text())
assert all(hashlib.sha256((ROOT / p).read_bytes()).hexdigest() == h for p,h in hashes.items())
result = {'selftest': test.stdout.strip(), 'official_full_report_equal_except_date_and_newlines': True,
          'downloaded_originals_unchanged': len(hashes), 'jar': str(jar),
          'jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
          'excel_fixture': str(xlsx), 'formula_rejected': True,
          'excel_export_verified': str(exported), 'excel_export_typed_numeric': True,
          'excel_export_fixed_and_g_only_verified': True,
          'ur_pilot_excel_verified': str(ur_export),
          'ur_two_way_excel_verified': str(ur_two_export),
          'ur_nested_cross_excel_verified': str(ur_nested_export),
          'ur_crossed_missing_excel_verified': str(ur_missing_export),
          'ur_nested_missing_excel_verified': str(ur_nested_missing_export),
          'ur_matrix_sampling_excel_verified': str(ur_matrix_export),
          'm_pxi_excel_verified': str(m_export),
          'm_pir_excel_verified': str(m_nested_export),
          'm_unequal_weight_excel_verified': str(m_unequal_export)}
if 'app' in build:
    app = Path(build['app'])
    packaged_jar = app / 'Contents/app/genova-studio.jar'
    bundled_java = app / 'Contents/runtime/Contents/Home/bin/java'
    packaged_test = subprocess.run([str(bundled_java), '-Djava.awt.headless=true', '-cp', str(packaged_jar),
                                  'org.genova.gui.SelfTest', *test_args], capture_output=True, text=True, timeout=60)
    (RUN / 'packaged_selftest.log').write_text(packaged_test.stdout + packaged_test.stderr)
    packaged_test.check_returncode()
    assert packaged_jar.read_bytes() == jar.read_bytes()
    result['bundled_runtime_selftest'] = packaged_test.stdout.strip()
    result['app'] = str(app)
(RUN / 'results.json').write_text(json.dumps(result, indent=2, ensure_ascii=False) + '\n')
print(json.dumps(result, indent=2, ensure_ascii=False))
