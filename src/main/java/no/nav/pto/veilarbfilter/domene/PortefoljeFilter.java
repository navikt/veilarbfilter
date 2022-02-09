package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static java.util.Collections.emptyList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class PortefoljeFilter {
    private final Aktiviteter aktiviteter = null;
    private final List<String> alder = emptyList();
    private final List<String> ferdigfilterListe = emptyList();
    private final List<String> fodselsdagIMnd = emptyList();
    private final List<String> formidlingsgruppe = emptyList();
    private final List<String> hovedmal = emptyList();
    private final List<String> innsatsgruppe = emptyList();
    private final String kjonn = "";
    private final List<String> manuellBrukerStatus = emptyList();
    private final String navnEllerFnrQuery = "";
    private final List<String> rettighetsgruppe = emptyList();
    private final List<String> servicegruppe = emptyList();
    private final List<String> tiltakstyper = emptyList();
    private final String veilederNavnQuery = "";
    @Setter
    private List<String> veiledere = emptyList();
    private final String ytelse = "";
    private final List<String> registreringstype = emptyList();
    private final String cvJobbprofil = "";
    private final List<KategoriModell> arbeidslisteKategori = emptyList();
    private final List<String> utdanning = emptyList();
    private final List<String> utdanningGodkjent = emptyList();
    private final List<String> utdanningBestatt = emptyList();
    private final List<String> sisteEndringKategori = emptyList();
    private final String ulesteEndringer = "";
    private final List<String> aktiviteterForenklet = emptyList();

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