package no.nav.pto.veilarbfilter.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpHeaders
import io.ktor.http.cookies
import kotlinx.coroutines.runBlocking
import no.nav.common.utils.IdUtils
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.config.Configuration
import org.slf4j.LoggerFactory


data class Enhet (val enhetId: String, val navn: String)
data class Veileder(val etternavn: String?, val fornavn: String?, val ident: String, val navn: String?)

data class VeiledereResponse (val enhet: Enhet, val veilederListe: List<Veileder>)


private val veilederPaEnhetenCache = VeilederCache()


class VeilarbveilederClient (config: Configuration) {
    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private val veilarbveilederClientUrl = config.veilarbveilederConfig.url

    fun hentVeilederePaEnheten(enhetId: String, requestToken: String?): VeiledereResponse {
        requireNotNull(requestToken) { "RequestToken is not set" }
        val veilederCacheValue = veilederPaEnhetenCache.veilederePaEnheten(enhetId)

        if(veilederCacheValue !== null) {
            return veilederCacheValue
        }

        return runBlocking {
            HttpClient(Apache).use { httpClient ->
                val result = httpClient.get<HttpResponse>("$veilarbveilederClientUrl/api/enhet/$enhetId/veiledere") {
                    header(HttpHeaders.Authorization, "Bearer $requestToken")
                    header("Nav-Call-Id", IdUtils.generateId())
                    header("Nav-Consumer-Id", "veilarbfilter")
                }
                if (result.status.value != 200) {
                    log.error("Veilarbveileder kallet feilet ${result.status.description}")
                }
                val res = result.readText()
                val veiledereResponse = ObjectMapperProvider.objectMapper.readValue(res, VeiledereResponse::class.java)
                veilederPaEnhetenCache.leggTilEnhetICachen(enhetId, veiledereResponse)
                veiledereResponse
            }
        }
    }

}