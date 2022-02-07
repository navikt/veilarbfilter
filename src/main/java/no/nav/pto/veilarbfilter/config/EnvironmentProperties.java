package no.nav.pto.veilarbfilter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {
    private String veilarbVeilederUrl;
    private String stsDiscoveryUrl;
    private String dbUrl;
    private String abacUrl;
    private String azureAdDiscoveryUrl;
    private String azureAdClientId;
    private String openAmDiscoveryUrl;
    private String openAmClientId;
    private String openAmRefreshUrl;

}
