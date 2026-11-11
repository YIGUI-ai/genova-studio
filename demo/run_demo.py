#!/usr/bin/env python3
"""Build, launch, and optionally record the isolated GENOVA Studio demonstration."""
from __future__ import annotations

import argparse
import json
import subprocess
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
GUI = HERE.parent


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--record", action="store_true", help="record the GENOVA window to MP4")
    parser.add_argument("--duration", type=int, default=180, help="recording duration in seconds")
    args = parser.parse_args()

    subprocess.run(["python3", str(HERE / "prepare_demo_data.py")], check=True)
    subprocess.run(["python3", str(GUI / "build.py"), "--package", "--demo"], check=True)
    build = json.loads((GUI / "build" / "latest.json").read_text())
    app = Path(build["app"])
    output = HERE / "output"
    output.mkdir(exist_ok=True)
    subprocess.run(["open", "-n", str(app)], check=True)
    print(f"Launched: {app}")
    print(f"Demo outputs: {output}")
    if not args.record:
        print("Use --record after granting Screen Recording permission to the recorder host.")
        return 0

    time.sleep(2)
    video = output / "genova-studio-demo.mp4"
    recorder = HERE / "genova-window-recorder"
    result = subprocess.run([str(recorder), "GENOVA Studio · Mac Preview", str(video), str(args.duration)])
    if result.returncode:
        print("Recording failed before frames were captured. Check Screen Recording permission for the recorder host.")
        return result.returncode
    print(f"Video: {video}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
