package no.nav.pto.veilarbfilter;

import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.config.AppConfig;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;


@ContextConfiguration(initializers = AbstractTest.DockerPostgreDataSourceInitializer.class)
@Testcontainers
@Import({AppConfig.class})
@ActiveProfiles({"test"})
public abstract class AbstractTest {
    public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:14.1-alpine");

    static {
        postgreDBContainer.start();

        MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class);
        authUtilsMockedStatic.when(() -> AuthUtils.getInnloggetVeilederIdent())
                .thenReturn(VeilederId.of("1"));
        authUtilsMockedStatic.when(() -> AuthUtils.getInnloggetVeilederUUID(any()))
                .thenReturn(UUID.randomUUID());
    }

    public static class DockerPostgreDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgreDBContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreDBContainer.getUsername(),
                    "spring.datasource.password=" + postgreDBContainer.getPassword()
            );
        }
    }

}
