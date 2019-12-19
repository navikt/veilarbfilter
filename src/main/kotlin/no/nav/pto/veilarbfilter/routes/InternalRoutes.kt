package no.nav.pto.veilarbfilter.routes

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.sbl.util.LogUtils

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
            val loggers = LogUtils.getAllLoggers()
            val rootLevel = loggers.get(0).level

            val loggerAndLevel = loggers
                .filter { logger -> !logger.effectiveLevel.equals(LogUtils.getRootLevel()) }
                .map { logger -> "<div> ${logger.name} - ${logger.effectiveLevel} </div>" }

            call.respondText {
                """
                    <html>
                        <head>
                        </head>
                        <body>
                            <h1>ROOT log level: $rootLevel </h1>
                            <h1>Loggere med annen log level enn root</h1>
                            ${loggerAndLevel}
                        </body>
                    </html>
                """.trimIndent()
            }
        }
    }
}
