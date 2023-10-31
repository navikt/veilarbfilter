package no.nav.pto.veilarbfilter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {
    private String dbUrl;
    private String abacUrl;
    private String naisAadDiscoveryUrl;
    private String naisAadClientId;
    private String poaoTilgangUrl;
    private String poaoTilgangScope;
}
