package org.sovereignos.git

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import org.sovereignos.core.IntakePolicy

class GitProviderAdapterContractTest {
    private lateinit var root: File
    private lateinit var workspace: GitWorkspace
    private lateinit var provider: JGitProviderAdapter

    @Before fun setUp() {
        root = Files.createTempDirectory("sovereign-provider-contract").toFile()
        workspace = GitWorkspace(root, "Contract Test", "contract@example.invalid")
        provider = JGitProviderAdapter(workspace)
    }

    @After fun tearDown() { root.deleteRecursively() }

    @Test fun exposesProviderIdentityAndContractVersion() {
        assertEquals("sovereign.android-jgit", provider.providerId)
        assertEquals("1.0", provider.contractVersion)
        assertEquals("ok", provider.health().status)
        assertEquals(provider.providerId, provider.health().providerId)
    }

    @Test fun createsListsAndReportsRepositoryStatus() {
        assertTrue(provider.repositories().isEmpty())
        val created = provider.createRepository("contract-repo")
        assertEquals("contract-repo", created.name)
        assertEquals("main", created.branch)
        assertEquals(0, created.changed)

        provider.writeFile("contract-repo", "README.md", "provider contract")
        val dirty = provider.status("contract-repo")
        assertEquals(listOf("README.md"), dirty.untracked)
        assertFalse(dirty.isClean)
    }

    @Test fun stagesCommitsAndReturnsNewestFirstHistory() {
        provider.createRepository("contract-repo")
        provider.writeFile("contract-repo", "README.md", "contract test")
        val staged = provider.stageAll("contract-repo")
        assertEquals(listOf("README.md"), staged.staged)

        val receipt = provider.commit("contract-repo", "Add provider fixture")
        assertEquals(40, receipt.sha.length)
        assertEquals("Add provider fixture", receipt.message)

        val history = provider.log("contract-repo", 5)
        assertEquals("Add provider fixture", history.first().message)
        assertEquals(receipt.sha, history.first().id)
        assertTrue(provider.status("contract-repo").isClean)
    }

    @Test fun exportsAndImportsBundleWithoutLosingHistoryOrContent() {
        provider.createRepository("bundle-source")
        provider.writeFile("bundle-source", "notes/portable.txt", "bundle contract")
        provider.stageAll("bundle-source")
        val sourceCommit = provider.commit("bundle-source", "Bundle baseline")

        val bundle = File(root, "source.bundle")
        val exported = provider.exportBundle("bundle-source", bundle)
        assertTrue(exported.isFile)
        assertTrue(exported.length() > 0)

        val imported = provider.importBundle(bundle, "bundle-copy")
        assertEquals("bundle-copy", imported.name)
        assertEquals("bundle contract", File(root, "bundle-copy/notes/portable.txt").readText())
        assertEquals(sourceCommit.sha, provider.log("bundle-copy", 1).single().id)
    }

    @Test fun rejectsInvalidBundleAndRemovesPartialDestination() {
        val invalid = File(root, "invalid.bundle").apply { writeText("not a Git Bundle") }
        try {
            provider.importBundle(invalid, "invalid-copy")
            fail("Invalid Bundle must be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected contract rejection.
        }
        assertFalse(File(root, "invalid-copy").exists())
    }

    @Test fun neverOverwritesAnExistingRepositoryDuringBundleImport() {
        provider.createRepository("protected-target")
        val invalid = File(root, "conflicting.bundle").apply { writeText("# v2 git bundle\ninvalid") }
        try {
            provider.importBundle(invalid, "protected-target")
            fail("Existing destination must be protected")
        } catch (_: IllegalArgumentException) {
            // Expected non-destructive import guard.
        }
        assertTrue(File(root, "protected-target/.git").isDirectory)
    }

    @Test fun rejectsBundleLargerThanTheSharedImportLimitBeforeImport() {
        val oversized = File(root, "oversized.bundle")
        RandomAccessFile(oversized, "rw").use { it.setLength(IntakePolicy.MAX_IMPORT_BYTES + 1) }
        try {
            provider.importBundle(oversized, "oversized-copy")
            fail("Oversized Bundle must be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected size-limit rejection.
        }
        assertFalse(File(root, "oversized-copy").exists())
    }
}
