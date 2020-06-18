package no.nav.pto.veilarbfilter.service

import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class CleanupVeilederGrupper(val veilederGrupperService: VeilederGrupperService, val interval: Long, val initialDelay: Long?) :
    CoroutineScope {
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
            veilederGrupperService.fjernIkkeAktiveVeiledereIVeilederGruppe()
            delay(interval)
        }
        println("cleanup ferdig")
    }
}