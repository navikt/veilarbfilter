package no.nav.pto.veilarbfilter

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.pto.veilarbfilter.ApplicationState
import no.nav.pto.veilarbfilter.ObjectMapperProvider
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.exceptionHandler
import no.nav.pto.veilarbfilter.notFoundHandler
import no.nav.pto.veilarbfilter.service.EnhetFilterServiceImpl
import org.slf4j.event.Level
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.routes.naisRoutes
import no.nav.pto.veilarbfilter.routes.veilarbfilterRoutes

fun createHttpServer(applicationState: ApplicationState,
                     port: Int = 8080,
                     configuration: Configuration,
                     useAuthentication: Boolean = true): ApplicationEngine = embeddedServer(Netty, port) {
    install(StatusPages) {
        exceptionHandler()
        notFoundHandler();
    }

    install(CallLogging) {
        level = Level.TRACE
        callIdMdc("X-Request-ID")
    }

    install(Authentication) {
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(ObjectMapperProvider.objectMapper))
    }

    //val database = Database(configuration);

    routing {
        route("veilarbfilter") {
            naisRoutes(readinessCheck = { applicationState.initialized }, livenessCheck = { applicationState.running })
            veilarbfilterRoutes(EnhetFilterServiceImpl(), false)
        }
    }

    applicationState.initialized = true
}
