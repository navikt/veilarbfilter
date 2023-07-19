package no.nav.pto.veilarbfilter.config;

import no.nav.common.abac.Pep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.types.identer.EnhetId;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.api.ApiResult;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.repository.MineLagredeChipsRepository;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import no.nav.pto.veilarbfilter.rest.MineChipsController;
import no.nav.pto.veilarbfilter.rest.MineLagredeFilterController;
import no.nav.pto.veilarbfilter.rest.VeilederGruppeController;
import no.nav.pto.veilarbfilter.service.*;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@Configuration
@ActiveProfiles({"test"})
@Import({DbConfigTest.class,
        VeilederGruppeFilterRepository.class,
        MineLagredeFilterRepository.class,
        VeilederGrupperService.class,
        MineLagredeFilterService.class,
        MineLagredeFilterController.class,
        VeilederGruppeController.class,
        UnleashService.class, ChipsServiceImpl.class, MineLagredeChipsRepository.class, MineChipsController.class})
public class AppConfig {
    @MockBean
    public InfluxClient metricsClient;

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public Pep pep() {
        Pep mockPep = mock(Pep.class);
        Mockito.when(mockPep.harVeilederTilgangTilEnhet(any(), any())).thenReturn(true);
        return mockPep;
    }
    @Bean
    public PoaoTilgangClient poaoTilgangClient() {
        PoaoTilgangClient mockPoaoTilgangClient = mock(PoaoTilgangClient.class);
        Mockito.when(mockPoaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));
        return mockPoaoTilgangClient; }

    @Bean
    public VeilarbveilederClient veilarbveilederClient() {
        VeilarbveilederClient mockVeilarbVeilederClient = mock(VeilarbveilederClient.class);
        Mockito.when(mockVeilarbVeilederClient.hentVeilederePaaEnhet(EnhetId.of("1"))).thenReturn(List.of("1", "2", "3"));
        return mockVeilarbVeilederClient;
    }

    @Bean
    public UnleashClient unleashClient() {
        UnleashClient mockUnleashClient = mock(UnleashClient.class);
        Mockito.when(mockUnleashClient.isEnabled(any())).thenReturn(true);
        return mockUnleashClient;
    }
}
