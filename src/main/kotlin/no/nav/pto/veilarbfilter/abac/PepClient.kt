package no.nav.pto.veilarbfilter.abac

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
private val abacCache = AbacCache()

class PepClient (config: Configuration) {

    private val abacUrl = ""
    private val username =""
    private val password = ""

    fun harTilgangTilEnhet (ident: String?, enhetId: String): Boolean {
        requireNotNull(ident) { "Ident is not set" }
        val finnesICache = abacCache.harTilgangTilEnheten(ident, enhetId)
        if(finnesICache != null) {
            return finnesICache
        }
        val xacmlRequest = createXacmlRequest(ident, enhetId)
        val xacmlResponse = askForPermission(xacmlRequest)
        val harTilgangFraAabac = harTilgang(xacmlResponse.response?.decision)
        abacCache.leggTilEnhetICachen(ident, enhetId, harTilgangFraAabac)

        return harTilgangFraAabac
    }

    private fun harTilgang (decision: Decision?): Boolean {
        return decision == Decision.Permit
    }

    private fun askForPermission(xacmlRequest: XacmlRequest): XacmlResponse {
        val xacml = xacmlRequest.build()
        val xacmlJson = ObjectMapperProvider.objectMapper.writeValueAsString(xacml)
        val abacClient = createAbacHttpClient(username, password)
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
            .addAttribute("Action", StandardAttributter.ACTION_ID, "read")
            .addAttribute("Resource", NavAttributter.RESOURCE_FELLES_DOMENE, "veilarb")
            .addAttribute("Resource", NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_FELLES_ENHET)
            .addAttribute("Resource", NavAttributter.RESOURCE_FELLES_ENHET, enhetId)
            .addAttribute("AccessSubject", StandardAttributter.SUBJECT_ID, subject)
            .addAttribute("AccessSubject", NavAttributter.SUBJECT_FELLES_SUBJECTTYPE, "InternBruker")
    }
}

private fun createAbacHttpClient(serviceUsername: String, servicePassword: String): HttpClient =
    HttpClient(Apache) {
        install(Auth) {
            basic {
                username = serviceUsername
                password = servicePassword
            }
        }
    }