package no.nav.pto.veilarbfilter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.pto.veilarbfilter.service.MetricsReporter;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig implements SchedulingConfigurer {
    private final LeaderElectionClient leaderElectionClient;
    private final VeilederGrupperService veilederGrupperService;
    private final MetricsReporter metricsReporter;

    @Scheduled(cron = "30 * * * * ?")
    public void fjernVeilederSomErIkkeAktive() {
        if (leaderElectionClient.isLeader()) {
            try {
                log.info("Fjern veileder som er ikke aktive...");
                veilederGrupperService.hentAlleEnheter().stream().forEach(enhetId -> veilederGrupperService.slettVeiledereSomIkkeErAktivePaEnheten(enhetId));
                log.info("Fjern veileder som er ikke aktive er ferdig");
            } catch (Exception e) {
                log.warn("Exception during clanup " + e, e);
            }
        } else {
            log.info("Starter ikke jobb: fjernVeilederSomErIkkeAktive");
        }
    }

    @Scheduled(cron = "10 * * * * ?")
    public void reportLagradeFilter() {
        if (leaderElectionClient.isLeader()) {
            metricsReporter.reportLagradeFilter();
        } else {
            log.info("Starter ikke jobb: reportLagradeFilter");
        }
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(20);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

}
