package no.nav.pto.veilarbfilter.client

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import no.nav.common.sts.NaisSystemUserTokenProvider
import no.nav.common.utils.IdUtils
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.config.Configuration


private val veilederPaEnhetenCache = VeilederCache()


class VeilarbveilederClient(config: Configuration, systemUserTokenProvider: NaisSystemUserTokenProvider?) {
    private val veilarbveilederClientUrl = config.veilarbveilederConfig.url
    private val systemUserTokenProvider = systemUserTokenProvider;

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
                    else -> throw IllegalStateException("Feilet mot veilarbveileder " + response.status.value)
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
            if (systemUserTokenProvider != null) {
                header("Authorization", "Bearer " + systemUserTokenProvider.systemUserToken)
            }
        }
                .execute()
    }

}