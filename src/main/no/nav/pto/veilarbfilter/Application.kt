package no.nav.pto.veilarbfilter

import no.nav.common.sts.NaisSystemUserTokenProvider
import no.nav.common.utils.SslUtils
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.jobs.CleanupVeilederGrupper
import no.nav.pto.veilarbfilter.jobs.MetricsReporter
import no.nav.pto.veilarbfilter.service.MetricsReporterService
import no.nav.pto.veilarbfilter.service.MineLagredeFilterServiceImpl
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl
import java.util.concurrent.TimeUnit

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)


private val INITIAL_DELAY_CLEANUP = TimeUnit.MINUTES.toMillis(1);
private val INTERVAL_CLEANUP = TimeUnit.MINUTES.toMillis(5);

private val INITIAL_DELAY_METRICS = TimeUnit.MINUTES.toMillis(2);
private val INTERVAL_METRICS_REPORT = TimeUnit.MINUTES.toMillis(5);

fun main() {
    main(Configuration())
}

fun main(configuration: Configuration) {
    SslUtils.setupTruststore();
    Database(configuration)
    val applicationState = ApplicationState()
    val systemUserTokenProvider = NaisSystemUserTokenProvider(
            configuration.stsDiscoveryUrl,
            configuration.serviceUser.username,
            configuration.serviceUser.password
    )
    val veilarbveilederClient = VeilarbveilederClient(
            config = configuration,
            systemUserTokenProvider = systemUserTokenProvider
    )

    val mineLagredeFilterService = MineLagredeFilterServiceImpl();
    val veilederGrupperService =
            VeilederGrupperServiceImpl(veilarbveilederClient, mineLagredeFilterService);
    val metricsReporterService = MetricsReporterService(mineLagredeFilterServiceImpl = mineLagredeFilterService)


    val cleanUpVeilederGrupper = CleanupVeilederGrupper(
            veilederGrupperService = veilederGrupperService,
            mineLagredeFilterService = mineLagredeFilterService,
            initialDelay = INITIAL_DELAY_CLEANUP,
            interval = INTERVAL_CLEANUP
    )

    val metrikker = MetricsReporter(
            initialDelay = INITIAL_DELAY_METRICS,
            interval = INTERVAL_METRICS_REPORT,
            metricsReporterService = metricsReporterService
    )

    val applicationServer = createHttpServer(
            applicationState = applicationState,
            configuration = configuration,
            veilederGrupperService = veilederGrupperService,
            useAuthentication = configuration.useAuthentication
    );

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        cleanUpVeilederGrupper.stop()
        metrikker.stop()

    })

    cleanUpVeilederGrupper.start()
    metrikker.start()
    applicationServer.start(wait = configuration.httpServerWait)

}