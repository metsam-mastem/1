# Orchestration and evidence plan

Date: 2026-11-02
Target: SOVEREIGN Workbench / UGI integration only.

## Guardrails

- UGI remains an independent Python/local-service + PWA component.
- SOVEREIGN OS remains an independent Android/JGit application.
- Cross-runtime integration is through versioned contracts and standard Git interchange, not shared runtime or forced project merges.
- GitLab `sheed-group/one` is a read-only upstream source during mirroring; GitHub `metsam-mastem/1` is the integration destination.
- Public repository contents must exclude secrets, private research, and undocumented rights. UGI MIT license is retained; SOVEREIGN OS project license remains undeclared pending an explicit grant.
- Evidence labels: EVIDENCE, INFERENCE, HYPOTHESIS, OPEN_QUESTION, PLANNED, EXECUTED, VALIDATED. No status promotion without an artifact, command, and result.

## Workstream routing

| Workstream | Role in this project | Boundary / output |
|---|---|---|
| JARYAN REA Lab | Evidence-led review of observable workflows and public/open-source references | Capability observation records; no copying of proprietary implementation |
| JARYAN Reverse Engineering Lab | Convert validated observations into provider-neutral capability contracts and independent modules | Clean-room specification, interface, tests and implementation brief |
| Engineering Atlas Research | Maintain source registry, architecture map, recipes, claims, decisions and risks | Versioned evidence-backed engineering atlas |
| Product Design | Audit and improve the actual Android/PWA user flows | Requires Work-mode visual capture for a full visual audit; chat-only review is not a substitute |
| Data / Deepnote | Quantitative acceptance, coverage, reliability and resource metrics | Use a dedicated Workbench analysis project; do not repurpose unrelated PRISMA or other projects |
| Canonical Memory Verifier | Verify an explicitly selected canonical memory bundle before relying on it | BLOCKED until the exact bundle is selected; chat summaries are not a substitute |
| Skill Craft | Review skills/plugins when a skill approval or supply-chain audit is requested | Not a substitute for runtime or repository testing |
| Build MCP Apps / Plugin Creator | Build a separate MCP app only if a concrete Workbench capability needs an MCP interface | No plugin is created merely because the tools are available |
| Hugging Face | Research candidate local models, datasets and inference runtimes | Optional model research; connector search must succeed and model licenses/resources must be checked |
| Deepnote | Run reproducible quantitative analysis | Existing PRISMA and JARYAN fallback projects remain separate; create a dedicated project only when needed |
| Cambiante Content Manager | Prepare and govern public product updates after verified milestones | No publishing or scheduling without explicit approval |
| AnyHowTo Guide | User-facing procedural guidance when a workflow needs it | Not required for current code integration |
| Business Strategy Builder | Business model and go-to-market exploration | Separate product-strategy workflow; not a code/build gate |

## Verified baseline

- GitHub `main` contains SOVEREIGN OS source snapshot, tests, architecture/license notices, workflows, UGI README, and integration documentation.
- Android CI run `41373586476` passed: 36 Debug tests, 36 Release tests, and Debug APK assembly.
- Debug APK SHA-256: `ade43ee4f807c232e81224f45299437b9f228b13ab0f6212b7d1bef48353da59`.
- Latest observed repository tree contains `components/ugi/README.md` only. The complete UGI alpha.4 source package has not yet been published as browsable source in GitHub.
- UGI local archive `/mnt/data/UGI-1.1.0-alpha.4-published.zip` contains 29 files and has SHA-256 `bef88d5094e3a3982ff018ac06aa1990ee42b9fe2343ee689f0085004c44d4fd`.
- Deepnote workspace contains a separate `PRISMA — Local Engineering Validation` notebook. It has no integrations attached and is not the SOVEREIGN/UGI project; do not repurpose it.
- Hugging Face model-search invocation returned a connector error (`Tool model_search not found`); no model search result is claimed.

## Ordered release gates

1. Publish the complete UGI alpha.4 source (not just its README) to `components/ugi/`; verify archive and source checksums.
2. Inspect and document dependency provenance, licenses, and security posture for both runtimes.
3. Define versioned GitProvider contract fixtures and run the same behavioral suite against UGI local adapter and Android/JGit adapter.
4. Add reproducible CI checks for UGI unit tests, Python compile, JS syntax, Android Debug/Release tests, and package assembly.
5. Run product-flow review against actual captures and device behavior; do not infer visual or accessibility quality from unit tests.
6. Run physical-device acceptance tests for import, immutable source handling, local Git operations, bundle round-trip, offline behavior, storage permissions, and recovery.
7. Add resource budgets and measurements (startup, memory, CPU, storage, model latency/footprint) before choosing embedded model/runtime combinations.
8. Resolve SOVEREIGN OS project licensing and release-signing authority before producing a distributable release.

## Current blockers

- UGI source archive is present locally but not fully committed to GitHub.
- No cross-runtime provider contract test suite exists yet.
- No physical-device test evidence exists.
- SOVEREIGN OS project license grant is not declared.
- Hugging Face connector call was unavailable in this session.
- No dedicated Deepnote Workbench analytics project has been created.

## Next action

Complete UGI source publication and then add the shared GitProvider contract tests using test-first development. Do not merge upstream GitLab branches or publish public marketing content as part of this step.
