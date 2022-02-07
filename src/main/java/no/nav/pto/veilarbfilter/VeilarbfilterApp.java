package no.nav.pto.veilarbfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.TimeZone;

@SpringBootApplication
@ServletComponentScan
public class VeilarbfilterApp {
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(Optional.ofNullable(System.getenv("TZ")).orElse("Europe/Oslo")));
    }

    public static void main(String... args) {
        SpringApplication.run(VeilarbfilterApp.class, args);
    }

}
