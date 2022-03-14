package no.nav.pto.veilarbfilter.config;

import no.nav.common.abac.Pep;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.utils.Credentials;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import no.nav.pto.veilarbfilter.rest.MineLagredeFilterController;
import no.nav.pto.veilarbfilter.rest.VeilederGruppeController;
import no.nav.pto.veilarbfilter.service.MineLagredeFilterService;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Configuration
@ActiveProfiles({"test"})
@Import({DbConfigTest.class,
        VeilederGruppeFilterRepository.class,
        MineLagredeFilterRepository.class,
        VeilederGrupperService.class,
        MineLagredeFilterService.class,
        MineLagredeFilterController.class,
        VeilederGruppeController.class})
public class AppConfig {
    @MockBean
    public InfluxClient metricsClient;

    @MockBean
    public Credentials serviceUserCredentials;

    @MockBean
    public SystemUserTokenProvider systemUserTokenProvider;

    @Bean
    public Pep pep() {
        Pep mockPep = Mockito.mock(Pep.class);
        Mockito.when(mockPep.harVeilederTilgangTilEnhet(Mockito.any(), Mockito.any())).thenReturn(true);
        return mockPep;
    }

    @Bean
    public VeilarbveilederClient veilarbveilederClient() {
        VeilarbveilederClient mockVeilarbVeilederClient = Mockito.mock(VeilarbveilederClient.class);
        Mockito.when(mockVeilarbVeilederClient.hentVeilederePaaEnhet(EnhetId.of("1"))).thenReturn(List.of("1", "2", "3"));
        return mockVeilarbVeilederClient;
    }
}
