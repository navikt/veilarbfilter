package no.nav.pto.veilarbfilter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import no.nav.common.abac.VeilarbPepFactory;
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.Credentials;
import no.nav.pto.veilarbfilter.auth.ModiaPep;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import no.nav.pto.veilarbfilter.rest.MineLagredeFilter;
import no.nav.pto.veilarbfilter.rest.RestResponseEntityExceptionHandler;
import no.nav.pto.veilarbfilter.rest.VeilederGruppe;
import no.nav.pto.veilarbfilter.service.MineLagredeFilterService;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;


@Configuration
@Import(DbConfigPostgres.class)
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    @Bean
    public LockProvider lockProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcTemplateLockProvider(jdbcTemplate);
    }

    @Bean
    public Credentials serviceUserCredentials() {
        return new Credentials("username", "password");
    }

    @Bean
    public SystemUserTokenProvider systemUserTokenProvider(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return new NaisSystemUserTokenProvider(properties.getStsDiscoveryUrl(), serviceUserCredentials.username, serviceUserCredentials.password);
    }

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public ModiaPep modiaPep(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        var pep = VeilarbPepFactory.get(
                properties.getAbacModiaUrl(), serviceUserCredentials.username,
                serviceUserCredentials.password, new SpringAuditRequestInfoSupplier()
        );

        return new ModiaPep(pep);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Bean
    public VeilederGruppeFilterRepository veilederGruppeFilterRepository(JdbcTemplate db, MineLagredeFilterRepository mineLagredeFilterRepository, ObjectMapper objectMapper) {
        return new VeilederGruppeFilterRepository(db, mineLagredeFilterRepository, objectMapper);
    }

    @Bean
    public VeilederGrupperService veilederGrupperService(VeilederGruppeFilterRepository veilederGruppeFilterRepository, VeilarbveilederClient veilarbveilederClient) {
        return new VeilederGrupperService(veilederGruppeFilterRepository, veilarbveilederClient);
    }

    @Bean
    public MineLagredeFilterRepository mineLagredeFilterRepository(JdbcTemplate db, ObjectMapper objectMapper) {
        return new MineLagredeFilterRepository(db, objectMapper);
    }

    @Bean
    public MineLagredeFilterService mineLagredeFilterService(MineLagredeFilterRepository mineLagredeFilterRepository) {
        return new MineLagredeFilterService(mineLagredeFilterRepository);
    }

    @Bean
    public RestResponseEntityExceptionHandler restResponseEntityExceptionHandler() {
        return new RestResponseEntityExceptionHandler();
    }

    @Bean
    public MineLagredeFilter mineLagredeFilter(MineLagredeFilterService mineLagredeFilterService) {
        return new MineLagredeFilter(mineLagredeFilterService);
    }

    @Bean
    public VeilederGruppe veilederGruppe(VeilederGrupperService veilederGrupperService) {
        return new VeilederGruppe(veilederGrupperService);
    }
}
