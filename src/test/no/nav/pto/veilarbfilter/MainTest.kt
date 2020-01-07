package no.nav.pto.veilarbfilter

import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.config.Configuration
import java.util.concurrent.TimeUnit

fun main() {

    val configuration = Configuration(
        clustername = "",
        serviceUser = NaisUtils.Credentials("foo", "bar"),
        abac = Configuration.Abac(""),
        veilarbveilederConfig = Configuration.VeilarbveilederConfig("")
    )

    val applicationState = ApplicationState()
    val applicationServer = createHttpServer(applicationState = applicationState, configuration = configuration);

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(5, 5, TimeUnit.SECONDS)
    })

    applicationServer.start(wait = true)
}
