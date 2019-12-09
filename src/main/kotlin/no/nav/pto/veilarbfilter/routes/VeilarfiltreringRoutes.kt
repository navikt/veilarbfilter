package no.nav.pto.veilarbfilter.routes

import io.ktor.application.ApplicationCall
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.EnhetFilterService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.pto.veilarbfilter.JwtUtil.Companion.getSubject
import no.nav.pto.veilarbfilter.abac.PepClient
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.model.FilterModel

suspend fun PipelineContext<Unit, ApplicationCall>.pepAuth(pepClient: PepClient, build: suspend PipelineContext<Unit, ApplicationCall>.(id: String) -> Unit) {
    val ident = getSubject(call)
    val enhetId = call.parameters["enhetId"]

    enhetId?.let {
        if (!it.matches("\\d{4}$".toRegex())) {
            call.respond(HttpStatusCode.BadRequest)
        } else if (!pepClient.harTilgangTilEnhet(ident, it)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            build.invoke(this, it)
        }
    }
}

fun Route.veilarbfilterRoutes(enhetFilterService: EnhetFilterService, pepClient: PepClient, veilarbveilederClient: VeilarbveilederClient) {
    authenticate {
        route("/api/enhet") {
            post("/{enhetId}") {
                pepAuth(pepClient) {
                    val request = call.receive<NyttFilterModel>()
                    val nyttfilter = enhetFilterService.lagreEnhetFilter(it, request)
                    call.respond(nyttfilter)
                }
            }
            put("/{enhetId}") {
                pepAuth(pepClient) {
                    val request = call.receive<FilterModel>()
                    val nyttfilter = enhetFilterService.oppdaterEnhetFilter(it, request)
                    call.respond(nyttfilter)
                }
            }
            get("/{enhetId}") {
                pepAuth(pepClient) {
                    val veilederePaEnheten = veilarbveilederClient.hentVeilederePaEnheten(it, call.request.cookies["ID_token"])
                    val filterListe = enhetFilterService.finnFilterForEnhet(it, veilederePaEnheten)
                    call.respond(filterListe)
                }
            }
            delete("/{enhetId}/filter/{filterId}") {
                pepAuth(pepClient) {
                    call.parameters["filterId"]?.let { filter ->
                        val slettetFilterId = enhetFilterService.slettFilter(it, filter)
                        if(slettetFilterId == 0 ) {
                            call.respond(HttpStatusCode.NotFound)
                        }
                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}