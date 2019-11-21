package no.nav.pto.veilarbfilter.routes

import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.EnhetFilterService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.JwtUtil
import no.nav.pto.veilarbfilter.JwtUtil.Companion.getBlob
import no.nav.pto.veilarbfilter.abac.PepClient


fun Route.veilarbfilterRoutes(enhetFilterService: EnhetFilterService, pepClient: PepClient) {
    route("/api/enhet") {
        authenticate {
            post {
                val cookie = JwtUtil.useJwtFromCookie(call)?.getBlob()
                call.request.queryParameters["enhetId"]?.let {
                    if (!it.matches("\\d{4}$".toRegex())) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                    if (!pepClient.harTilgangTilEnhet(cookie, it)) {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                    val request = call.receive<NyttFilterModel>()
                    val nyttfilter = enhetFilterService.lagreEnhetFilter(it, request)
                    call.respond(nyttfilter)
                }
            }
            get {
                val cookie = JwtUtil.useJwtFromCookie(call)?.getBlob()
                call.request.queryParameters["enhetId"]?.let {
                    if (!it.matches("\\d{4}$".toRegex())) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                    if (!pepClient.harTilgangTilEnhet(cookie, it)) {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                    val filterListe = enhetFilterService.finnFilterForEnhet(it)
                    call.respond(filterListe)
                }
            }
        }
    }
}

