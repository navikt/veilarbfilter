package no.nav.pto.veilarbfilter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.pto.veilarbfilter.service.MetricsReporter;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import no.nav.pto.veilarbfilter.util.SecureLogUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
@Service
public class ScheduleConfig {
    private final LeaderElectionClient leaderElectionClient;
    private final VeilederGrupperService veilederGrupperService;
    private final MetricsReporter metricsReporter;
    private final JdbcTemplate jdbcTemplate;

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

    @Scheduled(cron = "0 0 2 16 May *")
    public void migrerTrengerVurderingTilTrengerOppfolgingsvedtak() {
        String jobbNavn = "Migrer TRENGER_VURDERING til TRENGER_OPPFOLGINGSVEDTAK";
        
        //language=postgresql
        String antallSql = """
                select count(*) from filter
                where valgte_filter->'ferdigfilterListe' @> '["TRENGER_VURDERING"]'::jsonb;
                """;

        //language=postgresql
        String migrerSql = """
                UPDATE filter
                SET valgte_filter = replace(valgte_filter::text, '"TRENGER_VURDERING"', '"TRENGER_OPPFOLGINGSVEDTAK"')::jsonb
                where valgte_filter->'ferdigfilterListe' @> '["TRENGER_VURDERING"]'::jsonb;
                """;

        if (leaderElectionClient.isLeader()) {
            log.info("Jobb starter: {}", jobbNavn);
            try {
                Optional.ofNullable(jdbcTemplate.queryForObject(antallSql, Integer.class)).ifPresent(antall ->
                        log.info("Antall filter med TRENGER_VURDERING før migrering: {}.", antall)
                );

                jdbcTemplate.update(migrerSql);
            } catch (RuntimeException e) {
                log.error("Jobb feilet: {}. Se SecureLogs for stacktrace.", jobbNavn);
                SecureLogUtils.secureLog.error(String.format("Jobb feilet: %s.", jobbNavn), e);
            }

            try {
                Optional.ofNullable(jdbcTemplate.queryForObject(antallSql, Integer.class)).ifPresent(antall ->
                        log.info("Antall filter med TRENGER_VURDERING etter migrering: {}.", antall)
                );
            } catch (RuntimeException e) {
                return;
            }

            log.info("Jobb fullført: {}", jobbNavn);
        }
    }
}
