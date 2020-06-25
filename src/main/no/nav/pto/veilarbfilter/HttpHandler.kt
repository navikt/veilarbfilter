package no.nav.pto.veilarbfilter

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.jackson.JacksonConverter
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.log.LogFilter
import no.nav.pto.veilarbfilter.abac.PepClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.routes.internalRoutes
import no.nav.pto.veilarbfilter.routes.mineLagredeFilterRoutes
import no.nav.pto.veilarbfilter.routes.veilederGruppeRoutes
import no.nav.pto.veilarbfilter.service.MineLagredeFilterServiceImpl
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl

fun createHttpServer(
    applicationState: ApplicationState,
    port: Int = 8080,
    configuration: Configuration,
    veilederGrupperService: VeilederGrupperServiceImpl,
    useAuthentication: Boolean = true
): ApplicationEngine = embeddedServer(Netty, port) {
    install(StatusPages) {
        exceptionHandler()
        notFoundHandler()
    }


    install(Authentication) {
        jwt {
            authHeader(JwtUtil.Companion::useJwtFromCookie)
            realm = "veilarbfilter"
            verifier(configuration.jwt.jwksUrl, configuration.jwt.jwtIssuer)
            validate { JwtUtil.validateJWT(it) }
        }
    }


    install(CORS) {
        anyHost()
        method(HttpMethod.Put)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)

        allowCredentials = true
    }

    install(CallLogging) {
        LogFilter()
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(ObjectMapperProvider.objectMapper))
    }

    routing {
        route("veilarbfilter") {
            internalRoutes(
                readinessCheck = { applicationState.initialized },
                livenessCheck = { applicationState.running })
            route("/api/") {
                veilederGruppeRoutes(veilederGrupperService, PepClient(config = configuration))
                mineLagredeFilterRoutes(MineLagredeFilterServiceImpl(), useAuthentication)
            }
        }
    }

    applicationState.initialized = true
}
