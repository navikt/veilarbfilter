package no.nav.pto.veilarbfilter;

import no.nav.pto.veilarbfilter.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Import;

@EnableAutoConfiguration
@ServletComponentScan
@Import(AppConfig.class)
public class TestApp {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TestApp.class);
        application.setAdditionalProfiles("test");
        application.run(args);
    }
}