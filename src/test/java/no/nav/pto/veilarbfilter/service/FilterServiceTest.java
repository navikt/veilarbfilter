package no.nav.pto.veilarbfilter.service;


import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.UtdaterteRegistreringstyper;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WebMvcTest
@ActiveProfiles({"test"})
public class FilterServiceTest extends AbstractTest {
    @Autowired
    private MineLagredeFilterRepository mineLagredeFilterRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE filter CASCADE");
    }

    @Test
    void skal_få_ut_samme_registreringstyper_som_i_databasen_ved_henting() {
        // Given
        String veilederId = "Z111111";

        PortefoljeFilter filterMedRegistreringstyper = new PortefoljeFilter();
        filterMedRegistreringstyper.setRegistreringstype(List.of(
                UtdaterteRegistreringstyper.MISTET_JOBBEN.name(),
                UtdaterteRegistreringstyper.JOBB_OVER_2_AAR.name(),
                UtdaterteRegistreringstyper.VIL_FORTSETTE_I_JOBB.name()
        ));


        int filterId = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("Filter med registreringstyper", filterMedRegistreringstyper)).get().getFilterId();

        // When
        FilterModel hentetFilter = mineLagredeFilterRepository.hentFilter(filterId, false).get();

        // Then
        assertThat(hentetFilter.getFilterValg().getRegistreringstype().size()).isEqualTo(3);

        assertThat(hentetFilter.getFilterValg().getRegistreringstype()).isEqualTo(filterMedRegistreringstyper.getRegistreringstype());
        assertThat(hentetFilter.getFilterValg()).isEqualTo(filterMedRegistreringstyper);
    }
}
