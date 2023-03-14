package no.nav.pto.veilarbfilter.config;

import no.nav.common.auth.oidc.filter.*;
import no.nav.common.rest.filter.JavaxLogRequestFilter;
import no.nav.common.rest.filter.JavaxSetStandardHttpHeadersFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.auth.oidc.filter.JavaxOidcAuthenticator.fromConfigs;
import static no.nav.common.utils.EnvironmentUtils.isDevelopment;
import static no.nav.common.utils.EnvironmentUtils.requireApplicationName;

@Configuration
public class FilterConfig {
    public JavaxOidcAuthenticatorConfig azureAdAuthConfig(EnvironmentProperties properties) {
        return new JavaxOidcAuthenticatorConfig()
                .withDiscoveryUrl(properties.getNaisAadDiscoveryUrl())
                .withClientId(properties.getNaisAadClientId())
                .withUserRoleResolver(new AzureAdUserRoleResolver());
    }

    @Bean
    public FilterRegistrationBean<JavaxOidcAuthenticationFilter> authenticationFilterRegistrationBean(EnvironmentProperties properties) {
        FilterRegistrationBean<JavaxOidcAuthenticationFilter> registration = new FilterRegistrationBean<>();
        JavaxOidcAuthenticationFilter authenticationFilter = new JavaxOidcAuthenticationFilter(
                fromConfigs(
                        azureAdAuthConfig(properties)
                )
        );
        registration.setFilter(authenticationFilter);
        registration.setOrder(1);
        registration.addUrlPatterns("/api/*");
        return registration;
    }


    @Bean
    public FilterRegistrationBean<JavaxLogRequestFilter>  logFilterRegistrationBean() {
        FilterRegistrationBean<JavaxLogRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JavaxLogRequestFilter(requireApplicationName(), isDevelopment().orElse(false)));
        registration.setOrder(2);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JavaxSetStandardHttpHeadersFilter> setStandardHeadersFilterRegistrationBean() {
        FilterRegistrationBean<JavaxSetStandardHttpHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JavaxSetStandardHttpHeadersFilter());
        registration.setOrder(3);
        registration.addUrlPatterns("/*");
        return registration;
    }

}
