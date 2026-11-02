# Open-source ecosystem, dependency and standards audit

Date: 2026-11-02  
Scope: UGI and SOVEREIGN OS only. The two products remain independent runtimes; integration is limited to GitProvider v1.0 and standard Git Bundle interchange.

## Executive decisions

- UGI alpha.6 remains Git CLI-backed and Python standard-library-only. This keeps the Python dependency and license surface small while retaining reference Git behavior.
- Python 3.10 reached end of life on 2026-10-01. UGI now requires Python >=3.11. Python 3.11 receives security fixes through Oct 2027, 3.12 through Oct 2028, and 3.13 through Oct 2029.
- SOVEREIGN OS retains Eclipse JGit 7.8.0. It remains the independent Android Git engine and is not a dependency of UGI.
- Android dependency migration PR #2 is green in CI run #41420812073 but remains Draft/open and is not merged. Tested PR versions: AGP 9.3.1, Gradle 9.5.0, Kotlin/KGP 2.4.21, Compose BOM 2026.09.00, Activity 1.13.0, Lifecycle 2.11.0, LiteRT-LM 0.19.0, JGit 7.8.0, compileSdk 37 preview and targetSdk 36. Two AGP migration opt-out properties still emit deprecation warnings.
- Git Bundle exchanges refs and Git objects; it is not a complete workspace backup.
- Security/process references: OWASP ASVS 5.0.0, NIST SSDF SP 800-218 v1.1, CycloneDX 1.7 / ECMA-424 2nd edition.

## Open-source Git engine comparison

| Project | Language/model | Advantages | Limits and decision |
|---|---|---|---|
| Git CLI | C, reference implementation | Reference semantics, broad feature set, available on desktop and Termux | Retained in UGI; timeout, argument arrays, hook disabling and risky config restrictions added. Not a complete sandbox for malicious repositories. |
| Eclipse JGit 7.8.0 | Java library | Standalone Java Git implementation, suitable for Android | Retained in SOVEREIGN OS only; transitive notices and Gradle SBOM remain open. |
| Dulwich | Pure Python | Git object/protocol implementation; Protocol v2, Bundle URI and SHA-256 repository capabilities | Dual-licensed Apache-2.0 OR GPL-2.0-or-later; future isolated POC only after license review and behavioral compatibility tests. |
| isomorphic-git | Browser/Node JavaScript | Git operations in browser or Node; useful for OPFS/IndexedDB research | Potential PWA storage POC; do not replace Git CLI without browser/device tests and Bundle/remote benchmarks. |
| gitoxide (gix) | Rust | Portable Rust implementation; Apache-2.0 OR MIT licensing | Crate maturity varies; CLI portions may be unstable. Benchmark/research candidate, not production runtime yet. |

No upstream implementation files were copied during this audit. Source-level reuse requires a pinned upstream commit, exact file-level license and notice review, provenance, attribution, and compatibility tests. Prefer separately authored interfaces/tests or official declared dependencies over unreviewed code copying.

## Runtime and dependency inventory

### UGI alpha.6

| Layer | Status |
|---|---|
| Python | >=3.11; standard-library-only; empty third-party Python dependency list in pyproject.toml |
| Git CLI | >=2.28.0; checked at startup/health |
| HTTP | Python stdlib http.server/http.client; loopback-only |
| Web UI | HTML/CSS/JavaScript without a third-party runtime framework or CDN |
| QA | unittest, compileall, Node --check; Node is not a product runtime dependency |

### SOVEREIGN OS Android migration PR

| Component | Candidate PR version | Evidence/status |
|---|---:|---|
| Android Gradle Plugin | 9.3.1 | CI PASS in PR #2 |
| Gradle | 9.5.0 | CI PASS in PR #2 |
| JDK | 17 | Previously verified and retained |
| Kotlin / Compose compiler plugin | 2.4.21 | Within documented AGP/KGP compatibility; CI PASS |
| Compose BOM | 2026.09.00 | Dependency resolution PASS |
| Activity Compose | 1.13.0 | Dependency resolution PASS |
| Lifecycle | 2.11.0 | Dependency resolution PASS |
| Eclipse JGit | 7.8.0 | Retained; no upgrade without a specific fix/benchmark |
| LiteRT-LM Android | 0.19.0 | Dependency resolution/build PASS; device/model ABI and latency acceptance open |
| compileSdk / targetSdk | 37 preview / 36 stable | Compile-only preview; target API intentionally remains stable |

Kotlin KGP 2.4.21 is fully supported with AGP through 9.3.1 and Gradle through 9.7.0. AGP 9.4 is newer but outside that published KGP compatibility range; do not chase a version number at the cost of a supported toolchain. PR #2 is not merged. The properties android.builtInKotlin=false and android.newDsl=false still trigger deprecation warnings and are migration debt for a later tested change.

## Security and engineering standards

| Standard | Reference | Project use |
|---|---|---|
| OWASP ASVS | 5.0.0 stable | Testable controls for local HTTP API, input validation, file/Bundle import, path handling, error surfaces and trust boundaries. This project is not ASVS certified. |
| NIST SSDF | SP 800-218 v1.1 final | Secure development workflow: provenance, review, regression tests, dependency risk management and release evidence. |
| CycloneDX | 1.7; ECMA-424 2nd edition (Dec 2025) | Component, license, provenance, vulnerability and dependency inventory. UGI has a scoped source-release BOM; Android Gradle SBOM is not yet generated. |
| Git Bundle format | v2/v3 | Interchange of Git objects and refs. Destination prerequisites still need to exist; a Bundle is not a full backup. |

### Verified controls

- UGI: loopback/Host/Origin checks, remote URL allow-list, bounded Git subprocesses, hook/fsmonitor/external-diff controls, redirects rejected by local client, request/response size caps, chunked and ambiguous Content-Length rejection, non-destructive Bundle import and deterministic release package tests.
- Android: test-first RED-to-GREEN cycle for JGitProviderAdapter; baseline and adapter CI passed. Android dependency migration PR #2 also passed dependency resolution, unit tests and Debug APK assembly.
- Open: physical-device acceptance, additional fuzz/property tests, independent penetration test, Android CycloneDX BOM and transitive license scan, LiteRT on-device ABI/performance, production signing. UGI source BOM is explicitly incomplete and does not inventory an OS deployment.

## Next work

1. Keep Android PR #2 unmerged until review of its deprecation warnings and device/API compatibility; targetSdk remains 36.
2. Generate CycloneDX 1.7 BOM from the actual Gradle dependency graph with PURLs, versions, licenses, suppliers, hashes and dependency edges. The official Gradle plugin 3.5.1 has been researched but not added.
3. Extend UGI fuzz/property tests for HTTP framing, URL parser, path validation and malformed Bundle inputs.
4. Run physical-device acceptance on the target Android phone: install/launch, permissions, Bundle round-trip, offline operations, memory/CPU and crash recovery.
5. Consider isolated Dulwich or gitoxide POCs only if a no-system-Git use case is established; require benchmarks, compatibility fixtures and license gates before adopting.
6. Complete publication: upload the full UGI alpha.6 ZIP to GitHub and fetch it back for SHA-256 verification. Companion SBOM and scope docs are published; the archive itself remains a local attachment.

## Primary upstream references

- Python lifecycle: https://devguide.python.org/versions/ and https://www.python.org/downloads/release/python-31022/
- AGP 9.3: https://developer.android.com/build/releases/agp-9-3-0-release-notes
- Android AGP/Gradle compatibility: https://developer.android.com/build/releases/about-agp
- Kotlin Gradle compatibility: https://kotlinlang.org/docs/gradle-configure-project.html
- Compose BOM: https://developer.android.com/develop/ui/compose/bom
- LiteRT-LM: https://github.com/google-ai-edge/LiteRT-LM
- Eclipse JGit 7.8.0: https://projects.eclipse.org/projects/technology.jgit/releases/7.8.0
- Dulwich: https://dulwich.io/ and https://dulwich.readthedocs.io/en/latest/
- isomorphic-git: https://isomorphic-git.org/ and https://github.com/isomorphic-git/isomorphic-git
- gitoxide: https://github.com/GitoxideLabs/gitoxide
- Git Bundle format: https://git-scm.com/docs/bundle-format
- OWASP ASVS 5.0.0: https://asvs.owasp.org/
- NIST SSDF SP 800-218: https://csrc.nist.gov/projects/ssdf
- CycloneDX 1.7 schema: https://cyclonedx.org/schema/bom-1.7.schema.json
- CycloneDX Gradle plugin: https://github.com/CycloneDX/cyclonedx-gradle-plugin
- ECMA-424 2nd edition: https://ecma-international.org/publications-and-standards/standards/ecma-424/
- SPDX: https://spdx.org/licenses/PSF-2.0.html and https://spdx.org/licenses/GPL-2.0-only.html
