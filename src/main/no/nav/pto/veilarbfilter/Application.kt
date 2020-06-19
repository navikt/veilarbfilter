package no.nav.pto.veilarbfilter

import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.jobs.CleanupVeilederGrupper
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl
import java.util.concurrent.TimeUnit

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)


private val INITIAL_DELAY = TimeUnit.MINUTES.toMillis(5);
private val INTERVAL = TimeUnit.MINUTES.toMillis(15);

fun main() {

    val configuration = Configuration()
    Database(configuration)
    val applicationState = ApplicationState()

    val veilederGrupperService = VeilederGrupperServiceImpl(VeilarbveilederClient(config = configuration));
    val cleanUpVeilederGrupper = CleanupVeilederGrupper(veilederGrupperService = veilederGrupperService, initialDelay = INITIAL_DELAY, interval = INTERVAL)

    val applicationServer = createHttpServer(applicationState = applicationState, configuration = configuration, veilederGrupperService = veilederGrupperService);

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(5, 5, TimeUnit.SECONDS)
        cleanUpVeilederGrupper.stop()
    })

    applicationServer.start(wait = true)
    cleanUpVeilederGrupper.start()
}