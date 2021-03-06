package no.nav.pto.veilarbfilter.routes

import ch.qos.logback.classic.LoggerContext
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text.Html
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.slf4j.LoggerFactory


fun Route.internalRoutes(
    readinessCheck: () -> Boolean,
    livenessCheck: () -> Boolean = { true },
    collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
) {
    route("/internal") {

        get("/isAlive") {
            if (livenessCheck()) {
                call.respondText("Alive")
            } else {
                call.respondText("Not alive", status = HttpStatusCode.InternalServerError)
            }
        }

        get("/isReady") {
            if (readinessCheck()) {
                call.respondText("Ready")
            } else {
                call.respondText("Not ready", status = HttpStatusCode.InternalServerError)
            }
        }

        get("/metrics") {
            val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
            }
        }

        get("/loginfo") {
            val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext;
            val loggers =  loggerContext.loggerList
            val rootLevel = loggers.get(0).level

            val loggerAndLevel = loggers
                .filter { logger -> logger.effectiveLevel != loggerContext.getLogger("ROOT").level }
                .map { logger -> "<div> ${logger.name} - ${logger.effectiveLevel} </div>" }
                .joinToString { " " }

            call.respondText(contentType = Html) {
                """
                    <html>
                        <head>
                        </head>
                        <body>
                            <h1>ROOT log level: $rootLevel </h1>
                            <h1>Loggere med annen log level enn root</h1>
                            $loggerAndLevel
                        </body>
                    </html>
                """.trimIndent()
            }
        }
    }
}
