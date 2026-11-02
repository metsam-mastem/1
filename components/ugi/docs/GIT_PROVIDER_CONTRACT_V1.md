# GitProvider Contract v1.0

Status: **Python UGI adapter implemented; Android/JGit adapter mapping specified but not yet implemented as a common interface.**

This contract is the compatibility boundary between independent Git runtimes. It does not require UGI and SOVEREIGN OS to share a process, language, storage engine, or dependency graph. Cross-runtime repository transfer uses standard Git Bundle files.

## Required operations

| Operation | Input | Required outcome |
|---|---|---|
| `health()` | none | Reports local provider health; must not imply a remote connection is available |
| `repositories()` | none | Lists repositories owned by the provider's configured workspace |
| `create_repository(name)` | valid repository name | Creates an empty repository with an initial branch; rejects path traversal and conflicting destinations |
| `status(name)` | repository name | Returns current branch and working-tree/index changes without modifying them |
| `stage_all(name)` | repository name | Stages tracked and untracked changes; does not commit |
| `commit(name, message, author_name, author_email)` | repo + commit metadata | Creates one commit; rejects blank messages and invalid identities |
| `log(name)` | repository name | Returns newest-first commit history with stable object IDs |
| `export_bundle(name, destination)` | repo + destination path | Exports reachable refs and history in standard Git Bundle format |
| `import_bundle(source, repository_name)` | Bundle + new repo name | Imports into a new repository; MUST NOT overwrite or merge into an existing repository |

## Invariants

1. Provider identity is stable and exposed as `provider_id`.
2. Local Git operations are local by default. Network operations are separate explicit capabilities and never implicit side effects of status, staging, commit, history, or Bundle transfer.
3. Import is non-destructive: an existing target is rejected before any existing worktree is modified.
4. Bundle import validates the Git Bundle signature, enforces a configured size limit, and cleans temporary/partial targets on failure.
5. Bundle export/import must preserve commit object IDs and reachable refs supported by the source runtime.
6. Provider errors must be surfaced as bounded, user-readable errors without leaking credentials or process environment data.
7. Repository names and relative paths are validated independently of the UI.
8. No provider may claim a capability it has not implemented and tested.

## Current implementation mapping

| Contract operation | UGI `UgiLocalAdapter` | Android `GitWorkspace` primitive |
|---|---|---|
| `health` | `health` | No provider-level health wrapper yet |
| `repositories` | `repositories` | `repositoryNames` |
| `create_repository` | `create_repository` | `createRepository` |
| `status` | `status` | `status` → `GitStatusSnapshot` |
| `stage_all` | `stage_all` | `stageAll` |
| `commit` | `commit` | `commit` (author configured on workspace instance) |
| `log` | `log` | `recentCommits` → `GitCommitInfo` |
| `export_bundle` | `export_bundle` | `exportBundle` |
| `import_bundle` | `import_bundle` | `importBundle` |

The UGI Python `GitProvider` Protocol is runtime-checkable and is implemented by `UgiLocalAdapter`. The Android class currently exposes equivalent primitives, but no `JGitProviderAdapter` implementing this full contract is included in the inspected GitHub source snapshot. That remains an integration task, not a completed feature.

## Versioning and compatibility

- Contract version: `1.0`.
- Additive optional fields are allowed.
- Removing or renaming required operations, changing non-destructive import semantics, or changing Bundle compatibility requires a new major contract version.
- Each implementation must run the same behavioral fixture set for repository lifecycle, status/staging, commit/history, Bundle round-trip, invalid Bundle rejection, and existing-target protection.

## Test evidence

The UGI test suite contains provider conformance tests against a live loopback UGI server. Android/JGit conformance remains **NOT RUN** until the adapter and corresponding Kotlin tests are added and executed in Android CI.
