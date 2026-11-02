# SOVEREIGN Workbench

Universal Development Environment built around two independent engines and a shared integration contract.

## Project map

- **UGI** — independent, local-first Git infrastructure. Python local service + web UI; portable across desktop and Termux. Its runtime is separate from SOVEREIGN OS.
- **SOVEREIGN OS** — Android-native, offline-first personal development workspace. Its local Git implementation uses Eclipse JGit and does not require UGI's Python runtime.
- **Integration boundaries** — provider-neutral Git capability contract and Git Bundle / standard remote exchange, without merging runtimes or private data stores.

## Current evidence (2026-11-02)

- **SOVEREIGN OS Android CI: PASS.** GitHub Actions run [#41373586476](https://github.com/metsam-mastem/1/actions/runs/41373586476) completed successfully. JDK 17, Android SDK 36, Gradle 8.13; 36/36 Debug unit tests and 36/36 Release unit tests passed; `:app:assembleDebug` passed. The workflow artifact contains the APK and HTML test reports.
- **Debug APK:** `app-debug.apk`, 61,512,310 bytes. SHA-256: `ade43ee4f807c232e81224f45299437b9f228b13ab0f6212b7d1bef48353da59`. This is a CI debug build, not a production/release-signed APK. No physical-device installation has been verified.
- **Source mirror:** SOVEREIGN OS source and test files mirrored from GitLab `sheed-group/one`, branch `feat/sovereign-os-android-v0.1`, source commit `0fb856bcce95ccf07589ad536363fb698568131f`. Upstream GitLab was not modified; private commercial/competitive research documents were excluded.
- **UGI 1.1.0-alpha.4:** separate local source archive contains 29 files; 26 automated tests, Python compile and JavaScript syntax checks passed locally. The ZIP is not yet committed to this GitHub repository.

This is an integration workbench, not a production release. See [PROJECT_STATUS.md](PROJECT_STATUS.md) and [Issue #1](https://github.com/metsam-mastem/1/issues/1).

## Licensing

UGI is MIT-licensed in its own component package. The SOVEREIGN OS upstream branch had no project-level LICENSE file or explicit license grant at the mirrored revision. Its project code is marked **license not declared**; do not assume reuse rights. Third-party dependency licenses are separately documented. See `components/sovereign-os/LICENSE_STATUS.md`.

## Safety and contribution rule

Preserve component boundaries. Do not add private data, secrets, credentials, model weights, or internal commercial/research plans to this public repository. Changes to runtime behavior require tests before implementation and recorded verification.
