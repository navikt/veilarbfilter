package no.nav.pto.veilarbfilter.routes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pto.veilarbfilter.MockPayload
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.SortOrder
import no.nav.pto.veilarbfilter.service.MineLagredeFilterServiceImpl
import org.slf4j.LoggerFactory


private val log = LoggerFactory.getLogger("MineLagredeFilterRoutes")

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

fun Route.mineLagredeFilterRoutes(mineLagredeFilterService: MineLagredeFilterServiceImpl, useAuthentication: Boolean) {
    conditionalAuthenticate(useAuthentication) {
        route("/minelagredefilter/{enhetId}/") {
            post {
                call.getNavident()?.let { veilederId ->
                    val enhetId = call.parameters["enhetId"]
                    enhetId?.let {
                        val nyttFilter = call.receive<NyttFilterModel>()
                        val savedFilter = mineLagredeFilterService.lagreFilter(veilederId, nyttFilter, enhetId)
                        savedFilter?.let { call.respond(it) } ?: throw IllegalArgumentException()
                    }
                }
            }
            put {
                call.getNavident()?.let { veilederId ->
                    val enhetId = call.parameters["enhetId"]
                    enhetId?.let {
                        val filterModel: FilterModel = call.receive()
                        val oppdatertFilter = mineLagredeFilterService.oppdaterFilter(veilederId, filterModel, enhetId)
                        call.respond(oppdatertFilter)
                    }
                }
            }
            get {
                call.getNavident()?.let { veilederId ->
                    val enhetId = call.parameters["enhetId"]
                    enhetId?.let {
                        val filterListe = mineLagredeFilterService.finnFilterForFilterBruker(veilederId, enhetId)
                        call.respond(filterListe)
                    }
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
            post("/lagresortering") {
                call.getNavident()?.let { veilederId ->
                    try {
                        val enhetId = call.parameters["enhetId"]
                        enhetId?.let {
                            val sortOrder = jacksonObjectMapper().readValue<List<SortOrder>>(call.receiveText())
                            val lagreSortering = mineLagredeFilterService.lagreSortering(veilederId, sortOrder, enhetId)
                            lagreSortering?.let { call.respond(it) }
                        }
                    } catch (e: Exception) {
                        log.error("Lagre sortering fail:$e", e)
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