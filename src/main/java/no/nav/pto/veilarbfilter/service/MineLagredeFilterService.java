package no.nav.pto.veilarbfilter.service;

import io.getunleash.DefaultUnleash;
import io.getunleash.UnleashContext;
import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.config.FeatureToggle;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MineLagredeFilterService implements FilterService {
    private final MineLagredeFilterRepository mineLagredeFilterRepository;
    private final DefaultUnleash defaultUnleash;

    @Override
    public Optional<FilterModel> lagreFilter(String veilederId, NyttFilterModel nyttFilter) throws IllegalArgumentException {
        try {
            PortefoljeFilter filtereMedKopiertArenaFilter = leggTilDagpengerFraYtelseINyttDagpengerArenaFilter(nyttFilter.getFilterValg());
            nyttFilter.setFilterValg(filtereMedKopiertArenaFilter);
            Optional<FilterModel> lagraFilter = mineLagredeFilterRepository.lagreFilter(veilederId, nyttFilter);
            return fjernDuplikatAvDagpengerArenaFilterTilFrontend(lagraFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) throws IllegalArgumentException {
        try {
            PortefoljeFilter filtereMedKopiertArenaFilter = leggTilDagpengerFraYtelseINyttDagpengerArenaFilter(filter.getFilterValg());
            filter.setFilterValg(filtereMedKopiertArenaFilter);
            Optional<FilterModel> lagraFilter = mineLagredeFilterRepository.oppdaterFilter(veilederId, filter);
            return fjernDuplikatAvDagpengerArenaFilterTilFrontend(lagraFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        Optional<FilterModel> filterModel = mineLagredeFilterRepository.hentFilter(filterId);
        Optional<FilterModel> filterModelUtenDuplisertDagpenger = fjernDuplikatAvDagpengerArenaFilterTilFrontend(filterModel);
        return filtrerUtSamanlikn14aOgArenaFilterTilFrontend(filterModelUtenDuplisertDagpenger);
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {
        List<FilterModel> filterModelListe = mineLagredeFilterRepository.finnFilterForFilterBruker(veilederId);
        return filterModelListe.stream()
                .map(this::filtrerUtSamanlikn14aOgArenaFilterTilFrontend)
                .map(filter -> filter == null ? null : fjernDuplikatAvDagpengerArenaFilterTilFrontend(filter))
                .filter(Objects::nonNull).toList();
    }

    @Override
    public Integer slettFilter(Integer filterId, String veilederId) {
        return mineLagredeFilterRepository.slettFilter(filterId, veilederId);
    }

    @Override
    public List<FilterModel> lagreSortering(String veilederId, List<SortOrder> sortOrder) {
        return mineLagredeFilterRepository.lagreSortering(veilederId, sortOrder);
    }

    public List<MineLagredeFilterModel> hentAllLagredeFilter() {
        return mineLagredeFilterRepository.hentAllLagredeFilter();
    }

    private Optional<FilterModel> filtrerUtSamanlikn14aOgArenaFilterTilFrontend(Optional<FilterModel> filterModelOptional) {
        return filterModelOptional.map(this::filtrerUtSamanlikn14aOgArenaFilterTilFrontend);
    }

    /* I ein mellomfase der vi har skrudd på feature-toggle for å skjule samanliknings-filteret
     * mellom gjeldande § 14 a-vedtak og Arena filtrerer vi vekk filter som inneheld det.
     * Vi har på førehand sjekka at omfanget er lite, og at filtera berre handlar om avvik mellom gjeldande vedtak og Arena. */
    private FilterModel filtrerUtSamanlikn14aOgArenaFilterTilFrontend(FilterModel filter) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();
        UnleashContext unleashContext = UnleashContext.builder()
                .userId(veilederId)
                .build();

        boolean skjulFilterSammenligneGjeldende14aOgArena = defaultUnleash.isEnabled(FeatureToggle.SKJUL_FILTER_SAMMENLIGNE_GJELDENDE_14A_OG_ARENA, unleashContext);

        if (!skjulFilterSammenligneGjeldende14aOgArena) {
            // Ikkje gjer noko dersom feature-toggle er av
            return filter;
        } else {
            List<String> avvik14aFilter = filter.getFilterValg().getAvvik14aVedtak();

            // Ikkje gjer noko om filteret ikkje inneheld Samanlikningsfilteret
            if (avvik14aFilter.isEmpty()) {
                return filter;
            }

            // Filtrer bort filteret om det inneheld Samanlikningsfilteret
            return null;
        }
    }

    private PortefoljeFilter leggTilDagpengerFraYtelseINyttDagpengerArenaFilter(PortefoljeFilter filter) {
        String dpYtelse = filter.getYtelse();
        boolean dpOrdinar = Objects.equals(dpYtelse, "ORDINARE_DAGPENGER");
        boolean dpMedPermittering = Objects.equals(dpYtelse, "DAGPENGER_MED_PERMITTERING");
        boolean dpMedPermitteringFiskeindustri = Objects.equals(dpYtelse, "DAGPENGER_MED_PERMITTERING_FISKEINDUSTRI");
        boolean dpLonnsgarantimidler = Objects.equals(dpYtelse, "LONNSGARANTIMIDLER_DAGPENGER");
        boolean dpOvrige = Objects.equals(dpYtelse, "DAGPENGER_OVRIGE");
        boolean dpArena = Objects.equals(dpYtelse, "DAGPENGER");


        if (!dpOrdinar && !dpMedPermittering && !dpMedPermitteringFiskeindustri && !dpLonnsgarantimidler && !dpOvrige && !dpArena) {
            return filter;
        }

        List<String> dagpengerNyttFilterListe;

        if (dpOrdinar) {
            dagpengerNyttFilterListe = List.of("HAR_DAGPENGER_ORDINAER");
        } else if (dpMedPermittering) {
            dagpengerNyttFilterListe = List.of("HAR_DAGPENGER_MED_PERMITTERING");
        } else if (dpMedPermitteringFiskeindustri) {
            dagpengerNyttFilterListe = List.of("HAR_DAGPENGER_MED_PERMITTERING_FISKEINDUSTRI");
        } else if (dpLonnsgarantimidler) {
            dagpengerNyttFilterListe = List.of("HAR_DAGPENGER_LONNSGARANTIMIDLER");
        } else if (dpOvrige) {
            dagpengerNyttFilterListe = List.of("HAR_DAGPENGER_OVRIGE");
        } else {
            dagpengerNyttFilterListe = List.of("HAR_DAGPENGER_ORDINAER", "HAR_DAGPENGER_MED_PERMITTERING", "HAR_DAGPENGER_MED_PERMITTERING_FISKEINDUSTRI",
                    "HAR_DAGPENGER_LONNSGARANTIMIDLER", "HAR_DAGPENGER_OVRIGE");
        }

        filter.setYtelseDagpengerArena(dagpengerNyttFilterListe);
        return filter;
    }

    private Optional<FilterModel> fjernDuplikatAvDagpengerArenaFilterTilFrontend(Optional<FilterModel> filterModelOptional) {
        return filterModelOptional.map(this::fjernDuplikatAvDagpengerArenaFilterTilFrontend);
    }

    // Når toggle er på skal kun det nye dagpenger filteret returneres. Når den er av skal kun det gamle returneres.
    private FilterModel fjernDuplikatAvDagpengerArenaFilterTilFrontend(FilterModel filter) {
        boolean brukNyttDagpengerFilterErSkruddPå = defaultUnleash.isEnabled(FeatureToggle.BRUK_NYTT_ARENA_DAGPENGER_FILTER);

        if (brukNyttDagpengerFilterErSkruddPå) {
            String gammeltYtelseFilter = filter.getFilterValg().getYtelse();
            if (gammeltYtelseFilter != null) {
                List<String> muligeDagpengerYtelseFiltre = List.of("ORDINARE_DAGPENGER", "DAGPENGER_MED_PERMITTERING",
                        "DAGPENGER_MED_PERMITTERING_FISKEINDUSTRI", "LONNSGARANTIMIDLER_DAGPENGER", "DAGPENGER_OVRIGE", "DAGPENGER");
                if (muligeDagpengerYtelseFiltre.contains(gammeltYtelseFilter)) {
                    filter.getFilterValg().setYtelse(null);
                }
            }
        } else {
            filter.getFilterValg().setYtelseDagpengerArena(Collections.emptyList());
        }
        return filter;
    }
}
