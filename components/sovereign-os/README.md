# SOVEREIGN OS — Android development snapshot

Personal, offline-first intelligence workbench for Android. This component is mirrored from GitLab project sheed-group/one, branch feat/sovereign-os-android-v0.1, source commit 0fb856bcce95ccf07589ad536363fb698568131f.

## Included capabilities

- Android Sharesheet and Storage Access Framework intake into app-private storage, SHA-256 fingerprints, duplicate markers and text handoff.
- Local Git through Eclipse JGit: repositories, status/diff, stage, commit, local branches/history, file import and offline Git Bundle exchange.
- HTTPS remote clone/fetch/push with explicit actions, ephemeral authentication input and clean-worktree push confirmation.
- Three model routes: embedded LiteRT-LM, a user-configured local server, or manual Clipboard/Sharesheet handoff.
- Read-only model context; generated answers do not directly edit, commit, push or merge.

## Build requirements stated by upstream

JDK 17, Android SDK 36, Gradle 8.13+, Android Gradle Plugin 8.13.2. Run gradle test and gradle :app:assembleDebug from this component folder in a configured Android environment.

## Verification limits

The upstream branch's test source is included. Its GitLab CI was blocked by an identity-verification gate; no successful build, APK artifact or physical-device test is claimed here. This mirror operation did not run Gradle because the current verification environment has not been established as an Android SDK/JDK build host.

## License status

No project-level LICENSE file or explicit license grant was present at the mirrored source revision. See LICENSE_STATUS.md. Third-party components have separate license terms; see THIRD_PARTY_NOTICES.md.

## Upstream

https://gitlab.com/sheed-group/one/-/tree/feat/sovereign-os-android-v0.1
