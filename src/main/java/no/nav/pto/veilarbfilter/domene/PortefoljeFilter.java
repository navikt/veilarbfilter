package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static java.util.Collections.emptyList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortefoljeFilter {
    private Aktiviteter aktiviteter = null;
    private List<String> alder = emptyList();
    private List<String> ferdigfilterListe = emptyList();
    private List<String> fodselsdagIMnd = emptyList();
    private List<String> formidlingsgruppe = emptyList();
    private List<String> hovedmal = emptyList();
    private List<String> innsatsgruppe = emptyList();
    private String kjonn = "";
    private List<String> manuellBrukerStatus = emptyList();
    private String navnEllerFnrQuery = "";
    private List<String> rettighetsgruppe = emptyList();
    private List<String> servicegruppe = emptyList();
    private List<String> tiltakstyper = emptyList();
    private String veilederNavnQuery = "";
    @Setter
    private List<String> veiledere = emptyList();
    private String ytelse = "";
    private List<String> registreringstype = emptyList();
    private String cvJobbprofil = "";
    private List<KategoriModell> arbeidslisteKategori = emptyList();
    private List<String> utdanning = emptyList();
    private List<String> utdanningGodkjent = emptyList();
    private List<String> utdanningBestatt = emptyList();
    private List<String> sisteEndringKategori = emptyList();
    private String ulesteEndringer = "";
    private List<String> aktiviteterForenklet = emptyList();

    @JsonIgnore
    public Boolean isNotEmpty() {
        boolean existsNonEmptyStringVars = List.of(
                kjonn,
                navnEllerFnrQuery,
                veilederNavnQuery,
                ytelse,
                cvJobbprofil
        ).stream().anyMatch(x -> x != null && !x.isEmpty());

        boolean existsNonEmptyArrays = List.of(
                alder,
                ferdigfilterListe,
                fodselsdagIMnd,
                formidlingsgruppe,
                hovedmal,
                innsatsgruppe,
                manuellBrukerStatus,
                rettighetsgruppe,
                servicegruppe,
                tiltakstyper,
                veiledere,
                registreringstype,
                utdanning,
                utdanningGodkjent,
                utdanningBestatt
        ).stream().anyMatch(x -> x != null && !x.isEmpty());

        return existsNonEmptyStringVars || existsNonEmptyArrays || aktiviteter != null;
    }

}