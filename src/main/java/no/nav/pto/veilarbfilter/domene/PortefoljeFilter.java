package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortefoljeFilter {
    private Aktiviteter aktiviteter = null;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> alder = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ferdigfilterListe = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> fodselsdagIMnd = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> formidlingsgruppe = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> hovedmal = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> innsatsgruppe = emptyList();

    private String kjonn = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> manuellBrukerStatus = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String navnEllerFnrQuery = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> rettighetsgruppe = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> servicegruppe = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> tiltakstyper = emptyList();

    private String veilederNavnQuery = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Setter
    private List<String> veiledere = emptyList();

    private String ytelse = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> registreringstype = emptyList();

    private String cvJobbprofil = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<KategoriModell> arbeidslisteKategori = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> utdanning = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> utdanningGodkjent = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> utdanningBestatt = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> sisteEndringKategori = emptyList();

    private String ulesteEndringer = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> aktiviteterForenklet = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> landgruppe = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> foedeland = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> tolkebehov = emptyList();;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> tolkBehovSpraak = emptyList();

    @JsonIgnore
    public Boolean isNotEmpty() {
        return (kjonn != null && !kjonn.isEmpty()) ||
                (navnEllerFnrQuery != null && !navnEllerFnrQuery.isEmpty()) ||
                (veilederNavnQuery != null && !veilederNavnQuery.isEmpty()) ||
                (ytelse != null && !ytelse.isEmpty()) ||
                (cvJobbprofil != null && !cvJobbprofil.isEmpty()) ||
                (alder != null && !alder.isEmpty()) ||
                (ferdigfilterListe != null && !ferdigfilterListe.isEmpty()) ||
                (fodselsdagIMnd != null && !fodselsdagIMnd.isEmpty()) ||
                (formidlingsgruppe != null && !formidlingsgruppe.isEmpty()) ||
                (hovedmal != null && !hovedmal.isEmpty()) ||
                (innsatsgruppe != null && !innsatsgruppe.isEmpty()) ||
                (manuellBrukerStatus != null && !manuellBrukerStatus.isEmpty()) ||
                (rettighetsgruppe != null && !rettighetsgruppe.isEmpty()) ||
                (servicegruppe != null && !servicegruppe.isEmpty()) ||
                (tiltakstyper != null && !tiltakstyper.isEmpty()) ||
                (veiledere != null && !veiledere.isEmpty()) ||
                (registreringstype != null && !registreringstype.isEmpty()) ||
                (utdanning != null && !utdanning.isEmpty()) ||
                (utdanningGodkjent != null && !utdanningGodkjent.isEmpty()) ||
                (utdanningBestatt != null && !utdanningBestatt.isEmpty()) ||
                (sisteEndringKategori != null && !sisteEndringKategori.isEmpty()) ||
                (aktiviteterForenklet != null && !aktiviteterForenklet.isEmpty()) ||
                aktiviteter != null ||
                (landgruppe != null && !landgruppe.isEmpty()) || (foedeland != null && !foedeland.isEmpty()) ||
                (tolkebehov != null && !tolkebehov.isEmpty()) ||
                (tolkBehovSpraak != null && !tolkBehovSpraak.isEmpty());
    }

}