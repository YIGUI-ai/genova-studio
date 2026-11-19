# Frozen bundled-runtime validation record — 18 November 2026

## Outcome

**PASS.** The complete 543-assertion conventional `SelfTest` suite passed with the Java runtime and application JAR extracted from the frozen submission-candidate ZIP. This run includes the 76 Gleser checks and the 51 CiviConnections checks that had previously been exercised only in the development environment.

The candidate is frozen by artifact hashes for manuscript preparation. It is **not** yet a tagged public release: the source worktree was dirty at validation time, the repository remains private, and release licensing/rights decisions remain open.

## Frozen candidate

- ZIP: `dist/20261118-214156/GENOVA Studio.zip`
- ZIP SHA-256: `71e931521e6555e45d97f88691f84c2304bca27d6406b74325f0c758e5cce8ce`
- Extracted application JAR SHA-256: `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`
- Development-build JAR SHA-256: `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`
- Git HEAD at validation: `7ac27682e75f8a88ff8470e338d72743ab8b8d77`
- Worktree clean at validation: **no**

The matching JAR hashes establish that the test executed the same application bytecode that was built for the candidate. `validate.py` extracted the ZIP to a temporary directory, verified the extracted app's local code signature, and invoked the bundled `Contents/runtime/Contents/Home/bin/java` directly.

## Runtime and platform

- Bundled Java: `openjdk version "25.0.2" 2026-01-20`
- Runtime: `OpenJDK Runtime Environment Homebrew (build 25.0.2)`
- VM: `OpenJDK 64-Bit Server VM Homebrew (build 25.0.2, mixed mode)`
- Host: Apple Silicon (`arm64`), macOS 27.2.1, Darwin 27.2.0

## Checks completed in the validation session

| Check | Result | Evidence boundary |
| --- | ---: | --- |
| Full `SelfTest` from ZIP-extracted bundled runtime | 543/543 pass | Includes Gleser 76 and CiviConnections 51 |
| Code signature after ZIP extraction | Pass | Local signature verification; this is not notarization |
| JAR identity across development, packaged app, and ZIP extraction | Pass | All three SHA-256 values are identical |
| Original downloaded-distribution hashes | 95/95 unchanged | Confirms retained originals were not modified |
| Official balanced full report | Pass | Equal after normalizing run date and newline style |
| Jiang OSF subdesign comparisons | 28/28 pass | Separate external-data script in the same validation session; not part of the 543 bundled-runtime count |

## Evidence files

- `validation/release_candidate_manifest.json` — machine-readable artifact, runtime, platform, Git state, check summary, and build-input hashes.
- `validation/results.json` — complete validation result record.
- `validation/packaged_zip_selftest.log` — full 543-check output from the ZIP-extracted bundled runtime.
- `validation/packaged_selftest.log` — full 543-check output from the bundled runtime in the packaged app directory.
- `validation/selftest.log` — corresponding development-JDK run.
- `validation/osf_jiang_subdesigns.log` — 28 separate Jiang subdesign comparisons.

## Reproducibility note

The hash-frozen ZIP is adequate as the manuscript's current tested candidate. Before public release, commit or otherwise archive the intended source state, resolve license and third-party-rights requirements, tag the exact release, rebuild it, and rerun this procedure so the public release can be tied to an immutable source revision as well as artifact hashes.
