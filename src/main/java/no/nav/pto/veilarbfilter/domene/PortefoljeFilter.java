package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)

/* * * * * VIKTIG! * * * * * VIKTIG! * * * * * VIKTIG! * * * * * VIKTIG! * * * * * VIKTIG! * * * * *
 * Om PortefoljeFilter får endringar må ein også oppdatere FiltervalgModell i                      *
 * veilarbportefoljeflatefs. Begge repoa må deployast samstundes, elles knekk ein Mine filter i    *
 * prod.                                                                                           *
 *                                                                                                 *
 * Relevant fil: https://github.com/navikt/veilarbportefoljeflatefs/blob/main/src/typer/filtervalg-modell.ts (2025-11-10) *
 * Eksempel-PR frå "ytelseDagpengerArena"-filter: https://github.com/navikt/veilarbportefoljeflatefs/pull/1341 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
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

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Setter
    private List<String> registreringstype = emptyList();

    private String cvJobbprofil = "";

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
    private List<String> tolkebehov = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> tolkBehovSpraak = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> stillingFraNavFilter = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> geografiskBosted = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> visGeografiskBosted = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> avvik14aVedtak = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ensligeForsorgere = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> barnUnder18Aar = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> barnUnder18AarAlder = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> fargekategorier = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> gjeldendeVedtak14a = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Setter
    private List<String> innsatsgruppeGjeldendeVedtak14a = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Setter
    private List<String> hovedmalGjeldendeVedtak14a = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ytelseAapArena = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ytelseAapKelvin = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ytelseTiltakspenger = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ytelseTiltakspengerArena = emptyList();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ytelseDagpengerArena = emptyList();

    @JsonIgnore
    public Boolean isNotEmpty() {
        return (kjonn != null && !kjonn.isEmpty()) ||
               (navnEllerFnrQuery != null && !navnEllerFnrQuery.isEmpty()) ||
               (veilederNavnQuery != null && !veilederNavnQuery.isEmpty()) ||
               (cvJobbprofil != null && !cvJobbprofil.isEmpty()) ||
               (alder != null && !alder.isEmpty()) ||
               (ferdigfilterListe != null && !ferdigfilterListe.isEmpty()) ||
               (fodselsdagIMnd != null && !fodselsdagIMnd.isEmpty()) ||
               (formidlingsgruppe != null && !formidlingsgruppe.isEmpty()) ||
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
               (landgruppe != null && !landgruppe.isEmpty()) ||
               (foedeland != null && !foedeland.isEmpty()) ||
               (tolkebehov != null && !tolkebehov.isEmpty()) ||
               (tolkBehovSpraak != null && !tolkBehovSpraak.isEmpty()) ||
               (stillingFraNavFilter != null && !stillingFraNavFilter.isEmpty()) ||
               (visGeografiskBosted != null && !visGeografiskBosted.isEmpty()) ||
               (geografiskBosted != null && !geografiskBosted.isEmpty()) ||
               (avvik14aVedtak != null && !avvik14aVedtak.isEmpty()) ||
               (ensligeForsorgere != null && !ensligeForsorgere.isEmpty()) ||
               (barnUnder18Aar != null && !barnUnder18Aar.isEmpty()) ||
               (barnUnder18AarAlder != null && !barnUnder18AarAlder.isEmpty()) ||
               (fargekategorier != null && !fargekategorier.isEmpty()) ||
               (gjeldendeVedtak14a != null && !gjeldendeVedtak14a.isEmpty()) ||
               (innsatsgruppeGjeldendeVedtak14a != null && !innsatsgruppeGjeldendeVedtak14a.isEmpty()) ||
               (hovedmalGjeldendeVedtak14a != null && !hovedmalGjeldendeVedtak14a.isEmpty()) ||
               (ytelseAapArena != null && !ytelseAapArena.isEmpty()) ||
               (ytelseAapKelvin != null && !ytelseAapKelvin.isEmpty()) ||
               (ytelseTiltakspenger != null && !ytelseTiltakspenger.isEmpty()) ||
               (ytelseTiltakspengerArena != null && !ytelseTiltakspengerArena.isEmpty()) ||
               (ytelseDagpengerArena != null && !ytelseDagpengerArena.isEmpty());
    }
}
