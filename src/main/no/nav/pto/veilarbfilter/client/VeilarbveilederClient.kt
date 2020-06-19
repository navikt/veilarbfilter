package no.nav.pto.veilarbfilter.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import no.nav.common.utils.IdUtils
import no.nav.pto.veilarbfilter.BadGatewayException
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.config.Configuration
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException

data class Enhet(val enhetId: String, val navn: String)
data class Veileder(val etternavn: String?, val fornavn: String?, val ident: String, val navn: String?)

data class VeiledereResponse(val enhet: Enhet, val veilederListe: List<Veileder>)


private val veilederPaEnhetenCache = VeilederCache()


class VeilarbveilederClient(config: Configuration) {
    private val veilarbveilederClientUrl = config.veilarbveilederConfig.url

    fun hentVeilederePaEnheten(enhetId: String, requestToken: String?): VeiledereResponse? {
        requireNotNull(requestToken) { "RequestToken is not set" }
        val veilederCacheValue = veilederPaEnhetenCache.veilederePaEnheten(enhetId)

        if (veilederCacheValue !== null) {
            return veilederCacheValue
        }

        return runBlocking {
            HttpClient(Apache).use { httpClient ->
                val response = get(httpClient, enhetId, requestToken)
                when (response.status.value) {
                    200 -> readResponse(response, enhetId)
                    else -> throw IllegalStateException("Feilet mot veilarbveileder")
                }
            }
        }
    }

    private suspend fun readResponse(
        response: HttpResponse,
        enhetId: String
    ): VeiledereResponse? {
        val res = response.readText()
        val veiledereResponse =
            ObjectMapperProvider.objectMapper.readValue(res, VeiledereResponse::class.java)
        veilederPaEnhetenCache.leggTilEnhetICachen(enhetId, veiledereResponse)
        return veiledereResponse
    }

    private suspend fun get(
        httpClient: HttpClient,
        enhetId: String,
        requestToken: String?
    ): HttpResponse {
        val result = httpClient.get<HttpResponse>("$veilarbveilederClientUrl/api/enhet/$enhetId/veiledere") {
            header(HttpHeaders.Authorization, "Bearer $requestToken")
            header("Nav-Call-Id", IdUtils.generateId())
            header("Nav-Consumer-Id", "veilarbfilter")
        }
        return result
    }

}