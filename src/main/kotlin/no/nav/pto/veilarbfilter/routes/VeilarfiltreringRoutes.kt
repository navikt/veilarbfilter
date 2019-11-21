package no.nav.pto.veilarbfilter.routes

import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.EnhetFilterService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.JwtUtil.Companion.getSubject
import no.nav.pto.veilarbfilter.abac.PepClient


fun Route.veilarbfilterRoutes(enhetFilterService: EnhetFilterService, pepClient: PepClient) {
    route("/api/enhet") {
        authenticate {
            post {
                val ident = getSubject(call)
                call.request.queryParameters["enhetId"]?.let {
                    if (!it.matches("\\d{4}$".toRegex())) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                    if (!pepClient.harTilgangTilEnhet(ident, it)) {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                    val request = call.receive<NyttFilterModel>()
                    val nyttfilter = enhetFilterService.lagreEnhetFilter(it, request)
                    call.respond(nyttfilter)
                }
            }
            get {
                val ident = getSubject(call)
                call.request.queryParameters["enhetId"]?.let {
                    if (!it.matches("\\d{4}$".toRegex())) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                    if (!pepClient.harTilgangTilEnhet(ident, it)) {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                    val filterListe = enhetFilterService.finnFilterForEnhet(it)
                    call.respond(filterListe)
                }
            }
        }
    }
}

