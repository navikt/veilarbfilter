package no.nav.pto.veilarbfilter

import io.ktor.server.engine.ApplicationEngine
import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.config.Database
import no.nav.pto.veilarbfilter.jobs.CleanupVeilederGrupper
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl

class MainTest {

    private var databaseUrl: String
    private val databaseUsername: String
    private val databasePassword: String
    private lateinit var applicationServer: ApplicationEngine

    constructor(databaseUrl: String, databaseUsername: String, databasePassword: String) {
        this.databaseUrl = databaseUrl
        this.databaseUsername = databaseUsername
        this.databasePassword = databasePassword
    }

    fun start() {

        val configuration = Configuration(
            clustername = "",
            serviceUser = NaisUtils.Credentials("foo", "bar"),
            abac = Configuration.Abac(""),
            veilarbveilederConfig = Configuration.VeilarbveilederConfig(""),
            database = Configuration.DB(url = databaseUrl, username = databaseUsername, password = databasePassword)
        )
        Database(configuration)
        val applicationState = ApplicationState()

        val veilederGrupperService = VeilederGrupperServiceImpl(VeilarbveilederClient(config = configuration));
        val cleanupVeilederGrupper = CleanupVeilederGrupper(
            veilederGrupperService = veilederGrupperService,
            initialDelay = 10_000L,
            interval = 10_000L
        );

        this.applicationServer = createHttpServer(
            applicationState = applicationState,
            configuration = configuration,
            veilederGrupperService = veilederGrupperService,
            useAuthentication = false
        );

        Runtime.getRuntime().addShutdownHook(Thread {
            applicationState.initialized = false
            this.applicationServer.stop(5, 5)
        })

        cleanupVeilederGrupper.start()
        this.applicationServer.start(wait = false)
    }

    fun stop() {
        this.applicationServer.stop(5, 5)
    }
}
