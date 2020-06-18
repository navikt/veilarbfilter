package no.nav.pto.veilarbfilter.routes

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.JwtUtil
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.FilterService


fun Route.apiMineFilterRoutes(mineFilterService: FilterService) {
    authenticate {
        route("/api/minefilter") {
            post("/") {
                JwtUtil.getSubject(call)?.let { veilederId ->
                    val nyttFilter = call.receive<NyttFilterModel>();
                    val savedFilter = mineFilterService.lagreFilter(veilederId, nyttFilter);
                    savedFilter?.let { call.respond(it) } ?: throw IllegalArgumentException()
                }
            }
            put("/{enhetId}") {
                val oppdatertFilter = enhetFilterService.oppdaterEnhetFilter(it, request)
                call.respond(oppdatertFilter)
            }
            get("/{enhetId}") {
                pepAuth(pepClient) {
                    val veilederePaEnheten = veilarbveilederClient
                            .hentVeilederePaEnheten(it, call.request.cookies["ID_token"])
                            ?: throw IllegalStateException()

                    val filterListe = enhetFilterService.finnFilterForEnhet(it, veilederePaEnheten)
                    call.respond(filterListe)
                }
            }
            delete("/{enhetId}/filter/{filterId}") {
                pepAuth(pepClient) {
                    call.parameters["filterId"]?.let { filter ->
                        val slettetFilterId = enhetFilterService.slettFilter(it, filter)
                        if (slettetFilterId == 0) {
                            call.respond(HttpStatusCode.NotFound)
                        }
                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}

