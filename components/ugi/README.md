# Universal Git Infrastructure (UGI)

Independent local-first Git workbench. Python local service + Persian RTL PWA; does not require the SOVEREIGN OS Android app.

## Current source snapshot

- Local source package: **UGI 1.1.0-alpha.5**.
- Source license: MIT; retain the package LICENSE file.
- Python provider contract: `ugi.provider.GitProvider`, contract version `1.0`.
- Local implementation: `UgiLocalAdapter`.
- Verification on 2026-11-02: **32 tests + 12 subtests PASS**, Python compilation PASS, JavaScript syntax PASS.
- New remotely committed files: provider Protocol, provider conformance tests, and provider contract document.

## Integration boundary

UGI remains an independent Python local-service + PWA component. SOVEREIGN OS remains an independent Android/JGit application. Cross-runtime transfer is based on standard Git Bundle files; neither runtime is embedded into the other.

The inspected Android `GitWorkspace` has corresponding JGit primitives, but a `JGitProviderAdapter` implementing the complete v1.0 contract and matching Android conformance tests remain **NOT RUN / NOT IMPLEMENTED**.

## Publication status

The complete alpha.5 source archive is built and verified locally. The full archive has **not** yet been committed to this public repository; only the contract source/test delta and integration documentation are present here. Remote publication must not be marked complete until the full source tree or release archive is uploaded and fetched back for checksum verification.

See [GitProvider Contract v1.0](docs/GIT_PROVIDER_CONTRACT_V1.md).