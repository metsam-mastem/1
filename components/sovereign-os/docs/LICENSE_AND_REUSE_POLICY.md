# License and source reuse policy

This document is an engineering policy, not legal advice. Re-check exact license files at the exact revision before copying, modifying, linking or redistributing any third-party code.

## Initial dependency posture

| Component | License / posture | Commercial use direction |
|---|---|---|
| Eclipse JGit | Eclipse Distribution License 1.0 (permissive, BSD-style) | Suitable candidate for proprietary distribution with required notices and full dependency review |
| LiteRT-LM | Apache License 2.0 | Suitable candidate with license and NOTICE obligations; check model weights separately |
| GitSync | GPL-3.0-only | Study architecture and behavior; do not copy code into a closed-source app unless choosing full GPL compliance or obtaining a separate commercial license |
| GitNomad app/extension | No reusable public source/license located in this research | Product/UX reference only; do not copy code or proprietary assets |
| GitHub Mobile | Proprietary first-party application | Official documentation and public APIs only; no app code reuse |
| GitLab server | Open-source project under its own license; not a mobile Git library | Prefer documented APIs; server code reuse requires a separate architecture and license review |
| MGit / Gitling | GPL-family licenses | Study feature behavior; avoid embedding source in a proprietary client unless obligations are intentionally accepted |
| Model weights | Per-model license | Never infer the model license from the runtime license |

## GPL decision

GitSync's upstream LICENSE.md identifies GNU GPL version 3. The F-Droid listing identifies GPL-3.0-only. A commercial entity can sell GPL software, but distributing a derivative/combined work requires GPL obligations, including providing corresponding source under the applicable terms. This can conflict with a proprietary closed-source SOVEREIGN Pro client.

Selected default: independent implementation using permissively licensed libraries, documented protocols and clean-room feature specifications. No GitSync source files, snippets, UI assets or copied implementation logic are imported.

## Required release artifacts

- Complete SPDX SBOM for direct and transitive dependencies.
- License and NOTICE bundle for every redistributed dependency.
- Source offer / corresponding source workflow if any copyleft component is included.
- Model and tokenizer license manifest for bundled models (prefer no bundled model weights).
- Attribution for icons, fonts, UI assets and documentation.
- Software supply-chain provenance and checksums for release binaries.
- Legal review of app name, logo, package identifier and marketing claims.

## Clean-room evidence

For every reference project:
1. Record upstream URL, revision, date and license.
2. Record product behavior separately from implementation details.
3. Write an independent requirement and test before implementation.
4. Avoid copying nontrivial code, assets, naming, UI composition or documentation text.
5. Record dependency and attribution decisions in this file.
