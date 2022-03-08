package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
class PortefoljeFilterTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void testDeserialization() throws IOException {
        var jsonString =
                "{\"aktiviteter\":{\"BEHANDLING\":\"NA\",\"EGEN\":\"NA\",\"GRUPPEAKTIVITET\":\"NA\",\"IJOBB\":\"NA\",\"MOTE\":\"NA\",\"SOKEAVTALE\":\"NA\",\"STILLING\":\"NA\",\"TILTAK\":\"NA\",\"UTDANNINGAKTIVITET\":\"NA\"},\"alder\":[],\"ferdigfilterListe\":[\"MIN_ARBEIDSLISTE\",\"NYE_BRUKERE_FOR_VEILEDER\",\"PERMITTERTE_ETTER_NIENDE_MARS\"],\"fodselsdagIMnd\":[],\"formidlingsgruppe\":[],\"hovedmal\":[],\"innsatsgruppe\":[],\"kjonn\":null,\"manuellBrukerStatus\":[],\"navnEllerFnrQuery\":\"\",\"rettighetsgruppe\":[],\"servicegruppe\":[],\"tiltakstyper\":[],\"veilederNavnQuery\":\"\",\"veiledere\":[],\"ytelse\":\"DAGPENGER\",\"registreringstype\":[],\"cvJobbprofil\":null,\"arbeidslisteKategori\":[\"BLA\",\"LILLA\"]}";
        PortefoljeFilter filterModel = objectMapper.readValue(jsonString, PortefoljeFilter.class);

        Assertions.assertTrue(filterModel.getArbeidslisteKategori() != null);
        Assertions.assertTrue(filterModel.getArbeidslisteKategori().size() == 2);
    }

    @Test
    public void testDeserializationWithAktiviteter() throws JsonProcessingException {
        String inputJson = "{\"alder\": [], \"kjonn\": null, \"ytelse\": null, \"hovedmal\": [], \"utdanning\": [], \"veiledere\": [], \"aktiviteter\": {\"EGEN\": \"JA\", \"MOTE\": \"NEI\", \"IJOBB\": \"NA\", \"TILTAK\": \"NA\", \"STILLING\": \"NA\", \"BEHANDLING\": \"NA\", \"SOKEAVTALE\": \"NA\", \"GRUPPEAKTIVITET\": \"NA\", \"UTDANNINGAKTIVITET\": \"NA\"}, \"cvJobbprofil\": null, \"tiltakstyper\": [], \"innsatsgruppe\": [], \"servicegruppe\": [], \"fodselsdagIMnd\": [], \"rettighetsgruppe\": [], \"utdanningBestatt\": [], \"ferdigfilterListe\": [], \"formidlingsgruppe\": [], \"navnEllerFnrQuery\": \"\", \"registreringstype\": [], \"utdanningGodkjent\": [], \"veilederNavnQuery\": \"\", \"manuellBrukerStatus\": [], \"arbeidslisteKategori\": [], \"sisteEndringKategori\": [\"FULLFORT_BEHANDLING\"]}";

        PortefoljeFilter filterModel = objectMapper.readValue(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getAktiviteter());
        Assertions.assertNotNull(filterModel.getAktiviteter().getEGEN());
        Assertions.assertTrue(filterModel.getAktiviteter().getEGEN().equals("JA"));
        Assertions.assertTrue(filterModel.getAktiviteter().getMOTE().equals("NEI"));
    }

}