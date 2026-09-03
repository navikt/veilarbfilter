package no.nav.pto.veilarbfilter.config;

import io.getunleash.DefaultUnleash;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.json.DateModule;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.api.ApiResult;
import no.nav.pto.veilarbfilter.repository.OverblikkVisningRepository;
import no.nav.pto.veilarbfilter.rest.OverblikkVisningController;
import no.nav.pto.veilarbfilter.service.OverblikkVisningService;
import org.mockito.Mockito;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ActiveProfiles({"test"})
@Import({DbConfigTest.class,
        OverblikkVisningService.class,
        OverblikkVisningRepository.class,
        OverblikkVisningController.class})
public class AppConfig {

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public PoaoTilgangClient poaoTilgangClient() {
        PoaoTilgangClient mockPoaoTilgangClient = mock(PoaoTilgangClient.class);
        Mockito.when(mockPoaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));
        return mockPoaoTilgangClient;
    }

    @Bean
    public LeaderElectionClient leaderElectionClient() {
        LeaderElectionClient mockLeaderElectionClient = mock(LeaderElectionClient.class);
        when(mockLeaderElectionClient.isLeader()).thenReturn(true);
        return mockLeaderElectionClient;
    }

    @Bean
    public DefaultUnleash defaultUnleash() {
        final DefaultUnleash mock = mock(DefaultUnleash.class);
        when(mock.isEnabled(anyString())).thenReturn(true);

        return mock;
    }

    @Bean
    public JsonMapperBuilderCustomizer navCommonJacksonCustomizer() {
        return builder -> builder
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true)
                .addModule(DateModule.module());
    }
}
