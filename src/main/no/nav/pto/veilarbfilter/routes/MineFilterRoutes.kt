package no.nav.pto.veilarbfilter.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.MockPayload
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.FilterService


fun Route.conditionalAuthenticate(useAuthentication: Boolean, build: Route.() -> Unit): Route {
    if (useAuthentication) {
        return authenticate(build = build)
    }
    val route = createChild(AuthenticationRouteSelector(listOf<String?>(null)))
    route.insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.AuthenticatePhase)
    route.intercept(Authentication.AuthenticatePhase) {
        this.context.authentication.principal = JWTPrincipal(MockPayload("Z999999"))
    }
    route.build()
    return route
}

fun Route.mineFilterRoutes(mineFilterService: FilterService, useAuthentication: Boolean) {
    conditionalAuthenticate(useAuthentication) {
        route("/minefilter") {
            post {
                call.getNavident()?.let { veilederId ->
                    val nyttFilter = call.receive<NyttFilterModel>()
                    val savedFilter = mineFilterService.lagreFilter(veilederId, nyttFilter)
                    savedFilter?.let { call.respond(it) } ?: throw IllegalArgumentException()
                }
            }
            put("/{filterId}") {
                call.getNavident()?.let {
                    val oppdatertFilter = mineFilterService.oppdaterFilter(it, call.receive())
                    call.respond(oppdatertFilter)
                }
            }
            get {
                call.getNavident()?.let { veilederId ->
                    val filterListe = mineFilterService.finnFilterForFilterBruker(veilederId)
                    call.respond(filterListe)
                }
            }
            delete("/{enhetId}/filter/{filterId}") {
                call.getNavident()?.let { veilederId ->
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

private fun ApplicationCall.getNavident(): String? {
    return this.principal<JWTPrincipal>()
        ?.payload
        ?.subject
}