package no.nav.pto.veilarbfilter.client

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.readText
import kotlinx.coroutines.runBlocking
import no.nav.common.utils.IdUtils
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.config.Configuration
import java.lang.IllegalStateException

data class Enhet(val enhetId: String, val navn: String)
data class Veileder(val etternavn: String?, val fornavn: String?, val ident: String, val navn: String?)

data class VeiledereResponse(val enhet: Enhet, val veilederListe: List<Veileder>)


private val veilederPaEnhetenCache = VeilederCache()


class VeilarbveilederClient(config: Configuration) {
    private val veilarbveilederClientUrl = config.veilarbveilederConfig.url

    fun hentVeilederePaEnheten(enhetId: String): List<String>? {
        val veilederCacheValue = veilederPaEnhetenCache.veilederePaEnheten(enhetId)

        if (veilederCacheValue !== null) {
            return veilederCacheValue
        }

        return runBlocking {
            HttpClient(Apache).use { httpClient ->
                val response = get(httpClient, enhetId)
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
    ): List<String>? {
        val res = response.readText()
        val veiledereResponse: List<String> =
            ObjectMapperProvider.objectMapper.readValue(res, object : TypeReference<List<String>>() {});
        veilederPaEnhetenCache.leggTilEnhetICachen(enhetId, veiledereResponse)
        return veiledereResponse
    }

    private suspend fun get(
        httpClient: HttpClient,
        enhetId: String
    ): HttpResponse {
       return httpClient.get<HttpStatement>("$veilarbveilederClientUrl/api/enhet/$enhetId/identer") {
            header("Nav-Call-Id", IdUtils.generateId())
            header("Nav-Consumer-Id", "veilarbfilter")
        }
            .execute()
    }

}