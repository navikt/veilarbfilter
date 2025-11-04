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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;


@ContextConfiguration(initializers = AbstractTest.DockerPostgreDataSourceInitializer.class)
@Testcontainers
@Import({AppConfig.class})
@ActiveProfiles({"test"})
public abstract class AbstractTest {
    public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:14.1-alpine");
    public static final String TEST_VEILEDER_ID = "Z999999";
    public static final UUID TEST_VEILEDER_UUID = UUID.fromString("cb91d58b-a01c-4d3a-a28e-2e8c160425cb");

    static {
        postgreDBContainer.setWaitStrategy(Wait.defaultWaitStrategy());
        postgreDBContainer.start();

        MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class);
        authUtilsMockedStatic.when(AuthUtils::getInnloggetVeilederIdent)
                .thenReturn(VeilederId.of(TEST_VEILEDER_ID));
        authUtilsMockedStatic.when(() -> AuthUtils.getInnloggetVeilederUUID(any()))
                .thenReturn(TEST_VEILEDER_UUID);
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
