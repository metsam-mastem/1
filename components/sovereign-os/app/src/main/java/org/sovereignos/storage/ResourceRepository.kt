package org.sovereignos.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import org.sovereignos.core.IntakePolicy
import java.io.File
import java.security.MessageDigest
import java.util.Properties
import java.util.UUID

data class ResourceRecord(
    val id: String, val name: String, val mimeType: String, val size: Long,
    val sha256: String, val extractedText: String?, val duplicateOf: String?
)

class ResourceRepository(private val root: File, private val resolver: ContentResolver) {
    private val resourceRoot = File(root, "resources").apply { mkdirs() }

    fun list(): List<ResourceRecord> = resourceRoot.listFiles()
        ?.filter { it.isDirectory }
        ?.mapNotNull { dir ->
            runCatching {
                val p = Properties().apply { File(dir, "meta.properties").inputStream().use(::load) }
                ResourceRecord(
                    id = p.getProperty("id"), name = p.getProperty("name"),
                    mimeType = p.getProperty("mimeType"), size = p.getProperty("size").toLong(),
                    sha256 = p.getProperty("sha256"),
                    extractedText = File(dir, "extracted.txt").takeIf(File::exists)?.readText(),
                    duplicateOf = p.getProperty("duplicateOf").orEmpty().ifBlank { null }
                )
            }.getOrNull()
        }?.sortedByDescending { it.id } ?: emptyList()

    fun import(uri: Uri): ResourceRecord {
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        val name = queryName(uri) ?: "imported-file"
        val dir = newResourceDirectory()
        try {
            val temporary = File(dir, "source.partial")
            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L
            val input = requireNotNull(resolver.openInputStream(uri)) { "Cannot open selected content" }
            input.use { source ->
                temporary.outputStream().buffered().use { output ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        size = IntakePolicy.checkedImportSize(size, count)
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                    }
                }
            }
            val payload = File(dir, "source.bin")
            check(temporary.renameTo(payload)) { "Could not finalize imported resource." }
            return finishImport(dir, name, mime, size, digest.digest(), payload)
        } catch (failure: Throwable) {
            dir.deleteRecursively()
            throw failure
        }
    }

    fun importText(text: String, title: String?): ResourceRecord {
        val normalized = IntakePolicy.normalizeText(text)
        require(IntakePolicy.hasUsefulText(normalized)) { "Shared text is empty" }
        val bytes = normalized.toByteArray(Charsets.UTF_8)
        IntakePolicy.validateImportSize(bytes.size.toLong())
        val dir = newResourceDirectory()
        try {
            val temporary = File(dir, "source.partial")
            temporary.outputStream().use { it.write(bytes) }
            val payload = File(dir, "source.bin")
            check(temporary.renameTo(payload)) { "Could not finalize shared text." }
            val extracted = normalized.take(MAX_TEXT_BYTES.toInt())
            File(dir, "extracted.txt").writeText(extracted)
            return finishImport(
                dir, title ?: "shared-text.txt", "text/plain", bytes.size.toLong(),
                MessageDigest.getInstance("SHA-256").digest(bytes), payload, extracted
            )
        } catch (failure: Throwable) {
            dir.deleteRecursively()
            throw failure
        }
    }

    private fun finishImport(
        dir: File, name: String, mime: String, size: Long, digest: ByteArray,
        payload: File, knownText: String? = null
    ): ResourceRecord {
        val hash = digest.joinToString("") { "%02x".format(it) }
        val prior = list().firstOrNull { it.sha256 == hash }
        val extracted = knownText ?: if (IntakePolicy.isTextMime(mime) && size <= MAX_TEXT_BYTES) {
            payload.readText(Charsets.UTF_8).let(IntakePolicy::normalizeText).also {
                if (IntakePolicy.hasUsefulText(it)) File(dir, "extracted.txt").writeText(it)
            }.takeIf(IntakePolicy::hasUsefulText)
        } else null
        val id = dir.name
        val safeName = IntakePolicy.safeDisplayName(name)
        val props = Properties().apply {
            setProperty("id", id); setProperty("name", safeName); setProperty("mimeType", mime)
            setProperty("size", size.toString()); setProperty("sha256", hash)
            setProperty("duplicateOf", prior?.id ?: "")
        }
        val temporaryMetadata = File(dir, "meta.properties.partial")
        temporaryMetadata.outputStream().use { props.store(it, "SOVEREIGN resource metadata") }
        check(temporaryMetadata.renameTo(File(dir, "meta.properties"))) { "Could not finalize resource metadata." }
        return ResourceRecord(id, safeName, mime, size, hash, extracted, prior?.id)
    }

    private fun newResourceDirectory(): File = File(resourceRoot, UUID.randomUUID().toString()).apply {
        check(mkdirs() && isDirectory) { "Could not create a private resource directory." }
    }

    private fun queryName(uri: Uri): String? =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    companion object { private const val MAX_TEXT_BYTES = 4L * 1024 * 1024 }
}
