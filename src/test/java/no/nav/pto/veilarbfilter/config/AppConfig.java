package no.nav.pto.veilarbfilter.config;

import no.nav.common.metrics.InfluxClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.Credentials;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@Import(DbConfigTest.class)
@ActiveProfiles({"test"})
public class AppConfig {
    @MockBean
    public InfluxClient metricsClient;

    @MockBean
    public Credentials serviceUserCredentials;

    @MockBean
    public SystemUserTokenProvider systemUserTokenProvider;

    @Bean
    VeilederGruppeFilterRepository veilederGruppeFilterRepository(JdbcTemplate db, MineLagredeFilterRepository mineLagredeFilterRepository) {
        return new VeilederGruppeFilterRepository(db, mineLagredeFilterRepository);
    }
}
