package no.nav.pto.veilarbfilter

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.common.log.LogFilter
import no.nav.common.utils.EnvironmentUtils
import no.nav.pto.veilarbfilter.JwtUtil.Companion.useJwtFromCookie
import no.nav.pto.veilarbfilter.abac.PepClient
import no.nav.pto.veilarbfilter.config.AuthCookies
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
        jwt("AzureAD") {
            skipWhen { applicationCall -> applicationCall.request.cookies[AuthCookies.AZURE_AD.cookieName] == null }
            realm = "veilarbfilter"
            authHeader { applicationCall ->
                useJwtFromCookie(
                    applicationCall,
                    AuthCookies.AZURE_AD.cookieName
                )
            }
            verifier(configuration.jwt.azureAdJwksUrl)
            validate { JwtUtil.validateJWT(it, configuration.jwt.azureAdClientId) }
        }
        jwt("OpenAM") {
            skipWhen { applicationCall -> applicationCall.request.cookies[AuthCookies.OPEN_AM.cookieName] == null }
            realm = "veilarbfilter"
            authHeader { applicationCall ->
                useJwtFromCookie(
                    applicationCall,
                    AuthCookies.OPEN_AM.cookieName
                )
            }
            verifier(configuration.jwt.issoJwksUrl, configuration.jwt.issoJwtIssuer)
            validate { JwtUtil.validateJWT(it, null) }
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
        LogFilter(EnvironmentUtils.requireApplicationName(), EnvironmentUtils.isDevelopment().orElse(false))
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
                veilederGruppeRoutes(veilederGrupperService, PepClient(config = configuration), useAuthentication)
                mineLagredeFilterRoutes(MineLagredeFilterServiceImpl(), useAuthentication)
            }
        }
    }

    applicationState.initialized = true
}
