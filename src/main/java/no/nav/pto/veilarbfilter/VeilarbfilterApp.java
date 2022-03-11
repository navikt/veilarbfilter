package no.nav.pto.veilarbfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class VeilarbfilterApp {
    public static void main(String... args) {
        SpringApplication.run(VeilarbfilterApp.class, args);
    }

}
