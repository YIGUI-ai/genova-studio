"""Build the GUI and unchanged engine using the installed ARM JDK, no downloads."""
from pathlib import Path
import csv
import hashlib
import json
import os
import subprocess
import sys
import tempfile
import datetime
import shutil

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
JDK = Path(os.environ.get('GENOVA_JDK', '/opt/homebrew/opt/openjdk'))
BUILD = HERE / 'build'
BUILD.mkdir(exist_ok=True)
stage = Path(tempfile.mkdtemp(prefix='build-', dir=BUILD))
classes = stage / 'classes'
classes.mkdir()
sources = sorted((ROOT / 'workspace/jGENOVA1.0/src').rglob('*.java'))
sources += sorted((HERE / 'src').rglob('*.java'))
cmd = [str(JDK / 'bin/javac'), '--release', '17', '-encoding', 'UTF-8', '-d', str(classes), *map(str, sources)]
subprocess.run(cmd, check=True)
sampledir = classes / 'samples'
sampledir.mkdir()
for asset in sorted((HERE / 'samples').glob('*.csv')):
    (sampledir / asset.name).write_bytes(asset.read_bytes())
original = (ROOT / 'jGENOVA1/GENinp.txt').read_text().splitlines()
start = next(i for i, line in enumerate(original) if line.strip() == 'PROCESS') + 1
sample = sampledir / 'official_nested.csv'
with sample.open('w', newline='') as stream:
    writer = csv.writer(stream)
    writer.writerow(['person', 'rater', 'item', 'score'])
    for p, line in enumerate(original[start:start+10], 1):
        vals = line.split()[:12]
        for r in range(3):
            for i in range(4):
                writer.writerow([f'P{p:02}', f'R{r+1}', f'I{i+1}', vals[4*r+i]])
samples = HERE / 'samples'
samples.mkdir(exist_ok=True)
(samples / sample.name).write_bytes(sample.read_bytes())
jar_dir = stage / 'input'
jar_dir.mkdir()
jar = jar_dir / 'genova-studio.jar'
subprocess.run([str(JDK / 'bin/jar'), '--create', '--file', str(jar), '--main-class', 'org.genova.gui.App', '-C', str(classes), '.'], check=True)
metadata = {'jar': str(jar), 'stage': str(stage), 'engine_source_sha256': {
    str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest()
    for p in sorted((ROOT / 'workspace/jGENOVA1.0/src').rglob('*.java'))}}
if '--package' in sys.argv:
    demo_build = '--demo' in sys.argv
    manual_demo_build = '--manual-demo' in sys.argv
    dest = HERE / 'dist' / datetime.datetime.now().strftime('%Y%m%d-%H%M%S')
    dest.mkdir(parents=True)
    package_dest = Path(tempfile.mkdtemp(prefix='genova-package-', dir='/private/tmp'))
    package_args = [str(JDK / 'bin/jpackage'), '--type', 'app-image', '--name', 'GENOVA Studio',
                    '--app-version', '1.0.0', '--input', str(jar_dir), '--main-jar', jar.name,
                    '--main-class', 'org.genova.gui.App', '--dest', str(package_dest),
                    '--mac-package-identifier', 'local.genova.studio',
                    '--description', 'Generalizability theory on Mac',
                    '--add-modules', 'java.base,java.desktop,java.xml',
                    '--jlink-options', '--strip-debug --no-man-pages --no-header-files',
                    '--java-options', '-Dfile.encoding=UTF-8']
    if demo_build:
        package_args += ['--java-options', f'-Dgenova.demo.dir={HERE / "demo" / "data"}']
        package_args += ['--java-options', f'-Dgenova.demo.file={HERE / "demo" / "data" / "genova_official_nested_demo.xlsx"}']
        package_args += ['--java-options', f'-Dgenova.demo.output={HERE / "demo" / "output"}']
    if manual_demo_build:
        package_args += ['--java-options', f'-Dgenova.demo.dir={HERE / "demo" / "import_single"}']
        package_args += ['--java-options', '-Dgenova.demo.manual=true']
        package_args += ['--java-options', f'-Dgenova.demo.output={HERE / "demo" / "output"}']
    package = subprocess.run(package_args, capture_output=True, text=True)
    if package.returncode:
        app = package_dest / 'GENOVA Studio.app'
        if 'Disallowed xattr com.apple.FinderInfo' not in package.stderr + package.stdout:
            print(package.stdout + package.stderr)
            package.check_returncode()
        # File Provider can attach FinderInfo while jpackage is signing this new
        # local build. Remove only that metadata on this generated app root.
        subprocess.run(['/usr/bin/xattr', '-d', 'com.apple.FinderInfo', str(app)], check=True)
        subprocess.run(['/usr/bin/codesign', '--force', '--sign', '-', str(app)], check=True)
    built_app = package_dest / 'GENOVA Studio.app'
    subprocess.run(['/usr/bin/codesign', '--verify', '--deep', '--strict', str(built_app)], check=True)
    # Preserve runtime symlinks: following them changes the signed bundle's
    # resource layout even when the copied file bytes are identical.
    shutil.copytree(built_app, dest / 'GENOVA Studio.app', symlinks=True, copy_function=shutil.copy)
    subprocess.run(['/usr/bin/ditto', '--norsrc', '-c', '-k', '--keepParent', str(built_app), str(dest / 'GENOVA Studio.zip')], check=True)
    # Verify the deliverable after extraction. File Provider may later attach
    # FinderInfo to the workspace copy of .app, invalidating its local check.
    with tempfile.TemporaryDirectory(prefix='genova-verify-', dir='/private/tmp') as verify_dir:
        subprocess.run(['/usr/bin/ditto', '-x', '-k', str(dest / 'GENOVA Studio.zip'), verify_dir], check=True)
        subprocess.run(['/usr/bin/codesign', '--verify', '--deep', '--strict',
                        str(Path(verify_dir) / 'GENOVA Studio.app')], check=True)
    metadata['app'] = str(dest / 'GENOVA Studio.app')
(BUILD / 'latest.json').write_text(json.dumps(metadata, indent=2) + '\n')
print(json.dumps({k:v for k,v in metadata.items() if k != 'engine_source_sha256'}, indent=2))
