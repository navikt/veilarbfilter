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
            metrikk.addFieldToReport("navn-lengde", it.filterNavn.length)
            metrikk.addTagToReport("id", getHash(it.veilederId))
            metrikk.addFieldToReport("filterId", it.filterId)
            val filterValg = it.filterValg
            var antallFiltre = 0

            if (filterValg.aktiviteter != null) {
                metrikk.addTagToReport("aktiviteter", "1")
                if (filterValg.aktiviteter.BEHANDLING != "NA") {
                    metrikk.addTagToReport("BEHANDLING", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.EGEN != "NA") {
                    metrikk.addTagToReport("EGEN", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.GRUPPEAKTIVITET != "NA") {
                    metrikk.addTagToReport("GRUPPEAKTIVITET", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.IJOBB != "NA") {
                    metrikk.addTagToReport("IJOBB", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.MOTE != "NA") {
                    metrikk.addTagToReport("MOTE", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.SOKEAVTALE != "NA") {
                    metrikk.addTagToReport("SOKEAVTALE", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.STILLING != "NA") {
                    metrikk.addTagToReport("STILLING", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.TILTAK != "NA") {
                    metrikk.addTagToReport("TILTAK", "1")
                    antallFiltre++
                }
                if (filterValg.aktiviteter.UTDANNINGAKTIVITET != "NA") {
                    metrikk.addTagToReport("UTDANNINGAKTIVITET", "1")
                    antallFiltre++
                }
            }
            if (filterValg.alder.isNotEmpty()) {
                metrikk.addTagToReport("alder", "1")
                antallFiltre += filterValg.alder.size
            }
            if (filterValg.ferdigfilterListe.isNotEmpty()) {
                metrikk.addTagToReport("ferdigfilterListe", "1")
                addValuesAsTags(metrikk, filterValg.ferdigfilterListe)
                antallFiltre += filterValg.ferdigfilterListe.size
            }
            if (filterValg.fodselsdagIMnd.isNotEmpty()) {
                metrikk.addTagToReport("fodselsdagIMnd", "1")
                antallFiltre += filterValg.fodselsdagIMnd.size
            }
            if (filterValg.formidlingsgruppe.isNotEmpty()) {
                metrikk.addTagToReport("formidlingsgruppe", "1")
                addValuesAsTags(metrikk, filterValg.formidlingsgruppe)
                antallFiltre += filterValg.formidlingsgruppe.size
            }
            if (filterValg.hovedmal.isNotEmpty()) {
                metrikk.addTagToReport("hovedmal", "1")
                addValuesAsTags(metrikk, filterValg.hovedmal)
                antallFiltre += filterValg.hovedmal.size
            }
            if (filterValg.innsatsgruppe.isNotEmpty()) {
                metrikk.addTagToReport("innsatsgruppe", "1")
                addValuesAsTags(metrikk, filterValg.innsatsgruppe)
                antallFiltre += filterValg.innsatsgruppe.size
            }
            if (filterValg.kjonn != null && filterValg.kjonn.isNotEmpty()) {
                metrikk.addTagToReport("kjonn", "1")
                antallFiltre++
            }
            if (filterValg.manuellBrukerStatus.isNotEmpty()) {
                metrikk.addTagToReport("manuellBrukerStatus", "1")
                addValuesAsTags(metrikk, filterValg.manuellBrukerStatus)
                antallFiltre += filterValg.manuellBrukerStatus.size
            }
            if (filterValg.rettighetsgruppe.isNotEmpty()) {
                metrikk.addTagToReport("rettighetsgruppe", "1")
                addValuesAsTags(metrikk, filterValg.rettighetsgruppe)
                antallFiltre += filterValg.rettighetsgruppe.size
            }
            if (filterValg.servicegruppe.isNotEmpty()) {
                metrikk.addTagToReport("servicegruppe", "1")
                addValuesAsTags(metrikk, filterValg.servicegruppe)
                antallFiltre += filterValg.servicegruppe.size
            }
            if (filterValg.tiltakstyper.isNotEmpty()) {
                metrikk.addTagToReport("tiltakstyper", "1")
                addValuesAsTags(metrikk, filterValg.tiltakstyper)
                antallFiltre += filterValg.tiltakstyper.size
            }
            if (filterValg.veilederNavnQuery.isNotEmpty()) {
                metrikk.addTagToReport("veilederNavnQuery", "1")
                antallFiltre++
            }
            if (filterValg.veiledere.isNotEmpty()) {
                metrikk.addTagToReport("veiledere", "1")
                antallFiltre += filterValg.veiledere.size
            }
            if (filterValg.ytelse != null && filterValg.ytelse.isNotEmpty()) {
                metrikk.addTagToReport("ytelse", "1")
                antallFiltre++
            }
            if (filterValg.registreringstype != null && filterValg.registreringstype.isNotEmpty()) {
                metrikk.addTagToReport("registreringstype", "1")
                addValuesAsTags(metrikk, filterValg.registreringstype)
                antallFiltre += filterValg.registreringstype.size
            }
            if (filterValg.cvJobbprofil != null && filterValg.cvJobbprofil.isNotEmpty()) {
                metrikk.addTagToReport("cvJobbprofil", "1")
                antallFiltre++
            }
            if (filterValg.arbeidslisteKategori != null && filterValg.arbeidslisteKategori.isNotEmpty()) {
                metrikk.addTagToReport("arbeidslisteKategori", "1")
                antallFiltre += filterValg.arbeidslisteKategori.size
            }
            metrikk.addFieldToReport("antallFiltre", antallFiltre)
            metricsClient.report(metrikk)
        }
    }

    private fun addValuesAsTags(metrikk: Event, listValues: List<String>) {
        listValues.forEach { metrikk.addTagToReport(it, "1") }
    }

    private fun getHash(veilederId: String): String {
        return DigestUtils.md5Hex(veilederId)
    };

}