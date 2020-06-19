package no.nav.pto.veilarbfilter.routes

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.JwtUtil
import no.nav.pto.veilarbfilter.abac.PepClient
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.FilterService


fun Route.mineFilterRoutes(mineFilterService: FilterService, pepClient: PepClient) {
    authenticate {
        route("/api/minefilter") {
            post("/") {
                JwtUtil.getSubject(call)?.let { veilederId ->
                    val nyttFilter = call.receive<NyttFilterModel>();
                    val savedFilter = mineFilterService.lagreFilter(veilederId, nyttFilter);
                    savedFilter?.let { call.respond(it) } ?: throw IllegalArgumentException()
                }
            }
            put("/{filterId}") {
                JwtUtil.getSubject(call)?.let {
                    val oppdatertFilter = mineFilterService.oppdaterFilter(it, call.receive())
                    call.respond(oppdatertFilter)
                }
            }
            get("/") {
                pepAuth(pepClient) {
                    JwtUtil.getSubject(call)?.let { veilederId ->
                        val filterListe = mineFilterService.finnFilterForFilterBruker(it)
                        call.respond(filterListe)
                    }
                }
            }
            delete("/{enhetId}/filter/{filterId}") {
                pepAuth(pepClient) {
                    JwtUtil.getSubject(call)?.let { veilederId ->
                        call.parameters["filterId"]?.let { filter ->
                            val slettetFilterId = mineFilterService.slettFilter(filter.toInt(), veilederId)
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
}

