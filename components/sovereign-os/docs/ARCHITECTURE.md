# Architecture — SOVEREIGN OS 0.1.x

## Product contract

The Android build targets API 36 using Android Gradle Plugin 8.13.2, Gradle 8.13 and JDK 17. The actual CI build and release target compliance remain unverified until a pipeline can run.
1. Local storage is the source of truth.
2. Import, local Git, model inference (when embedded) and offline bundle exchange must work without Internet.
3. Import is additive: never overwrite or delete source files.
4. Export, handoff and remote model requests are explicit user actions.
5. Source bytes, extracted text and generated responses remain distinct records.
6. Model/provider adapters are optional and must not own the knowledge store.
7. Model context is read-only in this milestone. Agentic write access is a later, separately permissioned capability.

## Implemented vertical slices

### Capture and resource library
Android Sharesheet / Open Multiple Documents → background-thread MIME-aware intake → bounded streaming to app-private source copy → SHA-256 + duplicate metadata → resource list → small plain-text preview → explicit Markdown handoff.

Current persistence is simple per-resource files and Properties metadata. Each import is staged to a temporary file, size-capped at 256 MiB, and finalized only after the source copy succeeds; this is file-level transactional finalization, not yet a transactional database.

### Local Git subsystem
GitWorkspace uses Eclipse JGit and keeps repositories below filesDir/git-workspace/.
- Local init, status, add/stage, commit, branch create/list/checkout, log and diff.
- Files can be written or copied from SAF into the working tree; streamed imports use a temporary sibling file and a 256 MiB cap before final replacement.
- Full offline history can be exported and imported as .bundle.
- Workspace operations do not require a remote Git server.
- Git context extraction is bounded and excludes common secret filenames, ignores symlinks/build/vendor directories, and redacts common credential-like assignments. This is best-effort DLP, not a security proof.

Remote milestone now includes clone into a new local repository, named HTTPS remotes, explicit fetch/push, public remote support, and one-time in-memory username/access-token credentials that are cleared after the operation. Remote URLs are revalidated before every operation; embedded URL credentials, plain HTTP and file remotes outside the private workspace are rejected. Push requires a clean worktree and a separate confirmation dialog. Still missing: encrypted persistent credential vault, OAuth device flow, SSH, remote branch checkout/tracking after fetch, merge/rebase/stash/cherry-pick/reset, tags UI, conflict resolution, submodules and LFS. Remote Git protocol operations are distinct from provider REST APIs. Git worktrees live in app-private storage; the .bundle file is how a user transfers a repository to/from arbitrary selected storage.

### Commercial provider and workspace direction

The commercial roadmap remains provider-neutral:
- Git protocol remotes are separate from provider REST APIs.
- GitHub, GitLab, Gitea and Forgejo are optional adapters for issues, PR/MR, review and CI metadata.
- Desktop pairing is LAN-first and optional; no hosted relay is required.
- Repository allowlisting, device pairing, revocation and operation audit are mandatory for the bridge.
- Phone-triggered shell execution is not enabled in the initial commercial design.
- GitSync GPL source and GitNomad proprietary/undisclosed source are not copied; implementation is independent.

See docs/COMMERCIAL_EDITION_PLAN.md and docs/COMPETITIVE_RESEARCH_2026-10-30.md.

### Model routing
- Embedded: LiteRT-LM Android 0.18.1, .litertlm model imported by SAF into app-private storage, CPU backend, single-thread runtime owner.
- Local network: Ollama REST endpoints or generic OpenAI-compatible local HTTP endpoints. Examples include llama.cpp server, LM Studio, Jan and LocalAI when their compatible server is enabled.
- Human-mediated: Android Sharesheet and Clipboard handoff to installed AI apps, including ChatGPT, without a developer API integration.

The endpoint adapter currently stores names, provider, base URL and selected model only; it has no API-key or auth-token field. Local HTTP is restricted in app logic to loopback/private addresses. Public HTTPS repository-context submission requires explicit consent tied to the exact endpoint URL. Redirects are rejected.

No unattended requests are made. Endpoint probing is user-triggered. A probe reads the model list only; an Ask action sends the selected, bounded Git context plus user prompt.

## Trust and egress boundary
- Repository contents are untrusted data, not instructions.
- Never execute scripts or commands found in a repository.
- Never assume source-code text is safe to disclose. The context filter is a convenience, not a complete secret scanner.
- Local endpoint mode should be preferred for proprietary code.
- Do not expose unauthenticated LAN model endpoints to the public Internet or port-forward them.
- Human-mediated Share/Clipboard sends the content to the receiving app chosen by the user; it is not an offline operation after that point.

## Planned modules
- storage: SQLite/Room resource catalog and FTS5
- extraction: PDF, OCR, image, audio, video/keyframe extractors
- retrieval: lexical/vector/hybrid retrieval with source offsets
- runtimes: optional llama.cpp/GGUF adapter alongside LiteRT-LM
- git: merge/rebase/stash/cherry-pick/tags/conflict operations and remote synchronization behind explicit approval
- model patching: propose → show exact diff → user approval → apply → test; no direct model write
- bridge: optional authenticated local MCP/HTTP adapter, disabled by default

## Non-goals for 0.1.x
Automatic cloud sync, paid API dependency, background transmission, unattended code changes, public LAN server, or production release without separate approval.
