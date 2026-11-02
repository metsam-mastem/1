# SOVEREIGN Workbench

Universal Development Environment built around two independent engines and a shared integration contract.

## Project map

- **UGI** — independent, local-first Git infrastructure. Python local service + web UI; portable across desktop and Termux. Its Android-independent runtime is separate from SOVEREIGN OS.
- **SOVEREIGN OS** — Android-native, offline-first personal development workspace. Its local Git implementation uses Eclipse JGit and does not require UGI's Python runtime.
- **Integration boundaries** — provider-neutral contract and Git Bundle / standard remote exchange, without merging runtimes or private data stores.

## Current evidence (2026-11-02)

- UGI 1.1.0-alpha.4: the locally staged source archive contains 29 files; 26 automated tests, Python compile check and JavaScript syntax checks passed locally. The source archive has not yet been committed as a binary file in this GitHub repository.
- SOVEREIGN OS: source, test files and technical documents mirrored from GitLab `sheed-group/one`, branch `feat/sovereign-os-android-v0.1`, source commit `0fb856bcce95ccf07589ad536363fb698568131f`. Android CI has been added; see Actions for current result. Physical-device verification remains outstanding.
- This is an integration workbench, not a production release. See [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Licensing

UGI is MIT-licensed in its own component package. The SOVEREIGN OS upstream branch had no project-level LICENSE file or explicit license grant at the mirrored revision. Its project code is marked **license not declared**; do not assume reuse rights. Third-party dependency licenses are separately documented. See `components/sovereign-os/LICENSE_STATUS.md`.

## Safety and contribution rule

Preserve component boundaries. Do not add private data, secrets, credentials, model weights, or internal commercial/research plans to this public repository. Changes to runtime behavior require tests before implementation and recorded verification.
