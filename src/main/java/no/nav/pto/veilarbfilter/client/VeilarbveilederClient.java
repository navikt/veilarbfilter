package no.nav.pto.veilarbfilter.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.pto.veilarbfilter.config.EnvironmentProperties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static no.nav.common.client.utils.CacheUtils.tryCacheFirst;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class VeilarbveilederClient {
    private final String veilarbveilederBaseUrl;
    private final OkHttpClient client;
    private final Cache<EnhetId, List<String>> hentVeilederePaaEnhetCache;
    private final Supplier<String> systemUserTokenProvider;

    @Autowired
    public VeilarbveilederClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        final String appName = "veilarbveileder";
        final String namespace = "pto";
        this.veilarbveilederBaseUrl = properties.getVeilarbveilederUrl() + "/veilarbveileder";
        this.client = RestClient.baseClient();
        systemUserTokenProvider = () ->

                tokenClient.createMachineToMachineToken(String.format("api://%s-fss.%s.%s/.default",
                        (EnvironmentUtils.isProduction().orElseThrow()) ? "prod" : "dev", namespace, appName)
                );

        hentVeilederePaaEnhetCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(600)
                .build();
    }

    public List<String> hentVeilederePaaEnhet(EnhetId enhet) {
        return tryCacheFirst(hentVeilederePaaEnhetCache, enhet,
                () -> hentVeilederePaaEnhetQuery(enhet));
    }

    @SneakyThrows
    private List<String> hentVeilederePaaEnhetQuery(EnhetId enhet) {
        Request request = new Request.Builder()
                .header(AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
                .url(joinPaths(veilarbveilederBaseUrl, "/api/enhet/", enhet.get(), "/identer"))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseArrayOrThrow(response, String.class);
        }
    }
}
