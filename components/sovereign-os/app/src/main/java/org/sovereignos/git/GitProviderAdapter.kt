package org.sovereignos.git

import org.sovereignos.core.IntakePolicy
import java.io.File

/** Stable response types for the SOVEREIGN Android implementation of GitProvider v1.0. */
data class GitProviderHealth(
    val providerId: String,
    val contractVersion: String,
    val status: String
)

data class GitProviderRepository(
    val name: String,
    val branch: String,
    val changed: Int
)

data class GitProviderCommitReceipt(
    val sha: String,
    val message: String
)

/**
 * Provider-neutral facade over the Android JGit workspace.
 *
 * This is intentionally an adapter, not a shared runtime: the Android app keeps
 * using JGit directly and exchanges repositories with UGI through Git Bundles.
 */
class JGitProviderAdapter(
    private val workspace: GitWorkspace
) {
    val providerId: String = "sovereign.android-jgit"
    val contractVersion: String = "1.0"

    fun health(): GitProviderHealth = GitProviderHealth(
        providerId = providerId,
        contractVersion = contractVersion,
        status = "ok"
    )

    fun repositories(): List<GitProviderRepository> =
        workspace.repositoryNames().map { repositorySummary(it) }

    fun createRepository(name: String): GitProviderRepository {
        workspace.createRepository(name)
        return repositorySummary(name)
    }

    fun status(name: String): GitStatusSnapshot = workspace.status(name)

    fun writeFile(name: String, relativePath: String, content: String) =
        workspace.writeFile(name, relativePath, content)

    fun stageAll(name: String): GitStatusSnapshot {
        workspace.stageAll(name)
        return workspace.status(name)
    }

    fun commit(name: String, message: String): GitProviderCommitReceipt {
        val normalized = message.trim()
        require(normalized.isNotEmpty()) { "Commit message is required" }
        val sha = workspace.commit(name, normalized)
        return GitProviderCommitReceipt(sha = sha, message = normalized)
    }

    fun log(name: String, limit: Int = 20): List<GitCommitInfo> =
        workspace.recentCommits(name, limit)

    fun exportBundle(name: String, destination: File): File {
        require(destination.parentFile?.exists() != false) {
            "Bundle destination directory does not exist"
        }
        workspace.exportBundle(name, destination)
        check(destination.isFile && destination.length() > 0L) {
            "Git Bundle export did not produce a non-empty file"
        }
        return destination
    }

    /**
     * Imports only a valid, bounded Git Bundle into a new repository.
     * Existing targets are rejected by GitWorkspace before they can be changed.
     */
    fun importBundle(source: File, repositoryName: String): GitProviderRepository {
        validateBundle(source)
        workspace.importBundle(repositoryName, source)
        return repositorySummary(repositoryName)
    }

    private fun repositorySummary(name: String): GitProviderRepository {
        val snapshot = workspace.status(name)
        val changedPaths = (
            snapshot.staged + snapshot.modified + snapshot.untracked + snapshot.missing
        ).distinct()
        return GitProviderRepository(
            name = name,
            branch = snapshot.branch,
            changed = changedPaths.size
        )
    }

    private fun validateBundle(source: File) {
        require(source.isFile && source.length() > 0L) {
            "Git Bundle is empty or missing"
        }
        IntakePolicy.validateImportSize(source.length())

        val prefix = ByteArray(16)
        val count = source.inputStream().buffered().use { input -> input.read(prefix) }
        val header = if (count > 0) {
            String(prefix, 0, count, Charsets.US_ASCII)
        } else {
            ""
        }
        require(header.startsWith("# v2 git bundle\n") || header.startsWith("# v3 git bundle\n")) {
            "Selected file is not a supported Git Bundle (v2 or v3)"
        }
    }
}
