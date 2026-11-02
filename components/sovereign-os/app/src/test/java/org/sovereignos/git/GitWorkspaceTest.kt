package org.sovereignos.git

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.file.Files

class GitWorkspaceTest {
    private lateinit var root: java.io.File
    private lateinit var workspace: GitWorkspace

    @Before fun setUp() {
        root = Files.createTempDirectory("sovereign-git-test").toFile()
        workspace = GitWorkspace(root, "Test User", "test@example.invalid")
    }

    @After fun tearDown() { root.deleteRecursively() }

    @Test fun initializesAnOfflineRepositoryWithMainBranch() {
        workspace.createRepository("sample")
        assertEquals("main", workspace.status("sample").branch)
        assertTrue(workspace.listBranches("sample").isEmpty())
        assertTrue(workspace.status("sample").isClean)
    }

    @Test fun diffIsSafeBeforeTheFirstCommit() {
        workspace.createRepository("sample")
        val diff = workspace.diff("sample")
        assertTrue(diff.contains("No committed HEAD yet"))
    }

    @Test fun tracksStagesAndCommitsAFile() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "notes/idea.md", "# Local first")
        assertEquals(listOf("notes/idea.md"), workspace.status("sample").untracked)
        workspace.stageAll("sample")
        assertEquals(listOf("notes/idea.md"), workspace.status("sample").staged)
        val commit = workspace.commit("sample", "Add first note")
        assertTrue(commit.isNotBlank())
        assertTrue(workspace.status("sample").isClean)
        assertEquals("Add first note", workspace.recentCommits("sample", 1).single().message)
    }

    @Test fun createsAndChecksOutBranchesWithoutNetwork() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "README.md", "baseline")
        workspace.stageAll("sample")
        workspace.commit("sample", "Initial commit")
        workspace.createBranch("sample", "feature/local-git")
        workspace.checkoutBranch("sample", "feature/local-git")
        assertEquals("feature/local-git", workspace.status("sample").branch)
        assertTrue(workspace.listBranches("sample").contains("main"))
    }

    @Test fun importsBinaryDocumentsWithoutChangingTheirBytes() {
        workspace.createRepository("sample")
        val bytes = byteArrayOf(0, 1, 2, 10, 13, -1)
        workspace.importFile("sample", "assets/payload.bin", ByteArrayInputStream(bytes))
        assertArrayEquals(bytes, java.io.File(root, "sample/assets/payload.bin").readBytes())
        val untracked = workspace.status("sample").untracked
        assertTrue(untracked.any { it == "assets/" || it == "assets/payload.bin" })
    }

    @Test fun modelContextContainsUsefulFilesButExcludesSecretLikeFiles() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "README.md", "safe project notes")
        workspace.writeFile("sample", ".env", "API_TOKEN=should-not-leak")
        val context = workspace.modelContext("sample")
        assertTrue(context.contains("safe project notes"))
        assertFalse(context.contains("should-not-leak"))
    }

    @Test fun modelContextRedactsSecretAssignmentsInOtherwiseSafeFiles() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "README.md", "API_KEY=do-not-send\npublic project docs")
        val context = workspace.modelContext("sample")
        assertFalse(context.contains("do-not-send"))
        assertTrue(context.contains("public project docs"))
    }

    @Test fun modelContextRedactsJsonSecretAssignments() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "config.json", """{"api_key":"should-not-appear","mode":"offline"}""")
        val context = workspace.modelContext("sample")
        assertFalse(context.contains("should-not-appear"))
    }

    @Test fun modelContextExcludesChangesFromSecretNamedFiles() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", ".env", "PRIVATE_VALUE_ONE")
        workspace.stageAll("sample")
        workspace.commit("sample", "Add local environment file")
        workspace.writeFile("sample", ".env", "PRIVATE_VALUE_TWO")
        val context = workspace.modelContext("sample")
        assertFalse(context.contains("PRIVATE_VALUE_ONE"))
        assertFalse(context.contains("PRIVATE_VALUE_TWO"))
    }

    @Test fun exportsAndImportsRepositoryAsAnOfflineBundle() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "README.md", "portable offline history")
        workspace.stageAll("sample")
        workspace.commit("sample", "Portable baseline")
        val bundle = java.io.File(root, "sample.bundle")
        workspace.exportBundle("sample", bundle)
        val importedRoot = java.io.File(root, "imported")
        val imported = GitWorkspace(importedRoot, "Receiver", "receiver@example.invalid")
        imported.importBundle("copy", bundle)
        assertEquals("portable offline history", java.io.File(importedRoot, "copy/README.md").readText())
        assertEquals("Portable baseline", imported.recentCommits("copy", 1).single().message)
        assertTrue(imported.listBranches("copy").contains("main"))
        assertTrue("An offline Bundle import must not retain its temporary file as origin",
            imported.listRemotes("copy").isEmpty())
    }

    @Test fun managesNamedHttpsRemotesWithoutEmbeddingCredentials() {
        workspace.createRepository("sample")
        workspace.addRemote("sample", "origin", "https://git.example.test/team/project.git")
        assertEquals("https://git.example.test/team/project.git", workspace.listRemotes("sample").single().url)
        workspace.removeRemote("sample", "origin")
        assertTrue(workspace.listRemotes("sample").isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRemoteUrlsContainingEmbeddedCredentials() {
        workspace.createRepository("sample")
        workspace.addRemote("sample", "origin", "https://user:secret@git.example.test/team/project.git")
    }

    @Test fun pushesLocalCommitToAFileRemoteAndFetchesItBackOffline() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "README.md", "offline remote transport")
        workspace.stageAll("sample")
        workspace.commit("sample", "Create portable baseline")

        val bareRemote = java.io.File(root, "remote.git")
        org.eclipse.jgit.api.Git.init().setBare(true).setDirectory(bareRemote).call().use { }
        workspace.addRemote("sample", "origin", bareRemote.toURI().toString())

        workspace.push("sample", "origin")
        val remoteHead = org.eclipse.jgit.api.Git.open(bareRemote).use {
            it.repository.resolve("refs/heads/main")
        }
        assertNotNull(remoteHead)

        workspace.fetch("sample", "origin")
        assertTrue(workspace.listRemoteBranches("sample").contains("origin/main"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInsecureHttpRemoteUrls() {
        workspace.createRepository("sample")
        workspace.addRemote("sample", "origin", "http://git.example.test/team/project.git")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFileRemotesOutsideThePrivateWorkspace() {
        workspace.createRepository("sample")
        val outside = Files.createTempDirectory("outside-git").toFile()
        try {
            workspace.addRemote("sample", "origin", outside.toURI().toString())
        } finally {
            outside.deleteRecursively()
        }
    }

    @Test fun clearsEphemeralAccessTokenWhenRemoteAuthenticationCannotStart() {
        workspace.createRepository("sample")
        val bareRemote = java.io.File(root, "remote.git")
        org.eclipse.jgit.api.Git.init().setBare(true).setDirectory(bareRemote).call().use { }
        workspace.addRemote("sample", "origin", bareRemote.toURI().toString())
        val token = "temporary-secret".toCharArray()
        runCatching { workspace.fetch("sample", "origin", "", token) }
        assertArrayEquals(CharArray(token.size) { '\u0000' }, token)
    }

    @Test fun clonesAnOfflineBareRemoteIntoANewRepository() {
        workspace.createRepository("source")
        workspace.writeFile("source", "README.md", "clone this repository")
        workspace.stageAll("source")
        workspace.commit("source", "Source baseline")

        val bareRemote = java.io.File(root, "clone-origin.git")
        org.eclipse.jgit.api.Git.init().setBare(true).setDirectory(bareRemote).call().use { }
        workspace.addRemote("source", "origin", bareRemote.toURI().toString())
        workspace.push("source", "origin")
        // A bare repository HEAD must point to the branch that was pushed.
        org.eclipse.jgit.api.Git.open(bareRemote).use { remoteGit ->
            remoteGit.repository.updateRef(org.eclipse.jgit.lib.Constants.HEAD).link("refs/heads/main")
        }

        workspace.cloneRepository("cloned", bareRemote.toURI().toString())
        assertEquals("clone this repository", java.io.File(root, "cloned/README.md").readText())
        assertEquals("origin", workspace.listRemotes("cloned").single().name)
        assertEquals("Source baseline", workspace.recentCommits("cloned", 1).single().message)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPathTraversalWhenWritingRepositoryFiles() {
        workspace.createRepository("sample")
        workspace.writeFile("sample", "../outside.txt", "must not escape repository")
    }
    @Test fun clearsFetchTokenWhenRemotePreflightFailsBeforeAuthentication() {
        workspace.createRepository("sample")
        val token = "temporary-secret".toCharArray()
        runCatching { workspace.fetch("sample", "missing", "user", token) }
        assertTrue(token.all { it == '\u0000' })
    }

    @Test fun clearsCloneTokenWhenUrlValidationFailsBeforeAuthentication() {
        val token = "temporary-secret".toCharArray()
        runCatching { workspace.cloneRepository("sample", "http://git.example.test/repo.git", "user", token) }
        assertTrue(token.all { it == '\u0000' })
        assertFalse(java.io.File(root, "sample").exists())
    }

    @Test fun checksOutFetchedRemoteBranchAndConfiguresItsUpstream() {
        workspace.createRepository("source")
        workspace.writeFile("source", "README.md", "base")
        workspace.stageAll("source")
        workspace.commit("source", "Initial main")

        val bareRemote = java.io.File(root, "tracking-origin.git")
        org.eclipse.jgit.api.Git.init().setBare(true).setDirectory(bareRemote).call().use { }
        workspace.addRemote("source", "origin", bareRemote.toURI().toString())
        workspace.push("source", "origin")

        workspace.createBranch("source", "feature/remote-topic")
        workspace.checkoutBranch("source", "feature/remote-topic")
        workspace.writeFile("source", "topic.txt", "remote branch content")
        workspace.stageAll("source")
        workspace.commit("source", "Add remote topic")
        workspace.push("source", "origin")

        workspace.createRepository("consumer")
        workspace.addRemote("consumer", "origin", bareRemote.toURI().toString())
        workspace.fetch("consumer", "origin")

        assertEquals(
            "feature/remote-topic",
            workspace.checkoutRemoteBranch("consumer", "origin/feature/remote-topic")
        )
        assertEquals("feature/remote-topic", workspace.status("consumer").branch)
        org.eclipse.jgit.api.Git.open(java.io.File(root, "consumer")).use { git ->
            assertEquals("origin", git.repository.config.getString("branch", "feature/remote-topic", "remote"))
            assertEquals("refs/heads/feature/remote-topic", git.repository.config.getString("branch", "feature/remote-topic", "merge"))
        }
        assertEquals("remote branch content", java.io.File(root, "consumer/topic.txt").readText())
    }

    @Test(expected = IllegalArgumentException::class)
    fun refusesRemoteBranchCheckoutWhenWorkingTreeIsDirty() {
        workspace.createRepository("source")
        workspace.writeFile("source", "README.md", "base")
        workspace.stageAll("source")
        workspace.commit("source", "Initial main")
        val bareRemote = java.io.File(root, "dirty-origin.git")
        org.eclipse.jgit.api.Git.init().setBare(true).setDirectory(bareRemote).call().use { }
        workspace.addRemote("source", "origin", bareRemote.toURI().toString())
        workspace.push("source", "origin")

        workspace.createRepository("consumer")
        workspace.addRemote("consumer", "origin", bareRemote.toURI().toString())
        workspace.fetch("consumer", "origin")
        workspace.writeFile("consumer", "uncommitted.txt", "keep my work")
        workspace.checkoutRemoteBranch("consumer", "origin/main")
    }

}

