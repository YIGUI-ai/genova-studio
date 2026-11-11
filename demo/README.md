# GENOVA Studio demonstration

This folder contains the reproducible GENOVA-only demonstration workflow. It uses
the official nested `person × (item:rater)` example converted to an Excel workbook.
The packaged application imports that workbook, configures the nested design, runs
the real jGENOVA engine, opens the ANOVA, variance, D-study, heatmap, and 3D views,
then exports Excel, CSV, TXT, grid CSV, and a PNG plot into `output/`.

The data are a demonstration fixture reconstructed from the supplied official
jGENOVA input. They are not empirical research data.

## One-command run

From the project root:

```sh
python3 genova-gui/demo/run_demo.py
```

The script prepares the Excel fixture, builds the demo package, launches the
packaged application, and lets the application run the English GENOVA workflow
automatically. Add `--record` to start the native window-only MP4 recorder:

```sh
python3 genova-gui/demo/run_demo.py --record
```

The recorder filters the exact `GENOVA Studio · Mac Preview` window title; Codex,
Terminal, file chooser windows, and other desktop windows are excluded.

## macOS permissions

The first run may require:

* **Screen Recording** for the process hosting `genova-window-recorder`, so macOS can
  provide the selected GENOVA Studio window's pixels.

Grant these in System Settings → Privacy & Security. The workflow does not bypass
these protections. If a permission is denied, the script stops with an actionable
error rather than recording a full desktop or a fabricated result.

During the automated demo the known workbook is imported directly from the isolated
`demo/data` folder, so the macOS file chooser is not shown at all. In the normal
Import flow, the chooser opens in that isolated folder when the demo directory is
configured; unrelated files are never included in the recording.

## Scope and limitations

This demonstration intentionally covers the production GENOVA module only. It does
not demonstrate urGENOVA or mGENOVA pilot functionality. The selected nested design
uses the verified original jGENOVA engine. The 3D surface interpolates between the
computed D-study grid points for presentation; the white markers are the actual
engine scenarios.
