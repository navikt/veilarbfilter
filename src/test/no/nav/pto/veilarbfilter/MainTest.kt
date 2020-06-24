package no.nav.pto.veilarbfilter

import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.jobs.CleanupVeilederGrupper
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl

fun main() {

    val configuration = Configuration(
            clustername = "",
            serviceUser = NaisUtils.Credentials("foo", "bar"),
            abac = Configuration.Abac(""),
            veilarbveilederConfig = Configuration.VeilarbveilederConfig("")
    )
    Database(configuration)
    val applicationState = ApplicationState()

    val veilederGrupperService = VeilederGrupperServiceImpl(VeilarbveilederClient(config = configuration));
    val cleanupVeilederGrupper = CleanupVeilederGrupper(veilederGrupperService = veilederGrupperService, initialDelay = 10_000L, interval = 10_000L);

    val applicationServer = createHttpServer(applicationState = applicationState, configuration = configuration, veilederGrupperService = veilederGrupperService, useAuthentication = false);

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(5, 5)
    })

    cleanupVeilederGrupper.start()
    applicationServer.start(wait = true)

}
