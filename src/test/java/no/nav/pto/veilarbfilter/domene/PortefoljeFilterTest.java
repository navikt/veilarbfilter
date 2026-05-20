package no.nav.pto.veilarbfilter.domene;

import no.nav.common.json.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

class PortefoljeFilterTest {

    @Test
    public void testDeserialization() throws IOException {
        var jsonString =
                """
                                 {"aktiviteter":{"BEHANDLING":"NA","EGEN":"NA","GRUPPEAKTIVITET":"NA","IJOBB":"NA","MOTE":"NA","SOKEAVTALE":"NA","STILLING":"NA","TILTAK":"NA","UTDANNINGAKTIVITET":"NA"},"alder":[],"ferdigfilterListe":["NYE_BRUKERE_FOR_VEILEDER","PERMITTERTE_ETTER_NIENDE_MARS"],"fodselsdagIMnd":[],"formidlingsgruppe":[],"kjonn":null,"manuellBrukerStatus":[],"navnEllerFnrQuery":"","rettighetsgruppe":[],"servicegruppe":[],"tiltakstyper":[],"veilederNavnQuery":"","veiledere":[],"registreringstype":[],"cvJobbprofil":null}
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
                {"aktiviteter":null,"alder":[],"ferdigfilterListe":[],"fodselsdagIMnd":[],"formidlingsgruppe":[],"kjonn":"","manuellBrukerStatus":[],"navnEllerFnrQuery":"","rettighetsgruppe":[],"servicegruppe":[],"tiltakstyper":[],"veilederNavnQuery":"","veiledere":[],"registreringstype":[],"cvJobbprofil":"","utdanning":[],"utdanningGodkjent":[],"utdanningBestatt":[],"sisteEndringKategori":"","ulesteEndringer":"","aktiviteterForenklet":[],"landgruppe":[],"foedeland":[],"tolkebehov":[],"tolkBehovSpraak":[],"stillingFraNavFilter":[],"geografiskBosted":[],"visGeografiskBosted":[],"ensligeForsorgere":[],"barnUnder18Aar":[],"barnUnder18AarAlder":[],"fargekategorier":[],"gjeldendeVedtak14a":[],"innsatsgruppeGjeldendeVedtak14a":[],"hovedmalGjeldendeVedtak14a":[],"ytelseAapArena":[],"ytelseAapKelvin":[],"ytelseTiltakspenger":[],"ytelseTiltakspengerArena":[],"ytelseDagpengerArena":[],"ytelseDagpenger":[],"ytelseUngdomsprogram":[]}""";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        String jsonString = JsonUtils.toJson(portefoljeFilter);
        Assertions.assertEquals(correctOutput, jsonString);
    }

    @Test
    public void testSerializationOfVeiledere() {
        String correctOutput = """
                {"aktiviteter":null,"alder":null,"ferdigfilterListe":null,"fodselsdagIMnd":null,"formidlingsgruppe":null,"kjonn":null,"manuellBrukerStatus":null,"navnEllerFnrQuery":null,"rettighetsgruppe":null,"servicegruppe":null,"tiltakstyper":null,"veilederNavnQuery":null,"veiledere":["A123","B123"],"registreringstype":null,"cvJobbprofil":null,"utdanning":null,"utdanningGodkjent":null,"utdanningBestatt":null,"sisteEndringKategori":null,"ulesteEndringer":null,"aktiviteterForenklet":null,"landgruppe":null,"foedeland":null,"tolkebehov":null,"tolkBehovSpraak":null,"stillingFraNavFilter":null,"geografiskBosted":null,"visGeografiskBosted":null,"ensligeForsorgere":null,"barnUnder18Aar":null,"barnUnder18AarAlder":null,"fargekategorier":null,"gjeldendeVedtak14a":null,"innsatsgruppeGjeldendeVedtak14a":null,"hovedmalGjeldendeVedtak14a":null,"ytelseAapArena":null,"ytelseAapKelvin":null,"ytelseTiltakspenger":null,"ytelseTiltakspengerArena":null,"ytelseDagpengerArena":null,"ytelseDagpenger":null,"ytelseUngdomsprogram":null}""";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter(null, null, null, null, null, null, null, null,
                null, null, null, null, List.of("A123", "B123"), null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
        String jsonString = JsonUtils.toJson(portefoljeFilter);
        Assertions.assertEquals(correctOutput, jsonString);
    }

}
