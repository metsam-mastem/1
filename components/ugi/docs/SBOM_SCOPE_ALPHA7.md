# UGI alpha.7 — CycloneDX source-release SBOM scope

- Release: UGI 1.1.0-alpha.7
- Format: CycloneDX JSON 1.7 (ECMA-424, 2nd edition)
- Companion file: UGI-1.1.0-alpha.7.cdx.json

## Included inventory

The companion BOM identifies the UGI source archive by SHA-256, records its declared MIT license, and lists the two mandatory external host prerequisites:

- CPython >=3.11.0 (interpreter not bundled).
- Git CLI >=2.28.0 (executable not bundled).

The Python runtime dependency list is empty. No third-party Python packages are required by the UGI application source. The HTML/CSS/JavaScript UI has no CDN-loaded runtime framework. Node.js is used in QA only and is not a product runtime prerequisite.

## Explicit completeness boundary

The BOM marks its composition as incomplete. It is a source-release BOM plus declared host prerequisites, not a complete deployment or operations inventory. It does not enumerate OS distribution packages, libc, shell, TLS/OpenSSL provider, Git transitive packages, distribution-specific CPython notices, Android framework/device hardware, CI runner images, known vulnerabilities, or VEX.

SOVEREIGN OS Android requires a separate CycloneDX BOM generated from the resolved Gradle dependency graph. Never reuse this UGI BOM for Android.

## License representation

- UGI source: MIT, as declared in LICENSE.
- CPython core: PSF-2.0 reference; distributor-specific bundled notices are not exhaustively inventoried.
- Git CLI: GPL-2.0-only as the main project license; individual files may carry other compatible licenses.

These entries identify listed components; they do not grant or change license rights.

## Validation status

The BOM is generated for the exact ZIP hash, and its JSON syntax, required identity fields, dependency references, archive checksum linkage, and incomplete composition marker are checked locally. The official CycloneDX 1.7 JSON Schema validator was not run. Do not describe this BOM as schema-validated, complete for deployment, or vulnerability-assessed.
