package no.nav.pto.veilarbfilter.service;

import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.veilarbfilter.util.SingletonPostgresContainer;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

class VeilederGrupperServiceTest {
    private final JdbcTemplate db;
    private final VeilederGrupperService veilederGrupperService;

    @MockBean
    VeilarbveilederClient veilarbveilederClient = Mockito.mock(VeilarbveilederClient.class);

    @MockBean
    MineLagredeFilterService mineLagredeFilterService = Mockito.mock(MineLagredeFilterService.class);

    public VeilederGrupperServiceTest() {
        db = SingletonPostgresContainer.init().createJdbcTemplate();
        veilederGrupperService = new VeilederGrupperService(db);
    }

    @Before
    public void setUp() {
        Mockito.when(veilarbveilederClient.hentVeilederePaaEnhet(EnhetId.of("1"))).thenReturn(List.of("1", "2", "3"));
    }
    

}