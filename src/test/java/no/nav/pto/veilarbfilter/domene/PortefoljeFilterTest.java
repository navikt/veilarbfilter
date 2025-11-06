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
import java.util.List;

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
                """
                                 {"aktiviteter":{"BEHANDLING":"NA","EGEN":"NA","GRUPPEAKTIVITET":"NA","IJOBB":"NA","MOTE":"NA","SOKEAVTALE":"NA","STILLING":"NA","TILTAK":"NA","UTDANNINGAKTIVITET":"NA"},"alder":[],"ferdigfilterListe":["NYE_BRUKERE_FOR_VEILEDER","PERMITTERTE_ETTER_NIENDE_MARS"],"fodselsdagIMnd":[],"formidlingsgruppe":[],"hovedmal":[],"innsatsgruppe":[],"kjonn":null,"manuellBrukerStatus":[],"navnEllerFnrQuery":"","rettighetsgruppe":[],"servicegruppe":[],"tiltakstyper":[],"veilederNavnQuery":"","veiledere":[],"ytelse":"DAGPENGER","registreringstype":[],"cvJobbprofil":null}"
                        """;
        PortefoljeFilter filterModel = objectMapper.readValue(jsonString, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel.getFerdigfilterListe());
        Assertions.assertEquals(2, filterModel.getFerdigfilterListe().size());
    }


    @Test
    public void testDeserializationWithAktivitet() throws JsonProcessingException {
        String inputJson = """
                 {"alder": [],"kjonn": null,"ytelse": null,"hovedmal": [],"utdanning": [],"veiledere": [],"aktiviteter": {"EGEN":"JA","MOTE":"NEI","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"innsatsgruppe": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"utdanningBestatt": [],"ferdigfilterListe": [],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"utdanningGodkjent": [],"veilederNavnQuery":"","manuellBrukerStatus": [],"sisteEndringKategori": ["FULLFORT_BEHANDLING"]}
                """;

        PortefoljeFilter filterModel = objectMapper.readValue(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getAktiviteter());
        Assertions.assertNotNull(filterModel.getAktiviteter().getEGEN());
        Assertions.assertEquals("JA", filterModel.getAktiviteter().getEGEN());
        Assertions.assertEquals("NEI", filterModel.getAktiviteter().getMOTE());
    }

    @Test
    public void testDeserializationWithSisteEndringKategori() throws JsonProcessingException {
        String inputJson = """
                 {"alder": [],"kjonn": null,"ytelse": null,"hovedmal": [],"utdanning": [],"veiledere": [],"aktiviteter": {"EGEN":"NA","MOTE":"NA","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"innsatsgruppe": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"utdanningBestatt": [],"ferdigfilterListe": [],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"utdanningGodkjent": [],"veilederNavnQuery":"","manuellBrukerStatus": [],"sisteEndringKategori": ["FULLFORT_BEHANDLING"]}
                """;

        PortefoljeFilter filterModel = objectMapper.readValue(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getSisteEndringKategori());
        Assertions.assertTrue(filterModel.getSisteEndringKategori().contains("FULLFORT_BEHANDLING"));
    }

    @Test
    public void testDeserializationWithFerdigfilterListe() throws JsonProcessingException {
        String inputJson = """
                {"alder": [],"kjonn": null,"ytelse": null,"hovedmal": [],"veiledere": [],"aktiviteter": {"EGEN":"NA","MOTE":"NA","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"innsatsgruppe": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"ferdigfilterListe": ["TRENGER_VURDERING","UFORDELTE_BRUKERE"],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"veilederNavnQuery":"","manuellBrukerStatus": []}""";

        PortefoljeFilter filterModel = objectMapper.readValue(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getFerdigfilterListe());
        Assertions.assertTrue(filterModel.getFerdigfilterListe().contains("TRENGER_VURDERING"));
    }

    @Test
    public void testDeserializationAndSettingDefaultValue() throws JsonProcessingException {
        String inputJson = """
                 {"alder": [],"kjonn": null,"ytelse": null,"hovedmal": [],"veiledere": [],"aktiviteter": {"EGEN":"NA","MOTE":"NA","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"innsatsgruppe": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"ferdigfilterListe": ["TRENGER_VURDERING","UFORDELTE_BRUKERE"],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"veilederNavnQuery":"","manuellBrukerStatus": []}
                """;

        PortefoljeFilter filterModel = objectMapper.readValue(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel.getAktiviteterForenklet());
        Assertions.assertEquals(filterModel.getAktiviteterForenklet().size(), 0);
    }

    @Test
    public void testSerializationOfEmptyFilter() throws JsonProcessingException {
        String correctOutput = """
                {"aktiviteter":null,"aktiviteterForenklet":[],"alder":[],"avvik14aVedtak":[],"barnUnder18Aar":[],"barnUnder18AarAlder":[],"cvJobbprofil":"","ensligeForsorgere":[],"fargekategorier":[],"ferdigfilterListe":[],"fodselsdagIMnd":[],"foedeland":[],"formidlingsgruppe":[],"geografiskBosted":[],"gjeldendeVedtak14a":[],"hovedmal":[],"hovedmalGjeldendeVedtak14a":[],"innsatsgruppe":[],"innsatsgruppeGjeldendeVedtak14a":[],"kjonn":"","landgruppe":[],"manuellBrukerStatus":[],"navnEllerFnrQuery":"","registreringstype":[],"rettighetsgruppe":[],"servicegruppe":[],"sisteEndringKategori":[],"stillingFraNavFilter":[],"tiltakstyper":[],"tolkBehovSpraak":[],"tolkebehov":[],"ulesteEndringer":"","utdanning":[],"utdanningBestatt":[],"utdanningGodkjent":[],"veilederNavnQuery":"","veiledere":[],"visGeografiskBosted":[],"ytelse":"","ytelseAapArena":[],"ytelseAapKelvin":[],"ytelseTiltakspenger":[],"ytelseTiltakspengerArena":[]}""";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        String jsonString = objectMapper.writeValueAsString(portefoljeFilter);
        Assertions.assertEquals(jsonString, correctOutput);
    }

    @Test
    public void testSerializationOfVeiledere() throws JsonProcessingException {
        String correctOutput = """
                {"aktiviteter":null,"aktiviteterForenklet":null,"alder":null,"avvik14aVedtak":null,"barnUnder18Aar":null,"barnUnder18AarAlder":null,"cvJobbprofil":null,"ensligeForsorgere":null,"fargekategorier":null,"ferdigfilterListe":null,"fodselsdagIMnd":null,"foedeland":null,"formidlingsgruppe":null,"geografiskBosted":null,"gjeldendeVedtak14a":null,"hovedmal":null,"hovedmalGjeldendeVedtak14a":null,"innsatsgruppe":null,"innsatsgruppeGjeldendeVedtak14a":null,"kjonn":null,"landgruppe":null,"manuellBrukerStatus":null,"navnEllerFnrQuery":null,"registreringstype":null,"rettighetsgruppe":null,"servicegruppe":null,"sisteEndringKategori":null,"stillingFraNavFilter":null,"tiltakstyper":null,"tolkBehovSpraak":null,"tolkebehov":null,"ulesteEndringer":null,"utdanning":null,"utdanningBestatt":null,"utdanningGodkjent":null,"veilederNavnQuery":null,"veiledere":["A123","B123"],"visGeografiskBosted":null,"ytelse":null,"ytelseAapArena":null,"ytelseAapKelvin":null,"ytelseTiltakspenger":null,"ytelseTiltakspengerArena":null}""";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, List.of("A123", "B123"), null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        String jsonString = objectMapper.writeValueAsString(portefoljeFilter);
        Assertions.assertEquals(jsonString, correctOutput);
    }

}
