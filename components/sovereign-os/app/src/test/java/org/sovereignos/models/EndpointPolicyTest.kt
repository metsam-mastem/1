package org.sovereignos.models

import org.junit.Assert.*
import org.junit.Test

class EndpointPolicyTest {
    @Test fun allowsLoopbackAndPrivateLanHttpEndpoints() {
        assertTrue(EndpointPolicy.validate("http://127.0.0.1:11434").isValid)
        assertTrue(EndpointPolicy.validate("http://192.168.1.20:1234/v1").isValid)
        assertTrue(EndpointPolicy.validate("http://10.0.0.8:8080").isValid)
        assertTrue(EndpointPolicy.validate("http://[::1]:8080").isValid)
    }

    @Test fun blocksPlainHttpToPublicHosts() {
        val result = EndpointPolicy.validate("http://example.com:1234/v1")
        assertFalse(result.isValid)
        assertTrue(result.message.contains("HTTPS"))
    }

    @Test fun requiresConsentBeforeSendingRepositoryContextToPublicHttps() {
        assertTrue(EndpointPolicy.requiresRemoteConsent("https://api.example.com/v1"))
        assertFalse(EndpointPolicy.requiresRemoteConsent("https://192.168.1.10:8080/v1"))
        assertFalse(EndpointPolicy.requiresRemoteConsent("http://127.0.0.1:11434"))
    }

    @Test fun rejectsLinkLocalAndCloudMetadataAddresses() {
        assertFalse(EndpointPolicy.validate("http://169.254.169.254/latest/meta-data").isValid)
        assertFalse(EndpointPolicy.validate("http://[fe80::1]:8080").isValid)
    }

    @Test fun allowsHttpsWithoutApiKey() {
        assertTrue(EndpointPolicy.validate("https://my-local-domain.example/v1").isValid)
    }

    @Test fun rejectsMalformedAndCredentialBearingUrls() {
        assertFalse(EndpointPolicy.validate("not a url").isValid)
        assertFalse(EndpointPolicy.validate("http://user:pass@127.0.0.1:1234").isValid)
        assertFalse(EndpointPolicy.validate("file:///tmp/model").isValid)
    }
}
