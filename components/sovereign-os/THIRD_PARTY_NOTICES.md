# Third-party components and model notices

SOVEREIGN OS currently references these bundled runtime libraries:

- **Eclipse JGit 7.8.0.202609011348-r** — pure-Java Git implementation. Project license: Eclipse Distribution License 1.0, SPDX BSD-3-Clause. The JGit binary redistribution must retain the required notices. Source and license: https://github.com/eclipse-jgit/jgit and https://www.eclipse.org/org/documents/edl-v10/
- **Google AI Edge LiteRT-LM 0.18.1** — on-device inference runtime. Upstream project license: Apache License 2.0. Source and license: https://github.com/google-ai-edge/LiteRT-LM

This is an attribution index, not a complete release SBOM. Before any APK/AAB distribution, collect the licenses and NOTICE files of all resolved transitive dependencies, include a machine-generated dependency inventory, and run a license/security scan.

Model weights are separate from the runtime library. Each imported .litertlm model may have its own licence, attribution, acceptable-use limits and redistribution rules. SOVEREIGN does not bundle model weights in the APK; the user imports the model file and remains responsible for checking its licence.


## Research-only references (not dependencies)

- GitSync (GPL-3.0-only): architecture and product behavior studied; no source copied or linked.
- GitNomad: product/UX study only; no complete public source/license located, no code copied.
- GitHub Mobile: official documentation only; no app source copied.
- GitLab server: official product/API documentation only; no server source copied.
- MGit/Gitling: feature study only; no source copied pending exact license and commercial compatibility review.

See docs/COMPETITIVE_RESEARCH_2026-10-30.md and docs/LICENSE_AND_REUSE_POLICY.md.
