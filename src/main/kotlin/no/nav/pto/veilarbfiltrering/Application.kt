package no.nav.pto.veilarbfiltrering

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import org.slf4j.event.Level
import io.ktor.jackson.*
import io.ktor.features.*
import io.ktor.request.receive
import no.nav.pto.veilarbfiltrering.ObjectMapperProvider.Companion.objectMapper
import no.nav.pto.veilarbfiltrering.config.Database
import no.nav.pto.veilarbfiltrering.model.NyttFilterModel
import no.nav.pto.veilarbfiltrering.service.EnhetFilterServiceImpl
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("veilarbfiltrering.Application")


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

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
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }

    Database.init()

    val enhetFilterService = EnhetFilterServiceImpl()

    routing {
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
}
