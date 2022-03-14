package no.nav.pto.veilarbfilter.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbfilter.config.EnvironmentProperties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static no.nav.common.client.utils.CacheUtils.tryCacheFirst;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service

public class VeilarbveilederClient {
    private SystemUserTokenProvider systemUserTokenProvider;
    private String url;
    private OkHttpClient client;
    private Cache<EnhetId, List<String>> hentVeilederePaaEnhetCache;

    public VeilarbveilederClient(EnvironmentProperties environmentProperties, SystemUserTokenProvider systemUserTokenProvider) {
        this.url = environmentProperties.getVeilarbVeilederUrl();
        this.client = RestClient.baseClient();
        this.systemUserTokenProvider = systemUserTokenProvider;
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
        String path = format("/enhet/%s/identer", enhet);

        Request request = new Request.Builder()
                .header(AUTHORIZATION, "Bearer " + systemUserTokenProvider.getSystemUserToken())
                .url(url + path)
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseArrayOrThrow(response, String.class);
        }
    }
}
