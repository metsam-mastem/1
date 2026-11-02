package org.sovereignos.core
import org.junit.Assert.*
import org.junit.Test
class IntakePolicyTest {
    @Test fun recognizesPlainTextMimeTypes() {
        assertTrue(IntakePolicy.isTextMime("text/plain"))
        assertTrue(IntakePolicy.isTextMime("text/markdown"))
        assertFalse(IntakePolicy.isTextMime("image/jpeg"))
    }
    @Test fun normalizesLineEndingsAndTrimsOuterWhitespace() {
        assertEquals("alpha\nbeta", IntakePolicy.normalizeText("  alpha\r\nbeta  "))
    }
    @Test fun rejectsBlankTextAfterNormalization() {
        assertFalse(IntakePolicy.hasUsefulText(" \r\n  "))
        assertTrue(IntakePolicy.hasUsefulText("  meaningful  "))
    }
    @Test fun sanitizesDisplayNamesWithoutLosingExtension() {
        assertEquals("my-notes.txt", IntakePolicy.safeDisplayName("../my notes.txt"))
    }
    @Test fun enforcesTheImportSizeLimitAtTheBoundary() {
        assertEquals(16L, IntakePolicy.checkedImportSize(10L, 6, 16L))
        IntakePolicy.validateImportSize(16L, 16L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnImportThatWouldExceedTheConfiguredLimit() {
        IntakePolicy.checkedImportSize(10L, 7, 16L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnAlreadyOversizedImport() {
        IntakePolicy.validateImportSize(17L, 16L)
    }
}

