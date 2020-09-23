package no.nav.pto.veilarbfilter

import io.ktor.server.engine.*
import no.nav.common.utils.Credentials
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.jobs.CleanupVeilederGrupper
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl

fun mainTest(jdbcUrl: String, dbUsername: String, dbPass: String): ApplicationEngine {
    System.setProperty("NAIS_APP_NAME", "local")

    val configuration = Configuration(
        clustername = "",
        stsDiscoveryUrl = "",
        serviceUser = Credentials("foo", "bar"),
        abac = Configuration.Abac(""),
        veilarbveilederConfig = Configuration.VeilarbveilederConfig(""),
        database = Configuration.DB(
            url = jdbcUrl,
            username = dbUsername,
            password = dbPass
        )
    )

    Database(configuration)
    val applicationState = ApplicationState()

    val veilederGrupperService =
        VeilederGrupperServiceImpl(VeilarbveilederClient(config = configuration, systemUserTokenProvider = null));
    val cleanupVeilederGrupper =
        CleanupVeilederGrupper(
            veilederGrupperService = veilederGrupperService,
            initialDelay = null,
            interval = 100000L
        );

    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration,
        veilederGrupperService = veilederGrupperService,
        useAuthentication = false
    );

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(0, 0)
    })

    cleanupVeilederGrupper.start()
    applicationServer.start(wait = false)
    return applicationServer
}
