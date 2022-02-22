package no.nav.pto.veilarbfilter.config;

import no.nav.common.metrics.InfluxClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.utils.Credentials;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import no.nav.pto.veilarbfilter.rest.MineLagredeFilter;
import no.nav.pto.veilarbfilter.service.MineLagredeFilterService;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@TestConfiguration
@ActiveProfiles({"Test"})
public class AppConfig {
    @MockBean
    public InfluxClient metricsClient;

    @MockBean
    public Credentials serviceUserCredentials;

    @MockBean
    public SystemUserTokenProvider systemUserTokenProvider;

    @Bean
    public VeilarbveilederClient veilarbveilederClient() {
        VeilarbveilederClient mockVeilarbVeilederClient = Mockito.mock(VeilarbveilederClient.class);
        Mockito.when(mockVeilarbVeilederClient.hentVeilederePaaEnhet(EnhetId.of("1"))).thenReturn(List.of("1", "2", "3"));
        return mockVeilarbVeilederClient;
    }


    @Bean
    public VeilederGruppeFilterRepository veilederGruppeFilterRepository(JdbcTemplate db, MineLagredeFilterRepository mineLagredeFilterRepository) {
        return new VeilederGruppeFilterRepository(db, mineLagredeFilterRepository);
    }

    @Bean
    public VeilederGrupperService veilederGrupperService(VeilederGruppeFilterRepository veilederGruppeFilterRepository, VeilarbveilederClient veilarbveilederClient) {
        return new VeilederGrupperService(veilederGruppeFilterRepository, veilarbveilederClient);
    }

    @Bean
    public MineLagredeFilterRepository mineLagredeFilterRepository(JdbcTemplate db) {
        return new MineLagredeFilterRepository(db);
    }

    @Bean
    public MineLagredeFilterService mineLagredeFilterService(MineLagredeFilterRepository mineLagredeFilterRepository) {
        return new MineLagredeFilterService(mineLagredeFilterRepository);
    }

    @Bean
    public MineLagredeFilter mineLagredeFilter(MineLagredeFilterService mineLagredeFilterService) {
        return new MineLagredeFilter(mineLagredeFilterService);
    }
    
}
