package no.nav.pto.veilarbfilter

import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl

fun mainTest(configuration: Configuration) {
    System.setProperty("NAIS_APP_NAME", "local")
    Database(configuration)
    val applicationState = ApplicationState()

    val veilederGrupperService = VeilederGrupperServiceImpl(VeilarbveilederClient(config = configuration, systemUserTokenProvider = null));

    val applicationServer = createHttpServer(
            applicationState = applicationState,
            configuration = configuration,
            veilederGrupperService = veilederGrupperService,
            useAuthentication = false
    );

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(5, 5)
    })

    applicationServer.start(wait = false)

}
