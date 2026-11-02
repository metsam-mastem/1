# SOVEREIGN Workbench

Universal Development Environment built around two independent engines and a shared integration contract.

## Project map

- UGI — independent, local-first Git infrastructure. Python local service + web UI; portable across desktop and Termux. Its Android-independent source package is being published under components/ugi/.
- SOVEREIGN OS — Android-native, offline-first personal development workspace. Its local Git implementation uses Eclipse JGit and remains independent of UGI's Python runtime.
- Integration boundaries are documented in docs/INTEGRATION_BOUNDARIES.md. The two runtimes are not merged.

## Current evidence (2026-11-02)

- UGI 1.1.0-alpha.4: local package verification completed; 26 automated tests passed, Python compile and JavaScript syntax checks passed. Remote source archive publication is a separate pending step.
- SOVEREIGN OS snapshot: mirrored from GitLab sheed-group/one, branch feat/sovereign-os-android-v0.1, source commit 0fb856bcce95ccf07589ad536363fb698568131f. Source and test files are included; an Android build and physical-device run have not been verified in this environment.
- This repository is an integration workbench, not a production release. See PROJECT_STATUS.md.

## Licensing

UGI is MIT-licensed in its own component package. The SOVEREIGN OS source branch did not contain a project-level LICENSE file or explicit license grant at the mirrored revision. Its project code is therefore marked license not declared; do not assume a right to reuse it. Third-party dependency licenses are listed separately. See each component's license status before reuse.

## Safety and contribution rule

Preserve component boundaries. Do not move private data, secrets, credentials, model weights, or internal commercial/research plans into this public repository. Changes to runtime behavior require tests before implementation and a recorded verification result.
