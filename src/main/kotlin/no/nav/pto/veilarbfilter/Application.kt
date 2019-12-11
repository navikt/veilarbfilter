package no.nav.pto.veilarbfilter

import no.nav.pto.veilarbfilter.config.Configuration
import java.util.concurrent.TimeUnit

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    val configuration = Configuration()
    val applicationState = ApplicationState()

    val applicationServer = createHttpServer(applicationState = applicationState, configuration = configuration);

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(5, 5, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}
