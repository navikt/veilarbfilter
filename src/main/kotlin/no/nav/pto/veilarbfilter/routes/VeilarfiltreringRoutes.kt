package no.nav.pto.veilarbfilter.routes

import io.ktor.http.ContentType
import io.ktor.response.respondText
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.service.EnhetFilterService
import io.ktor.application.call
import io.ktor.auth.AuthenticationRouteSelector
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.conditionalAuthenticate(useAuthentication: Boolean, build: Route.() -> Unit): Route {
    if (useAuthentication) {
        return authenticate(build = build)
    }
    val route = createChild(AuthenticationRouteSelector(listOf<String?>(null)))
    route.build()
    return route
}


fun Route.veilarbfilterRoutes(enhetFilterService: EnhetFilterService, useAuthentication: Boolean) {
    route("/api/enhet") {
        post("/{enhetId}") {
            val request = call.receive<NyttFilterModel>();
            call.parameters["enhetId"]?.let {
                val nyttfilter = enhetFilterService.lagreEnhetFilter(it, request)
                call.respond(nyttfilter);
            }
        }
        get("/{enhetId}") {
            call.parameters["enhetId"]?.let {
                val filterListe = enhetFilterService.finnFilterForEnhet(it)
                call.respond(filterListe);
            }
        }
    }
    route("/api/veileder") {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
    }
}

