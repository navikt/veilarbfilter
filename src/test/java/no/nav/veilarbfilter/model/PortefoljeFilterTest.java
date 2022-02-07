package no.nav.veilarbfilter.model;

import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class PortefoljeFilterTest {
    @Test
    public void testDeserialization() throws IOException {
        var jsonString =
                "{\"aktiviteter\":{\"BEHANDLING\":\"NA\",\"EGEN\":\"NA\",\"GRUPPEAKTIVITET\":\"NA\",\"IJOBB\":\"NA\",\"MOTE\":\"NA\",\"SOKEAVTALE\":\"NA\",\"STILLING\":\"NA\",\"TILTAK\":\"NA\",\"UTDANNINGAKTIVITET\":\"NA\"},\"alder\":[],\"ferdigfilterListe\":[\"MIN_ARBEIDSLISTE\",\"NYE_BRUKERE_FOR_VEILEDER\",\"PERMITTERTE_ETTER_NIENDE_MARS\"],\"fodselsdagIMnd\":[],\"formidlingsgruppe\":[],\"hovedmal\":[],\"innsatsgruppe\":[],\"kjonn\":null,\"manuellBrukerStatus\":[],\"navnEllerFnrQuery\":\"\",\"rettighetsgruppe\":[],\"servicegruppe\":[],\"tiltakstyper\":[],\"veilederNavnQuery\":\"\",\"veiledere\":[],\"ytelse\":\"DAGPENGER\",\"registreringstype\":[],\"cvJobbprofil\":null,\"arbeidslisteKategori\":[\"BLA\",\"LILLA\"]}";
        var objectMapper = new ObjectMapper();
        PortefoljeFilter filterModel = objectMapper.readValue(jsonString, PortefoljeFilter.class);

        Assert.assertTrue(filterModel.getArbeidslisteKategori() != null);
        Assert.assertTrue(filterModel.getArbeidslisteKategori().size() == 2);
    }
}
