package no.nav.pto.veilarbfilter.jobs

import kotlinx.coroutines.*
import no.nav.pto.veilarbfilter.service.VeilederGrupperServiceImpl
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class CleanupVeilederGrupper(val veilederGrupperService: VeilederGrupperServiceImpl, val interval: Long, val initialDelay: Long?) :
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
            delay(interval)
        }
        println("cleanup ferdig")
    }
}