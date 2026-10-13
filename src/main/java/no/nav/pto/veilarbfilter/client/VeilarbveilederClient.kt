package no.nav.pto.veilarbfilter.client

import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.json.JsonUtils
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.pto.veilarbfilter.domene.OverblikkVisningRequest
import no.nav.pto.veilarbfilter.domene.OverblikkVisningResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient

class VeilarbveilederClient(
    private val restClient: RestClient,
    private val scope: String,
    private val tokenClient: AzureAdOnBehalfOfTokenClient,
    private val authContextHolder: AuthContextHolder
) {
    fun hent(): ResponseEntity<OverblikkVisningResponse> {
        val response = send(HttpMethod.GET)
        if (response.statusCode != HttpStatus.OK) {
            return ResponseEntity.status(response.statusCode).build()
        }
        return ResponseEntity.ok(JsonUtils.fromJson(response.body, OverblikkVisningResponse::class.java))
    }

    fun lagre(request: OverblikkVisningRequest): ResponseEntity<Any> {
        val response = send(HttpMethod.POST, JsonUtils.toJson(request))
        return ResponseEntity.status(response.statusCode).build()
    }

    fun slett(): ResponseEntity<Any> {
        val response = send(HttpMethod.DELETE)
        return ResponseEntity.status(response.statusCode).build()
    }

    private fun send(method: HttpMethod, body: String? = null): ResponseEntity<String> {
        val token = tokenClient.exchangeOnBehalfOfToken(scope, authContextHolder.requireIdTokenString())
        val request = restClient.method(method)
            .uri(ENDPOINT)
            .header("Authorization", "Bearer $token")
            .accept(MediaType.APPLICATION_JSON)

        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body)
        }

        return request.retrieve()
            .onStatus({ it.isError }) { _, _ -> }
            .toEntity(String::class.java)
    }

    companion object {
        private const val ENDPOINT = "/veilarbveileder/api/overblikkvisning"
    }
}
