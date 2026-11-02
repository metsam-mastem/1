# Universal Git Infrastructure (UGI)

Independent local-first Git workbench. Python local service + Persian RTL PWA; does not require the SOVEREIGN OS Android app.

## Current source snapshot

- Local source package: **UGI 1.1.0-alpha.6**.
- Source license: MIT; the archive retains LICENSE.
- Python provider contract: ugi.provider.GitProvider, contract version 1.0.
- Local implementation: UgiLocalAdapter.
- Verification from a clean ZIP extraction on 2026-11-02: **49 tests PASS**, Python compileall PASS, JavaScript syntax checks PASS, CLI version PASS.
- Package is reproducible: two builds from the same source produced the same SHA-256.
- ZIP SHA-256: 9b975f864edb62819e09c450a40761ad6523f96313a951ffdb4fc4e21a17eb2d.
- Companion CycloneDX 1.7 source-release BOM published at releases/UGI-1.1.0-alpha.6.cdx.json.
- SBOM SHA-256: 79ea000a7c8c5c217931644681ee442b02b0aa964ce99a1e0a1e49e42f0b8563.
- SBOM license nodes include SPDX references; completeness is explicitly marked incomplete. The official CycloneDX JSON Schema validator was not run.

## Integration boundary

UGI remains an independent Python local-service + PWA component. SOVEREIGN OS remains an independent Android/JGit application. Cross-runtime transfer is based on standard Git Bundle files; neither runtime is embedded into the other.

Android has a separate JGitProviderAdapter facade over GitWorkspace. Provider conformance tests passed in CI. Physical-device acceptance remains NOT RUN.

## Publication status

The complete alpha.6 ZIP has been built, checksum-verified, and delivered as a local file attachment. GitHub contains the companion SBOM, updated SBOM checksum, SBOM scope, provider contract, and UGI integration documentation, but the full ZIP archive has **not** yet been uploaded to this public repository. Remote source-package publication remains open until the ZIP can be uploaded and fetched back for checksum comparison.

See:
- [GitProvider Contract v1.0](docs/GIT_PROVIDER_CONTRACT_V1.md)
- [SBOM scope and limitations](docs/SBOM_SCOPE.md)
- [Open-source ecosystem and standards audit](../../docs/OPEN_SOURCE_AND_STANDARDS_AUDIT_2026-11-02.md)
