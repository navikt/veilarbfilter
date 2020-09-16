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
import no.nav.pto.veilarbfilter.client.dto.VeiledereResponse
import no.nav.pto.veilarbfilter.config.Configuration
import org.slf4j.LoggerFactory


private val veilederPaEnhetenCache = VeilederCache()

class VeilarbveilederClient(config: Configuration, systemUserTokenProvider: NaisSystemUserTokenProvider?) {
    private val veilarbveilederClientUrl = config.veilarbveilederConfig.url
    private val systemUserTokenProvider = systemUserTokenProvider;
    private val log = LoggerFactory.getLogger("VeilarbveilederClient")

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
        val veiledereResponse: VeiledereResponse =
            ObjectMapperProvider.objectMapper.readValue(res, object : TypeReference<VeiledereResponse>() {});
        val veilederIdentList = veiledereResponse.veilederListe.map { it.ident }
        veilederPaEnhetenCache.leggTilEnhetICachen(enhetId, veilederIdentList)
        return veilederIdentList
    }

    private suspend fun get(
        httpClient: HttpClient,
        enhetId: String
    ): HttpResponse {
        return httpClient.get<HttpStatement>("$veilarbveilederClientUrl/api/enhet/$enhetId/veiledere") {
            header("Nav-Call-Id", IdUtils.generateId())
            header("Nav-Consumer-Id", "veilarbfilter")
            if (systemUserTokenProvider != null) {
                log.info("Adding user token, token length: " + systemUserTokenProvider.systemUserToken.length)
                header("Authorization", "Bearer " + systemUserTokenProvider.systemUserToken)
            } else {
                log.warn("System user token er null")
            }
        }
            .execute()
    }

}