# Third party notices

This file records third-party material included in GENOVA Studio and the basis on which it is retained. It does not replace the terms supplied by a third-party rights holder and does not assign third-party material a new license.

## jGENOVA 1.0 balanced statistical core

- **Authors and developers:** Joe E. Crick and Robert L. Brennan.
- **Project website:** https://brennancrickgenova.org/
- **Source download page:** https://brennancrickgenova.org/genova-suite/
- **Website checked:** 18 November 2026.
- **Original local source archive:** `jGENOVAsource.zip`, retained outside this repository.
- **Original archive SHA-256:** `e6ae00cf459c956d1998ae50a8f517a477bcfdaf0e8a56d954028696fcf22f2d`.
- **Repository location:** `third_party/jGENOVA1.0/src/initGen/`.

The authors' website publicly provides both the jGENOVA application and its Eclipse-workspace source archive. The site states that the programs “may be distributed freely to others,” and the GENOVA Suite page separately provides the jGENOVA source-code download.

The source archive does not contain a standalone SPDX or standard open-source license file. GENOVA Studio therefore preserves the jGENOVA source unchanged, credits its authors, and relies on the authors' published redistribution statement for distribution of this component. The BSD 3-Clause License in `LICENSE.txt` applies only to original GENOVA Studio code and materials; it does not apply to jGENOVA. This project does not claim a right to relicense jGENOVA, and it does not imply endorsement by Crick or Brennan.

GENOVA Studio invokes the unchanged balanced jGENOVA computation through its own adapter and process-isolation layer. The separately implemented unbalanced and multivariate pilot engines are GENOVA Studio code and are not represented as the original urGENOVA or mGENOVA programs.
