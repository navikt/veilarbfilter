package no.nav.pto.veilarbfilter.jobs

import kotlinx.coroutines.*
import no.nav.pto.veilarbfilter.service.MineLagredeFilterServiceImpl
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class CleanupVeilederGrupper(
    val veilederGrupperService: VeilederGrupperServiceImpl,
    val mineLagredeFilterService: MineLagredeFilterServiceImpl,
    val interval: Long,
    val initialDelay: Long?
) :
    CoroutineScope {
    private val log = LoggerFactory.getLogger("CleanupVeilederGrupper")

    private val job = Job()

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()


    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay?.let {
            delay(it)
        }
        while (isActive) {
            fjernVeilederSomErIkkeAktive()
            leggeTilEnhetIdTilMineFilter()
            cleanupMineFilter()
            delay(interval)
        }
    }

    private suspend fun fjernVeilederSomErIkkeAktive() {
        try {
            log.info("Fjern veileder som er ikke aktive...")
            veilederGrupperService.hentAlleEnheter().forEach {
                veilederGrupperService.slettVeiledereSomIkkeErAktivePaEnheten(it)
            }
            log.info("Fjern veileder som er ikke aktive er ferdig")
        } catch (e: Exception) {
            log.warn("Exception during cleanup $e", e)
        }
    }

    private suspend fun leggeTilEnhetIdTilMineFilter() {
        try {
            log.info("Legg til enhet id til mine filter...")
            mineLagredeFilterService.leggTilEnhetIdTilMineFilter()
            log.info("Legg til enhet id til mine filter er ferdig")
        } catch (e: Exception) {
            log.warn("Exception during adding enheter id to mine filter $e", e)
        }
    }

    private suspend fun cleanupMineFilter() {
        try {
            log.info("Cleanup mine filter...")
            mineLagredeFilterService.cleanupMineFilter()
            log.info("Cleanup mine filter er ferdig")
        } catch (e: Exception) {
            log.warn("Exception during adding enheter id to mine filter $e", e)
        }
    }
}