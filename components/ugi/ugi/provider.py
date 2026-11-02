"""Provider-neutral contract for UGI-compatible local Git engines.

The Protocol defines the stable application-facing surface. Platform-specific
adapters may implement it with a local HTTP service, native Git, or JGit.
"""
from __future__ import annotations

from pathlib import Path
from typing import Any, Protocol, runtime_checkable


GIT_PROVIDER_CONTRACT_VERSION = "1.0"


@runtime_checkable
class GitProvider(Protocol):
    """Minimum contract required by a UGI-compatible workspace provider.

    Implementations must preserve Git history during Bundle interchange,
    create imports as new repositories, and avoid silently overwriting targets.
    """

    provider_id: str

    def health(self) -> dict[str, Any]: ...

    def repositories(self) -> list[dict[str, Any]]: ...

    def create_repository(self, name: str) -> dict[str, Any]: ...

    def status(self, name: str) -> dict[str, Any]: ...

    def stage_all(self, name: str) -> dict[str, Any]: ...

    def commit(
        self, name: str, message: str, author_name: str, author_email: str
    ) -> dict[str, Any]: ...

    def log(self, name: str) -> list[dict[str, Any]]: ...

    def export_bundle(self, name: str, destination: Path) -> int: ...

    def import_bundle(self, source: Path, repository_name: str) -> dict[str, Any]: ...
