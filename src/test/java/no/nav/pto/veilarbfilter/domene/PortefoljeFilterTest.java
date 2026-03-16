package no.nav.pto.veilarbfilter.domene;

import no.nav.common.json.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
class PortefoljeFilterTest {

    @Test
    public void testDeserialization() throws IOException {
        var jsonString =
                """
                                 {"aktiviteter":{"BEHANDLING":"NA","EGEN":"NA","GRUPPEAKTIVITET":"NA","IJOBB":"NA","MOTE":"NA","SOKEAVTALE":"NA","STILLING":"NA","TILTAK":"NA","UTDANNINGAKTIVITET":"NA"},"alder":[],"ferdigfilterListe":["NYE_BRUKERE_FOR_VEILEDER","PERMITTERTE_ETTER_NIENDE_MARS"],"fodselsdagIMnd":[],"formidlingsgruppe":[],"kjonn":null,"manuellBrukerStatus":[],"navnEllerFnrQuery":"","rettighetsgruppe":[],"servicegruppe":[],"tiltakstyper":[],"veilederNavnQuery":"","veiledere":[],"registreringstype":[],"cvJobbprofil":null}"
                        """;
        PortefoljeFilter filterModel = JsonUtils.fromJson(jsonString, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel.getFerdigfilterListe());
        Assertions.assertEquals(2, filterModel.getFerdigfilterListe().size());
    }


    @Test
    public void testDeserializationWithAktivitet() {
        String inputJson = """
                 {"alder": [],"kjonn": null,"utdanning": [],"veiledere": [],"aktiviteter": {"EGEN":"JA","MOTE":"NEI","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"utdanningBestatt": [],"ferdigfilterListe": [],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"utdanningGodkjent": [],"veilederNavnQuery":"","manuellBrukerStatus": [],"sisteEndringKategori": "FULLFORT_BEHANDLING"}
                """;

        PortefoljeFilter filterModel = JsonUtils.fromJson(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getAktiviteter());
        Assertions.assertNotNull(filterModel.getAktiviteter().getEGEN());
        Assertions.assertEquals("JA", filterModel.getAktiviteter().getEGEN());
        Assertions.assertEquals("NEI", filterModel.getAktiviteter().getMOTE());
    }

    @Test
    public void testDeserializationWithSisteEndringKategori() {
        String inputJson = """
                 {"alder": [],"kjonn": null,"utdanning": [],"veiledere": [],"aktiviteter": {"EGEN":"NA","MOTE":"NA","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"utdanningBestatt": [],"ferdigfilterListe": [],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"utdanningGodkjent": [],"veilederNavnQuery":"","manuellBrukerStatus": [],"sisteEndringKategori": "FULLFORT_BEHANDLING"}
                """;

        PortefoljeFilter filterModel = JsonUtils.fromJson(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getSisteEndringKategori());
        Assertions.assertEquals("FULLFORT_BEHANDLING", filterModel.getSisteEndringKategori());
    }

    @Test
    public void testDeserializationWithFerdigfilterListe() {
        String inputJson = """
                {"alder": [],"kjonn": null,"veiledere": [],"aktiviteter": {"EGEN":"NA","MOTE":"NA","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"ferdigfilterListe": ["TRENGER_VURDERING","UFORDELTE_BRUKERE"],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"veilederNavnQuery":"","manuellBrukerStatus": []}""";

        PortefoljeFilter filterModel = JsonUtils.fromJson(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel);
        Assertions.assertNotNull(filterModel.getFerdigfilterListe());
        Assertions.assertTrue(filterModel.getFerdigfilterListe().contains("TRENGER_VURDERING"));
    }

    @Test
    public void testDeserializationAndSettingDefaultValue() {
        String inputJson = """
                 {"alder": [],"kjonn": null,"veiledere": [],"aktiviteter": {"EGEN":"NA","MOTE":"NA","IJOBB":"NA","TILTAK":"NA","STILLING":"NA","BEHANDLING":"NA","SOKEAVTALE":"NA","GRUPPEAKTIVITET":"NA","UTDANNINGAKTIVITET":"NA"},"cvJobbprofil": null,"tiltakstyper": [],"servicegruppe": [],"fodselsdagIMnd": [],"rettighetsgruppe": [],"ferdigfilterListe": ["TRENGER_VURDERING","UFORDELTE_BRUKERE"],"formidlingsgruppe": [],"navnEllerFnrQuery":"","registreringstype": [],"veilederNavnQuery":"","manuellBrukerStatus": []}
                """;

        PortefoljeFilter filterModel = JsonUtils.fromJson(inputJson, PortefoljeFilter.class);

        Assertions.assertNotNull(filterModel.getAktiviteterForenklet());
        Assertions.assertEquals(filterModel.getAktiviteterForenklet().size(), 0);
    }

    @Test
    public void testSerializationOfEmptyFilter() {
        String correctOutput = """
                {"aktiviteter":null,"aktiviteterForenklet":[],"alder":[],"barnUnder18Aar":[],"barnUnder18AarAlder":[],"cvJobbprofil":"","ensligeForsorgere":[],"fargekategorier":[],"ferdigfilterListe":[],"fodselsdagIMnd":[],"foedeland":[],"formidlingsgruppe":[],"geografiskBosted":[],"gjeldendeVedtak14a":[],"hovedmalGjeldendeVedtak14a":[],"innsatsgruppeGjeldendeVedtak14a":[],"kjonn":"","landgruppe":[],"manuellBrukerStatus":[],"navnEllerFnrQuery":"","registreringstype":[],"rettighetsgruppe":[],"servicegruppe":[],"sisteEndringKategori":"","stillingFraNavFilter":[],"tiltakstyper":[],"tolkBehovSpraak":[],"tolkebehov":[],"ulesteEndringer":"","utdanning":[],"utdanningBestatt":[],"utdanningGodkjent":[],"veilederNavnQuery":"","veiledere":[],"visGeografiskBosted":[],"ytelseAapArena":[],"ytelseAapKelvin":[],"ytelseDagpenger":[],"ytelseDagpengerArena":[],"ytelseTiltakspenger":[],"ytelseTiltakspengerArena":[]}""";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        String jsonString = JsonUtils.toJson(portefoljeFilter);
        Assertions.assertEquals(jsonString, correctOutput);
    }

    @Test
    public void testSerializationOfVeiledere() {
        String correctOutput = """
                {"aktiviteter":null,"aktiviteterForenklet":null,"alder":null,"barnUnder18Aar":null,"barnUnder18AarAlder":null,"cvJobbprofil":null,"ensligeForsorgere":null,"fargekategorier":null,"ferdigfilterListe":null,"fodselsdagIMnd":null,"foedeland":null,"formidlingsgruppe":null,"geografiskBosted":null,"gjeldendeVedtak14a":null,"hovedmalGjeldendeVedtak14a":null,"innsatsgruppeGjeldendeVedtak14a":null,"kjonn":null,"landgruppe":null,"manuellBrukerStatus":null,"navnEllerFnrQuery":null,"registreringstype":null,"rettighetsgruppe":null,"servicegruppe":null,"sisteEndringKategori":null,"stillingFraNavFilter":null,"tiltakstyper":null,"tolkBehovSpraak":null,"tolkebehov":null,"ulesteEndringer":null,"utdanning":null,"utdanningBestatt":null,"utdanningGodkjent":null,"veilederNavnQuery":null,"veiledere":["A123","B123"],"visGeografiskBosted":null,"ytelseAapArena":null,"ytelseAapKelvin":null,"ytelseDagpenger":null,"ytelseDagpengerArena":null,"ytelseTiltakspenger":null,"ytelseTiltakspengerArena":null}""";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter(null, null, null, null, null, null, null, null,
                null, null, null, null, List.of("A123", "B123"), null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        String jsonString = JsonUtils.toJson(portefoljeFilter);
        Assertions.assertEquals(jsonString, correctOutput);
    }

}
