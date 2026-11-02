# Universal Git Infrastructure (UGI)

Independent local-first Git workbench. Python local service + Persian RTL PWA; does not require the SOVEREIGN OS Android app.

## Current local candidate

- Package: **UGI 1.1.0-alpha.7** (development candidate; not stable/production).
- Runtime: Python >=3.11, Git CLI >=2.28.0; zero third-party Python runtime packages.
- Source license: MIT; PEP 639 metadata uses SPDX identifier MIT and declares LICENSE.
- Provider contract: Python Protocol v1.0 implemented by UgiLocalAdapter.
- Verification from a clean extraction of the final ZIP: **45 tests PASS**; compileall, JavaScript syntax checks and CLI version PASS.
- Reproducible package: two independent builds byte-for-byte identical.
- ZIP size: 65,562 bytes.
- ZIP SHA-256: b48273886beb7a4915a99d2cdb199e81062b3fd4a9c07ca04fea461f18200f7c.
- Companion CycloneDX 1.7 source-release BOM: releases/UGI-1.1.0-alpha.7.cdx.json.
- SBOM SHA-256: 45a446018723f63bf90d1327be055374431b571a4c399a96854c847119cc158b.
- SBOM scope is explicitly incomplete; JSON syntax/internal reference checks PASS, but official CycloneDX schema validation is NOT RUN.

## Alpha.7 security and reliability fixes

- Strict HTTP/1.1 request framing: reject Transfer-Encoding, duplicate/comma-combined Content-Length and ambiguous uploads; close malformed connections.
- Require application/json on mutating JSON endpoints.
- Apply the same framing rules to Git Bundle PUT uploads.
- Remove temporary clone-origin references after Bundle import so imported repositories do not retain dead upstream paths.
- Existing clone rollback, Fetch Metadata rejection, remote URL allow-list, Git version gate, bounded streaming and non-destructive import tests remain in the suite.

## Integration boundary

UGI remains an independent Python local-service + PWA component. SOVEREIGN OS remains an independent Android/JGit application. Cross-runtime transfer is based on standard Git Bundle files; neither runtime is embedded into the other.

Android's JGitProviderAdapter and Python's UgiLocalAdapter both pass implementation-level contract tests. The Android offline Bundle import now removes the temporary bundle origin; Android CI GREEN run: https://github.com/metsam-mastem/1/actions/runs/41459518928.

## Publication status

- The alpha.7 ZIP and detached SHA-256 sidecar are delivered as local files.
- GitHub contains the alpha.7 companion SBOM, its checksum, SBOM scope, and a Python 3.11–3.15 CI workflow.
- The full alpha.7 ZIP is **not yet committed** to this public repository. The hosted Python matrix is therefore **NOT RUN** until the release ZIP is published and fetched back for SHA-256 comparison.

## Open gates

Physical-device acceptance (Android/Termux), Windows/macOS acceptance, browser E2E/accessibility, hosted Python matrix, official CycloneDX schema validation, deployment-specific SBOM, fuzz/property testing, independent security review, and signed release remain NOT RUN or OPEN.

See:
- [GitProvider Contract v1.0](docs/GIT_PROVIDER_CONTRACT_V1.md)
- [Alpha.7 SBOM scope](docs/SBOM_SCOPE_ALPHA7.md)
- [Open-source ecosystem and standards audit](../../docs/OPEN_SOURCE_AND_STANDARDS_AUDIT_2026-11-02.md)
