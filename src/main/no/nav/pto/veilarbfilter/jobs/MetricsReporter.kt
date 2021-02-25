package no.nav.pto.veilarbfilter.jobs

import kotlinx.coroutines.*
import no.nav.pto.veilarbfilter.service.MetricsReporterService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class MetricsReporter : CoroutineScope {
    private val job = Job()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private var interval: Long = 0
    private var initialDelay: Long = 0
    private var metricsReporterService: MetricsReporterService

    constructor(
        interval: Long,
        initialDelay: Long,
        metricsReporterService: MetricsReporterService
    ) {
        this.interval = interval
        this.initialDelay = initialDelay
        this.metricsReporterService = metricsReporterService
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
            metricsReporterService.reportLagradeFilter()
            delay(interval)
        }
    }


}