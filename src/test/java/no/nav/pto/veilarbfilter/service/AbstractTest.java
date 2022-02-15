package no.nav.pto.veilarbfilter.service;

import no.nav.pto.veilarbfilter.config.AppConfig;
import no.nav.pto.veilarbfilter.config.DbConfigTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest({"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(initializers = AbstractTest.DockerPostgreDataSourceInitializer.class)
@Testcontainers
@Import({AppConfig.class, DbConfigTest.class})
@ActiveProfiles({"test"})
public abstract class AbstractTest {
    public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:14.1-alpine");

    static {
        postgreDBContainer.start();
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
