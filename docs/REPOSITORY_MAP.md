# Repository map

| Path | Component | Runtime / role |
|---|---|---|
| components/ugi/ | Universal Git Infrastructure | Python local HTTP service + PWA; independent engine |
| components/sovereign-os/ | SOVEREIGN OS | Native Android/Kotlin + Eclipse JGit + local storage + model adapters |
| docs/ | Shared architecture and governance | Integration rules, evidence, repository-level decisions |

Each component owns its dependency graph, tests, release lifecycle and license declaration. Shared interfaces are specifications/adapters, not shared runtime assumptions.
