# Acceptance criteria — SOVEREIGN OS 0.1.x

## Android intake
- [ ] GitLab CI passes unit tests and builds a debug APK.
- [x] Native Android project and Compose entry screen committed.
- [x] Sharesheet receive/send and SAF multi-file picker wired.
- [x] Source bytes copied into app-private storage; originals remain untouched.
- [x] Resource and Git file imports stream through temporary files, enforce a 256 MiB per-item cap, and finalize only after a complete copy.
- [x] Incoming Sharesheet data is imported on an IO dispatcher rather than the activity's main thread.
- [x] SHA-256 and duplicate metadata recorded.
- [x] Explicit Markdown/Clipboard/Sharesheet handoff.

## Offline Git
- [x] Create local repositories without network access.
- [x] Read status/branches, stage, commit and inspect recent history.
- [x] Create and checkout local branches.
- [x] Write text files and import binary files.
- [x] Export/import repository refs and reachable history using Git Bundle.
- [x] Guard repository paths against traversal and .git metadata overwrite.
- [x] Bound and filter the Git context used for model analysis.
- [ ] Run Git unit tests, new import/credential regression tests and offline bundle round-trip in an actual Android/JVM build.
- [ ] Physical-device test of Git operations and SAF bundle exchange.
- [ ] Merge/rebase/stash/cherry-pick/reset/tags/conflict resolution.
- [x] Remote fetch/push with explicit credential and network policy; actual execution still requires a passing integration test.

## Remote Git and provider integrations — commercial roadmap
- [x] Add/remove/list named HTTPS remotes with URL validation.
- [x] Explicit HTTPS/file-workspace fetch and push operations; push requires a clean worktree and user confirmation.
- [x] One-time in-memory username/access-token input; clear the character array after the operation and never persist it.
- [ ] Secure persistent credential vault backed by Android Keystore; never persist PATs in preferences or logs.
- [x] Authenticated HTTPS fetch/push using ephemeral JGit credential providers.
- [ ] SSH keys and host-key verification; no permissive host-key bypass.
- [ ] OAuth device authorization for GitHub/GitLab with least-privilege scopes.
- [x] Clone flow into a new repository and checkout fetched remote branches as local branches with upstream tracking; automated tests are committed but await execution.
- [ ] Network progress reporting and cancelable network operations.
- [ ] Provider API adapters for GitHub PR/issues/checks and GitLab MR/issues/pipelines.
- [ ] Gitea/Forgejo and self-managed GitLab base URL support.
- [ ] Manual sync first; background scheduling only with explicit per-repository opt-in.
- [ ] Offline queue, retry/backoff and conflict state; never auto-resolve destructive divergence.
- [ ] Signed commits, tags, LFS, submodules and partial/shallow clone compatibility tests.

## AI connections
- [x] Save named local model endpoints without storing keys.
- [x] Probe Ollama /api/tags and OpenAI-compatible /v1/models.
- [x] Discover models and submit read-only Git context on explicit user action.
- [x] Restrict plain HTTP to loopback/private LAN through endpoint policy.
- [x] Require per-endpoint consent before sending Git context to a public HTTPS host.
- [x] Offer no-API app-to-app Sharesheet and Clipboard handoff.
- [x] Import/select/load/unload .litertlm models with a CPU-backed LiteRT-LM adapter.
- [ ] Compile and run the embedded runtime on a real Android device.
- [ ] Add llama.cpp/GGUF embedded runtime as an independent adapter.
- [ ] Add authenticated endpoint support only after secure secret storage and consent UX are defined.
- [ ] Add model patch preview/apply workflow with independent confirmation.
- [ ] Add local authenticated MCP/tool bridge.

## Commercial product gates
- [ ] Feature/edition boundaries implemented without disabling core offline Git or data export.
- [ ] Full SBOM and license/NOTICE review, including transitive dependencies.
- [ ] Independent security review of credential storage, provider OAuth and desktop pairing.
- [ ] Opt-in diagnostics only; privacy and network egress audit.
- [ ] Accessibility, localization, backup/restore and migration testing.
- [ ] Terms, privacy policy, support/incident process, trademark review and release signing.

## Release and verification
- [x] Set compileSdk/targetSdk to API 36 for the current Google Play target-level requirement; verify with an actual CI build before release.
- [ ] Verify Android 36 build, tests and debug APK artifact in CI.
- [ ] Install and test on target Android device with network disabled.
- [ ] Verify local server endpoint from a second device on a trusted LAN.
- [ ] Verify redaction tests against JSON/YAML/text secret patterns.
- [ ] Signed release APK/AAB only after explicit signing/release approval.
