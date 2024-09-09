package no.nav.pto.veilarbfilter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.pto.veilarbfilter.service.MetricsReporter;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
@Service
public class ScheduleConfig {
    private final LeaderElectionClient leaderElectionClient;
    private final VeilederGrupperService veilederGrupperService;
    private final MetricsReporter metricsReporter;

    @Scheduled(fixedDelay = 30, initialDelay = 2, timeUnit = TimeUnit.MINUTES)
    public void fjernVeilederSomErIkkeAktive() {
        if (leaderElectionClient.isLeader()) {
            try {
                log.info("Fjern veileder som er ikke aktive...");
                veilederGrupperService.hentAlleEnheter().forEach(veilederGrupperService::slettVeiledereSomIkkeErAktivePaEnheten);
                log.info("Fjern veileder som er ikke aktive er ferdig");
            } catch (Exception e) {
                log.warn("Exception during clanup " + e, e);
            }
        } else {
            log.info("Starter ikke jobb: fjernVeilederSomErIkkeAktive");
        }
    }

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.MINUTES)
    public void reportLagradeFilter() {
        try {
            if (leaderElectionClient.isLeader()) {
                metricsReporter.reportLagradeFilter();
            } else {
                log.info("Starter ikke jobb: reportLagradeFilter");
            }
        } catch (Exception e) {
            log.warn("Exception during metrics reporting " + e, e);
        }
    }
}
