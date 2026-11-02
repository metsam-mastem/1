# Software Bill of Materials — scope and limitations

Release: UGI 1.1.0-alpha.6  
Format: CycloneDX JSON 1.7 (ECMA-424, 2nd edition)  
Companion file: UGI-1.1.0-alpha.6.cdx.json

## What this BOM covers

The companion BOM identifies the UGI source release archive by SHA-256, records the declared MIT license, and lists the two external runtime prerequisites declared by the source package:

- CPython >=3.11.0 (external host platform; no CPython binary is bundled).
- Git CLI >=2.28.0 (external executable; no Git binary is bundled).

pyproject.toml declares an empty Python dependency list. The package therefore has no declared third-party Python package dependencies. Node.js is used by QA only for JavaScript syntax checks; it is not required to run the product and is excluded from the runtime dependency graph.

## Explicit completeness boundary

The BOM uses CycloneDX compositions.aggregate = incomplete. It is a source-release BOM plus declared host prerequisites, not a complete operating-system or deployment BOM. It does not enumerate:

- distribution-specific CPython bundled standard-library modules and their per-file notices;
- Git binary package dependencies, TLS/OpenSSL provider, libc, shell, or OS packages;
- device firmware, kernel, Android framework, or hardware;
- test runner operating-system images and GitHub Actions runner packages;
- known vulnerabilities or a VEX assessment.

A deployment-specific BOM must be generated from the actual installed Python distribution, Git package, OS image, and any packaged runtime dependencies. Android SOVEREIGN OS requires a separate CycloneDX BOM generated from the resolved Gradle graph; the UGI BOM must not be reused for Android.

## License representation

- UGI source: MIT (declared by the included LICENSE file).
- CPython core runtime: PSF-2.0 is recorded for the interpreter; distribution-specific bundled notices are not fully enumerated in this source BOM.
- Git CLI: GPL-2.0-only is recorded as the main project license. Git notes that some individual files may use other compatible licenses.

These entries describe the listed components; they do not grant or change any license rights.

## Reproduction

From the extracted source directory:

    python scripts/package_release.py --output ../UGI-1.1.0-alpha.6.zip
    python scripts/generate_sbom.py ../UGI-1.1.0-alpha.6.zip --output ../UGI-1.1.0-alpha.6.cdx.json

The generated BOM includes the SHA-256 of the exact ZIP passed to the generator. Regenerate it after rebuilding the ZIP. The JSON generator is deterministic for the same archive and SOURCE_DATE_EPOCH; set that variable when a different stable timestamp is required.

## Validation status

Local checks validate JSON syntax, CycloneDX identity/version fields, component references, declared runtime ranges, the incomplete composition marker, deterministic serial number, and archive SHA-256 linkage. The official CycloneDX JSON Schema validator was not run in this environment; do not label this BOM schema-validated or a complete supply-chain inventory.
