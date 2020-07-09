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
import no.nav.pto.veilarbfilter.model.FilterModel
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

fun Route.mineLagredeFilterRoutes(mineLagredeFilterService: FilterService, useAuthentication: Boolean) {
    conditionalAuthenticate(useAuthentication) {
        route("/minelagredefilter") {
            post {
                call.getNavident()?.let { veilederId ->
                    val nyttFilter = call.receive<NyttFilterModel>()
                    val savedFilter = mineLagredeFilterService.lagreFilter(veilederId, nyttFilter)
                    savedFilter?.let { call.respond(it) } ?: throw IllegalArgumentException()
                }
            }
            put {
                call.getNavident()?.let { veilederId ->
                    val filterModel: FilterModel = call.receive()
                    val oppdatertFilter = mineLagredeFilterService.oppdaterFilter(veilederId, filterModel)
                    call.respond(oppdatertFilter)
                }
            }
            get {
                call.getNavident()?.let { veilederId ->
                    val filterListe = mineLagredeFilterService.finnFilterForFilterBruker(veilederId)
                    call.respond(filterListe)
                }
            }
            delete("/{filterId}") {
                call.getNavident()?.let { veilederId ->
                    call.parameters["filterId"]?.let { filter ->
                        val slettetFilterId = mineLagredeFilterService.slettFilter(filter.toInt(), veilederId)
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