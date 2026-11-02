package org.sovereignos.models

import java.net.URI

data class EndpointValidation(val isValid: Boolean, val message: String)

object EndpointPolicy {
    fun validate(raw: String): EndpointValidation {
        val uri = runCatching { URI(raw.trim()) }.getOrNull()
            ?: return EndpointValidation(false, "Enter a valid URL.")
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.removePrefix("[")?.removeSuffix("]")
            ?: return EndpointValidation(false, "URL must include a hostname or IP address.")
        if (uri.userInfo != null || uri.rawQuery != null || uri.rawFragment != null) {
            return EndpointValidation(false, "Credentials, query strings and fragments are not allowed in endpoint URLs.")
        }
        if (uri.port !in -1..65535 || uri.port == 0) {
            return EndpointValidation(false, "Port must be between 1 and 65535.")
        }
        if (scheme == "https") {
            return EndpointValidation(true, if (isPrivateHost(host)) {
                "Private HTTPS endpoint accepted; no API key is stored."
            } else {
                "Public HTTPS endpoint accepted. Confirm before sending repository data."
            })
        }
        if (scheme != "http") return EndpointValidation(false, "Only HTTP or HTTPS endpoints are supported.")
        return if (isPrivateHost(host)) {
            EndpointValidation(true, "Private-network HTTP endpoint accepted.")
        } else {
            EndpointValidation(false, "Plain HTTP is restricted to loopback/private LAN addresses. Use HTTPS for public hosts.")
        }
    }

    fun requiresRemoteConsent(raw: String): Boolean {
        val uri = runCatching { URI(raw.trim()) }.getOrNull() ?: return true
        val host = uri.host?.removePrefix("[")?.removeSuffix("]") ?: return true
        return uri.scheme.equals("https", true) && !isPrivateHost(host)
    }

    private fun isPrivateHost(host: String): Boolean {
        if (host.equals("localhost", true)) return true
        if (host.contains(':')) {
            val normalized = host.lowercase()
            return normalized == "::1" || normalized.startsWith("fc") || normalized.startsWith("fd")
        }
        val parts = host.split('.')
        if (parts.size != 4) return false
        val octets = parts.map { it.toIntOrNull() ?: return false }
        if (octets.any { it !in 0..255 }) return false
        return octets[0] == 10 || octets[0] == 127 ||
            (octets[0] == 192 && octets[1] == 168) ||
            (octets[0] == 172 && octets[1] in 16..31)
    }
}
