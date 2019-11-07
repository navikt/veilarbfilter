package no.nav.pto.veilarbfilter

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import no.nav.pto.veilarbfilter.config.Configuration

private val logger = LoggerFactory.getLogger("veilarbfilter.Application")
data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    val configuration = Configuration()
    val applicationState = ApplicationState()

    val applicationServer = createHttpServer(applicationState = applicationState, configuration = configuration);

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Shutdown hook called, shutting down gracefully")
        applicationState.initialized = false
        applicationServer.stop(5, 5, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}