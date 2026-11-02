package org.sovereignos.core

import java.io.File

object IntakePolicy {
    const val MAX_IMPORT_BYTES = 256L * 1024 * 1024

    fun checkedImportSize(currentSize: Long, incomingBytes: Int, maxBytes: Long = MAX_IMPORT_BYTES): Long {
        require(currentSize >= 0 && incomingBytes >= 0 && maxBytes >= 0) { "Import size values cannot be negative." }
        require(currentSize <= maxBytes - incomingBytes) {
            "Selected file exceeds the ${maxBytes / (1024 * 1024)} MB import limit."
        }
        return currentSize + incomingBytes
    }

    fun validateImportSize(size: Long, maxBytes: Long = MAX_IMPORT_BYTES) {
        require(size in 0..maxBytes) {
            "Selected file exceeds the ${maxBytes / (1024 * 1024)} MB import limit."
        }
    }

    fun isTextMime(mimeType: String?): Boolean =
        mimeType?.lowercase()?.let { it.startsWith("text/") || it == "application/json" || it == "application/xml" } ?: false

    fun normalizeText(value: String): String =
        value.replace("\r\n", "\n").replace('\r', '\n').trim()

    fun hasUsefulText(value: String): Boolean = normalizeText(value).isNotEmpty()

    fun safeDisplayName(value: String?): String {
        val raw = value?.substringAfterLast('/')?.substringAfterLast('\\')?.trim().orEmpty()
        val cleaned = raw.replace(Regex("[^A-Za-z0-9._-]+"), "-")
            .trim('-', '.', '_')
            .take(120)
        return cleaned.ifBlank { "imported-file" }
    }
}
