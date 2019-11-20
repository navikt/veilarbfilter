package no.nav.pto.veilarbfilter.abac

import com.sun.tools.doclets.standard.Standard
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import kotlinx.coroutines.runBlocking
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import no.nav.abac.xacml.NavAttributter
import no.nav.abac.xacml.StandardAttributter
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.config.Configuration

private const val MEDIA_TYPE = "application/xacml+json"

class PepClient (config: Configuration) {

    private val abacUrl = config.abac.url

    private val abacClient by lazy {
        HttpClient(Apache) {
            install(Auth) {
                basic {
                    username = config.abac.username
                    password = config.abac.password
                }
            }
        }
    }

    fun harTilgangTilEnhet (bearerToken: String?, enhetId: String): Boolean {
        requireNotNull(bearerToken) { "Authorization token not set" }
        val subject = extractTokenBody(bearerToken);
        val xacmlRequest = createXacmlRequest(subject, enhetId)
        val xacmlResponse = askForPermission(xacmlRequest)
        return harTilgang(xacmlResponse.response?.decision)
    }

    private fun harTilgang (decision: Decision?): Boolean {
        return decision == Decision.Permit;
    }

    private fun askForPermission(xacmlRequest: XacmlRequest): XacmlResponse {
        val xacml = xacmlRequest.build();
        val xacmlJson = ObjectMapperProvider.objectMapper.writeValueAsString(xacml);
        return runBlocking {
            abacClient.use { httpClient ->
                val result = httpClient.post<HttpResponse>(abacUrl) {
                    body = TextContent(xacmlJson, ContentType.parse(MEDIA_TYPE))
                }
                if (result.status.value != 200) {
                    throw RuntimeException("Abac kallet feilet ${result.status.description}")
                }
                val res = result.readText()
                ObjectMapperProvider.objectMapper.readValue(res, XacmlResponse::class.java)
            }
        }
    }

    private fun createXacmlRequest(subject: String, enhetId: String) : XacmlRequest {
        return XacmlRequest()
            .addAttribute("Environment", NavAttributter.ENVIRONMENT_FELLES_PEP_ID, "veilarbfilter")
            .addAttribute("Environment", NavAttributter.ENVIRONMENT_FELLES_CONSUMER_OIDC_TOKEN_BODY, subject)
            .addAttribute("Action", StandardAttributter.ACTION_ID, "read")
            .addAttribute("Resource", NavAttributter.RESOURCE_FELLES_DOMENE, "veilarb")
            .addAttribute("Resource", NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_FELLES_ENHET)
            .addAttribute("Resource", NavAttributter.RESOURCE_FELLES_ENHET, enhetId)
    }

    private fun extractTokenBody(bearerToken: String): String {
        return bearerToken.substringAfter(" ")
    }
}