package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class MineLagredeFilterModelTest {

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
    public void testDeserialization() throws JsonProcessingException {
        String inputJson = "{\"alder\": [], \"kjonn\": null, \"ytelse\": null, \"hovedmal\": [], \"utdanning\": [], \"veiledere\": [], \"aktiviteter\": {\"EGEN\": \"NA\", \"MOTE\": \"NA\", \"IJOBB\": \"NA\", \"TILTAK\": \"NA\", \"STILLING\": \"NA\", \"BEHANDLING\": \"NA\", \"SOKEAVTALE\": \"NA\", \"GRUPPEAKTIVITET\": \"NA\", \"UTDANNINGAKTIVITET\": \"NA\"}, \"cvJobbprofil\": null, \"tiltakstyper\": [], \"innsatsgruppe\": [], \"servicegruppe\": [], \"fodselsdagIMnd\": [], \"rettighetsgruppe\": [], \"utdanningBestatt\": [], \"ferdigfilterListe\": [], \"formidlingsgruppe\": [], \"navnEllerFnrQuery\": \"\", \"registreringstype\": [], \"utdanningGodkjent\": [], \"veilederNavnQuery\": \"\", \"manuellBrukerStatus\": [], \"arbeidslisteKategori\": [], \"sisteEndringKategori\": [\"FULLFORT_BEHANDLING\"]}";

        MineLagredeFilterModel mineLagredeFilterModel = objectMapper.readValue(inputJson, MineLagredeFilterModel.class);

        Assertions.assertNotNull(mineLagredeFilterModel);
        Assertions.assertNotNull(mineLagredeFilterModel.getFilterValg().getAktiviteter());
        Assertions.assertNotNull(mineLagredeFilterModel.getFilterValg().getSisteEndringKategori());
    }

}