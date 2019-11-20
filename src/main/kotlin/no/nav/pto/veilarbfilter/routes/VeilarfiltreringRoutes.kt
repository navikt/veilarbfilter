package no.nav.pto.veilarbfilter.routes

import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.EnhetFilterService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.abac.PepClient


fun Route.veilarbfilterRoutes(enhetFilterService: EnhetFilterService, pepClient: PepClient) {
    route("/api/enhet") {
        post("/") {
            val request = call.receive<NyttFilterModel>();
            call.request.queryParameters["enhetId"]?.let {
                if(!it.matches("\\d{4}$".toRegex())) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                if(!pepClient.harTilgangTilEnhet(call.request.header("Authorization"),it)) {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                val nyttfilter = enhetFilterService.lagreEnhetFilter(it, request)
                call.respond(nyttfilter);
            }
        }
        get("/") {
            call.request.queryParameters["enhetId"]?.let {
                if(!it.matches("\\d{4}$".toRegex())) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                if(!pepClient.harTilgangTilEnhet(call.request.header("Authorization"),it)) {
                    call.respond(HttpStatusCode.Forbidden)
                }
                val filterListe = enhetFilterService.finnFilterForEnhet(it)
                call.respond(filterListe);
            }
        }
    }
}

