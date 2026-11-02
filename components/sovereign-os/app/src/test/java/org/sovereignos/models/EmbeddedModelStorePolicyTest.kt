package org.sovereignos.models

import org.junit.Assert.*
import org.junit.Test

class EmbeddedModelStorePolicyTest {
    @Test fun modelFilesAreExpectedToUseLiteRtLmFormat() {
        assertTrue("Gemma3-1B.litertlm".endsWith(".litertlm", true))
        assertFalse("model.gguf".endsWith(".litertlm", true))
    }
}
