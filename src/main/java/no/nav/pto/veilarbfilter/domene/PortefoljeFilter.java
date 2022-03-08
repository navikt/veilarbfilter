package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static java.util.Collections.emptyList;

@JsonIgnoreProperties
@Getter
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonIgnore
    public Boolean isNotEmpty() {
        boolean existsNonEmptyStringVars = List.of(
                kjonn,
                navnEllerFnrQuery,
                veilederNavnQuery,
                ytelse,
                cvJobbprofil,
                ulesteEndringer
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
                utdanningBestatt,
                sisteEndringKategori,
                aktiviteterForenklet
        ).stream().anyMatch(x -> x != null && !x.isEmpty());

        return existsNonEmptyStringVars || existsNonEmptyArrays || aktiviteter != null;
    }

}