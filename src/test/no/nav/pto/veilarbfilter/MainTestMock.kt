package no.nav.pto.veilarbfilter;


import com.nhaarman.mockito_kotlin.withSettings
import io.ktor.server.engine.*
import no.nav.common.utils.Credentials
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.jobs.CleanupVeilederGrupper
import no.nav.pto.veilarbfilter.service.MineLagredeFilterServiceImpl
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy


suspend fun mainTestWithMock(jdbcUrl: String, dbUsername: String, dbPass: String): ApplicationEngine {
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
    val veilarbveilederClient: VeilarbveilederClient =
        mock(VeilarbveilederClient::class.java, withSettings().useConstructor(configuration, null))
    val mineFilterServiceReal = MineLagredeFilterServiceImpl()
    val veilederGrupperServiceReal = VeilederGrupperServiceImpl(veilarbveilederClient, mineFilterServiceReal)
    val veilederGrupperService: VeilederGrupperServiceImpl = spy(veilederGrupperServiceReal)

    val mineFilterService: MineLagredeFilterServiceImpl = spy(mineFilterServiceReal)


    Mockito.`when`(veilarbveilederClient.hentVeilederePaEnheten("1")).thenReturn(listOf("1", "2", "3"))
    Mockito.`when`(veilederGrupperService.hentAlleEnheter()).thenReturn(listOf("1", "2", "3"))

    val cleanupVeilederGrupper =
        CleanupVeilederGrupper(
            veilederGrupperService = veilederGrupperService,
            mineLagredeFilterService = mineFilterService,
            initialDelay = null,
            interval = 500L
        );  // Low interval cleanup to be able to test the function

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

