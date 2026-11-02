# GitProvider Contract v1.0

Status: **Python UGI adapter and Android/JGit adapter implemented; both have implementation-level conformance tests.** The latest Android CI run is green.

This contract is the compatibility boundary between independent Git runtimes. It does not require UGI and SOVEREIGN OS to share a process, language, storage engine, or dependency graph. Cross-runtime repository transfer uses standard Git Bundle files.

## Required operations

| Operation | Input | Required outcome |
|---|---|---|
| `health()` | none | Reports local provider health; must not imply a remote connection is available |
| `repositories()` | none | Lists repositories owned by the provider's configured workspace |
| `create_repository(name)` | valid repository name | Creates an empty repository with an initial branch; rejects path traversal and conflicting destinations |
| `status(name)` | repository name | Returns current branch and working-tree/index changes without modifying them |
| `stage_all(name)` | repository name | Stages tracked and untracked changes; does not commit |
| `commit(name, message)` | repository and commit message | Creates one commit; rejects blank messages. Author identity comes from provider configuration on Android and explicit arguments in the UGI Python adapter. |
| `log(name)` | repository name | Returns newest-first commit history with stable object IDs |
| `export_bundle(name, destination)` | repo + destination path | Exports reachable refs and history in standard Git Bundle format |
| `import_bundle(source, repository_name)` | Bundle + new repo name | Imports into a new repository; MUST NOT overwrite or merge into an existing repository |

## Invariants

1. Provider identity is stable and exposed as `provider_id`; the contract version is `1.0`.
2. Local Git operations are local by default. Network operations are separate explicit capabilities and never implicit side effects of status, staging, commit, history, or Bundle transfer.
3. Import is non-destructive: an existing target is rejected before any existing worktree is modified.
4. Bundle import validates the Git Bundle signature, enforces a configured size limit, and cleans temporary/partial targets on failure.
5. Bundle export/import must preserve commit object IDs and reachable refs supported by the source runtime.
6. Provider errors must be surfaced as bounded, user-readable errors without leaking credentials or process environment data.
7. Repository names and relative paths are validated independently of the UI.
8. No provider may claim a capability it has not implemented and tested.

## Current implementation mapping

| Contract operation | UGI `UgiLocalAdapter` | Android `JGitProviderAdapter` |
|---|---|---|
| `health` | `health` | `health` → `GitProviderHealth` |
| `repositories` | `repositories` | `repositories` → `GitProviderRepository` |
| `create_repository` | `create_repository` | `createRepository` |
| `status` | `status` | `status` → `GitStatusSnapshot` |
| `stage_all` | `stage_all` | `stageAll` → staged snapshot |
| `commit` | `commit` | `commit` → `GitProviderCommitReceipt` |
| `log` | `log` | `log` → `GitCommitInfo` |
| `export_bundle` | `export_bundle` | `exportBundle` |
| `import_bundle` | `import_bundle` | `importBundle` |

The UGI Python `GitProvider` Protocol is runtime-checkable and is implemented by `UgiLocalAdapter`. The Android `JGitProviderAdapter` is a facade over the independent `GitWorkspace` JGit engine. It does not embed the UGI Python service.

Android Bundle import checks the v2/v3 Bundle header and applies the shared 256 MiB import cap before handing validation and repository creation to JGit/ `GitWorkspace`. Existing destinations are rejected by `GitWorkspace` and failed imports clean their partial target.

## Versioning and compatibility

- Contract version: `1.0`.
- Additive optional fields are allowed.
- Removing or renaming required operations, changing non-destructive import semantics, or changing Bundle compatibility requires a new major contract version.
- Each implementation must run the same behavioral fixture concepts for repository lifecycle, status/staging, commit/history, Bundle round-trip, invalid Bundle rejection, and existing-target protection. Platform test syntax may differ.

## Test evidence

- UGI: provider conformance tests run against a live loopback UGI server; local alpha.5 verification reports 32 tests + 12 subtests PASS.
- Android/JGit: [test-first RED run #41387563469](https://github.com/metsam-mastem/1/actions/runs/41387563469) confirmed the missing adapter before implementation. [GREEN run #41387657992](https://github.com/metsam-mastem/1/actions/runs/41387657992) passed the Android unit-test task and Debug APK assembly.
- Physical-device Android acceptance remains **NOT RUN**. CI success is not evidence of installation or runtime behavior on the target phone.
