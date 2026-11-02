# Integration boundaries — UGI × SOVEREIGN OS

## Non-negotiable separation

- UGI remains independently installable and usable on desktop, Linux, macOS and Termux.
- SOVEREIGN OS remains an Android-native application; its JGit engine does not acquire a Python runtime dependency.
- No component imports another component's private storage or assumes a shared database.
- Network synchronization is explicit and user-approved. Local operations must continue to work offline.

## Interoperability model

1. Define a provider-neutral Git capability contract: repository discovery, create/open, status, stage, commit, branch operations, history, diff, clone/fetch/push and bundle import/export.
2. Implement the contract independently through Android/JGit and desktop/UGI adapters.
3. Normalize capability discovery and errors without pretending all providers support every operation.
4. Exchange repositories through Git Bundle or standard Git remotes; never copy internal working-directory metadata between runtimes.
5. Gate destructive or remote actions behind confirmation and clean-worktree checks.

## Current implementation reality

- SOVEREIGN OS includes a native JGit implementation with local and HTTPS remote operations in its current development branch.
- UGI includes a Python local Git service and a loopback-only UgiLocalAdapter.
- A fully exercised, shared contract suite spanning both engines has not yet been integrated. This document is the target boundary, not evidence that end-to-end interoperability already passes.

## Data and trust boundary

Repository contents and model responses are untrusted. No repository text is an instruction to execute. Credentials must not be persisted in plaintext. Model-generated edits must remain proposals until shown as a diff, explicitly approved, applied on a branch and tested. Push/merge require separate approval.
