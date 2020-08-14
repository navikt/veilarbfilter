package no.nav.pto.veilarbfilter.jobs

import kotlinx.coroutines.*
import no.nav.common.metrics.Event
import no.nav.common.metrics.InfluxClient
import no.nav.common.metrics.MetricsClient
import no.nav.pto.veilarbfilter.service.MineLagredeFilterServiceImpl
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class MetricsReporter : CoroutineScope {
    private val job = Job()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val log = LoggerFactory.getLogger("MetricsReporter")
    private var interval: Long = 0
    private var initialDelay: Long = 0
    private var mineLagredeFilterServiceImpl: MineLagredeFilterServiceImpl
    private var metricsClient: MetricsClient

    constructor(interval: Long,
                initialDelay: Long,
                mineLagredeFilterServiceImpl: MineLagredeFilterServiceImpl) {

        this.interval = interval
        this.initialDelay = initialDelay
        this.mineLagredeFilterServiceImpl = mineLagredeFilterServiceImpl
        metricsClient = InfluxClient()
    }

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay.let {
            delay(it)
        }
        while (isActive) {
            reportLagradeFilter()
            log.info("Metrics reported")
            delay(interval)
        }
    }

    private suspend fun reportLagradeFilter() {
        mineLagredeFilterServiceImpl.hentAllLagredeFilter().forEach {
            val metrikk = Event("portefolje.metrikker.lagredefilter.veileder-filter-counter")
            metrikk.addFieldToReport("id", getHash(it.veilederId))
            metrikk.addFieldToReport("filterId", it.filterId)
            metrikk.addFieldToReport("navn-lengde", it.filterNavn.length)
            val filterValg = it.filterValg
            if (filterValg.aktiviteter != null) {
                metrikk.addTagToReport("aktiviteter", "1")
            }
            if (filterValg.alder.isNotEmpty()) {
                metrikk.addTagToReport("alder", "1")
            }
            if (filterValg.ferdigfilterListe.isNotEmpty()) {
                metrikk.addTagToReport("ferdigfilterListe", "1")
            }
            if (filterValg.fodselsdagIMnd.isNotEmpty()) {
                metrikk.addTagToReport("fodselsdagIMnd", "1")
            }
            if (filterValg.formidlingsgruppe.isNotEmpty()) {
                metrikk.addTagToReport("formidlingsgruppe", "1")
            }
            if (filterValg.hovedmal.isNotEmpty()) {
                metrikk.addTagToReport("hovedmal", "1")
            }
            if (filterValg.innsatsgruppe.isNotEmpty()) {
                metrikk.addTagToReport("innsatsgruppe", "1")
            }
            if (filterValg.kjonn != null && filterValg.kjonn.isNotEmpty()) {
                metrikk.addTagToReport("kjonn", "1")
            }
            if (filterValg.manuellBrukerStatus.isNotEmpty()) {
                metrikk.addTagToReport("manuellBrukerStatus", "1")
            }
            if (filterValg.rettighetsgruppe.isNotEmpty()) {
                metrikk.addTagToReport("rettighetsgruppe", "1")
            }
            if (filterValg.servicegruppe.isNotEmpty()) {
                metrikk.addTagToReport("servicegruppe", "1")
            }
            if (filterValg.tiltakstyper.isNotEmpty()) {
                metrikk.addTagToReport("tiltakstyper", "1")
            }
            if (filterValg.veilederNavnQuery.isNotEmpty()) {
                metrikk.addTagToReport("veilederNavnQuery", "1")
            }
            if (filterValg.veiledere.isNotEmpty()) {
                metrikk.addTagToReport("veiledere", "1")
            }
            if (filterValg.ytelse != null && filterValg.ytelse.isNotEmpty()) {
                metrikk.addTagToReport("ytelse", "1")
            }
            if (filterValg.registreringstype != null && filterValg.registreringstype.isNotEmpty()) {
                metrikk.addTagToReport("registreringstype", "1")
            }
            if (filterValg.cvJobbprofil != null && filterValg.cvJobbprofil.isNotEmpty()) {
                metrikk.addTagToReport("cvJobbprofil", "1")
            }
            if (filterValg.arbeidslisteKategori != null && filterValg.arbeidslisteKategori.isNotEmpty()) {
                metrikk.addTagToReport("arbeidslisteKategori", "1")
            }
            metricsClient.report(metrikk)
        }
    }

    private fun getHash(veilederId: String): String {
        return DigestUtils.md5Hex(veilederId)
    };

}