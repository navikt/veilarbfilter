package no.nav.pto.veilarbfilter.client

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import no.nav.common.sts.NaisSystemUserTokenProvider
import no.nav.common.types.identer.EnhetId
import no.nav.common.utils.IdUtils
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.client.dto.IdentOgEnhetliste
import no.nav.pto.veilarbfilter.config.Configuration
import kotlin.streams.toList


private val veilederPaEnhetenCache = VeilederCache()
private val enheterForVeilederCache = EnheterForVeilederCache()


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
                val response = hentVeilederePaEnhetenFraAPI(httpClient, enhetId)
                when (response.status.value) {
                    200 -> readVeiledereResponse(response, enhetId)
                    else -> throw IllegalStateException("Feilet mot veilarbveileder " + response.status.value)
                }
            }
        }
    }

    fun hentEnheterForVeileder(veilederId: String): List<EnhetId>? {
        val enheterForVeileder = enheterForVeilederCache.enheterForVeileder(veilederId)

        if (enheterForVeileder !== null) {
            return enheterForVeileder
        }

        return runBlocking {
            HttpClient(Apache).use { httpClient ->
                val response = hentEnhetenForVeilederFraAPI(httpClient, veilederId)
                when (response.status.value) {
                    200 -> readEnheterForVeileder(response, veilederId)
                    else -> throw IllegalStateException("Feilet mot henting enheter for veileder " + response.status.value)
                }
            }
        }
    }

    private suspend fun readVeiledereResponse(
        response: HttpResponse,
        enhetId: String
    ): List<String>? {
        val res = response.readText()
        val veiledereResponse: List<String> =
            ObjectMapperProvider.objectMapper.readValue(res, object : TypeReference<List<String>>() {});
        veilederPaEnhetenCache.leggTilEnhetICachen(enhetId, veiledereResponse)
        return veiledereResponse
    }

    private suspend fun readEnheterForVeileder(
        response: HttpResponse,
        veilederIdent: String
    ): List<EnhetId> {
        val res = response.readText()
        val identOgEnhetliste: IdentOgEnhetliste =
            ObjectMapperProvider.objectMapper.readValue(res, object : TypeReference<IdentOgEnhetliste>() {});
        val enheterIdsList = identOgEnhetliste.enhetliste.stream().map { it.enhetId }.toList()
        enheterForVeilederCache.leggTilEnheterICachen(veilederIdent, enheterIdsList)
        return enheterIdsList
    }

    private suspend fun hentVeilederePaEnhetenFraAPI(
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

    private suspend fun hentEnhetenForVeilederFraAPI(
        httpClient: HttpClient,
        veilederIdent: String
    ): HttpResponse {
        return httpClient.get<HttpStatement>("$veilarbveilederClientUrl/api/veileder/enheter/$veilederIdent") {
            header("Nav-Call-Id", IdUtils.generateId())
            header("Nav-Consumer-Id", "veilarbfilter")
            if (systemUserTokenProvider != null) {
                header("Authorization", "Bearer " + systemUserTokenProvider.systemUserToken)
            }
        }
            .execute()
    }

}