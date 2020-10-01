package no.nav.pto.veilarbfilter.routes

import io.ktor.application.ApplicationCall
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.pto.veilarbfilter.JwtUtil.Companion.getSubject
import no.nav.pto.veilarbfilter.abac.PepClient
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.service.FilterService

suspend fun PipelineContext<Unit, ApplicationCall>.pepAuth(pepClient: PepClient,useAuthentication: Boolean, build: suspend PipelineContext<Unit, ApplicationCall>.(id: String) -> Unit) {
    val ident = getSubject(call)
    val enhetId = call.parameters["enhetId"]

    enhetId?.let {
        if(!useAuthentication){
            build.invoke(this, it)
        }else if (!it.matches("\\d{4}$".toRegex())) {
            call.respond(HttpStatusCode.BadRequest)
        } else if (!pepClient.harTilgangTilEnhet(ident, it)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            build.invoke(this, it)
        }
    }
}

fun Route.veilederGruppeRoutes(veilederGrupperService: FilterService, pepClient: PepClient, useAuthentication: Boolean) {
    conditionalAuthenticate(useAuthentication) {
        route("/enhet") {
            post("/{enhetId}") {
                pepAuth(pepClient, useAuthentication) {
                    val request = call.receive<NyttFilterModel>()
                    veilederGrupperService.lagreFilter(it, request)
                        ?.let {nyttfilter -> call.respond(nyttfilter)}
                        ?: throw IllegalStateException()

                }
            }
            put("/{enhetId}") {
                pepAuth(pepClient, useAuthentication) {
                    val request = call.receive<FilterModel>()
                    val oppdatertFilter = veilederGrupperService.oppdaterFilter(it, request)
                    call.respond(oppdatertFilter)
                }
            }
            get("/{enhetId}") {
                pepAuth(pepClient, useAuthentication) {
                    val filterListe = veilederGrupperService.finnFilterForFilterBruker(it)
                    call.respond(filterListe)
                }
            }
            delete("/{enhetId}/filter/{filterId}") {
                pepAuth(pepClient, useAuthentication) {
                    call.parameters["filterId"]?.let { filter ->
                        val slettetFilterId = veilederGrupperService.slettFilter(filter.toInt(), call.parameters["enhetId"]!!)
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
