package no.nav.pto.veilarbfilter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.nav.pto.veilarbfilter.repository.FilterRepository.HOVEDMAL_FILTERVALG_JSON_KEY;


@Service
@RequiredArgsConstructor
public class MigrerFilterService {
    private final FilterRepository filterRepository;
    private final ObjectMapper objectMapper;

    public void migrerFilter(int batchStorrelse) {
        List<FilterModel> filtreSomSkalMigreres = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(HOVEDMAL_FILTERVALG_JSON_KEY, batchStorrelse);
        erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalgBatch(filtreSomSkalMigreres);
    }

    public void erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(FilterModel filterSomSkalOppdateres) throws JsonProcessingException {
        // Lag liste over migrerte hovedmål
        migrerArenaHovedmalTilGjeldendeVedtakHovedmalForFilter(filterSomSkalOppdateres);
        filterRepository.oppdaterFilterValg(filterSomSkalOppdateres.getFilterId(), filterSomSkalOppdateres.getFilterValg()); // todo handter feil ved skriving
    }

    public void erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalgBatch(List<FilterModel> filtreSomSkalOppdateres) {
        List<FilterRepository.FilterIdOgFilterValgPar> lagredeFilterMedMigrerteHovedmal = filtreSomSkalOppdateres.stream()
                .peek(this::migrerArenaHovedmalTilGjeldendeVedtakHovedmalForFilter)
                .map(this::mapTilFilterIdOgFilterValgPar)
                .toList();

        filterRepository.oppdaterFilterValgBatch(lagredeFilterMedMigrerteHovedmal);
    }

    private FilterRepository.FilterIdOgFilterValgPar mapTilFilterIdOgFilterValgPar(FilterModel mappetFilter) {
        try {
            return new FilterRepository.FilterIdOgFilterValgPar(mappetFilter.getFilterId(), objectMapper.writeValueAsString(mappetFilter.getFilterValg()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void migrerArenaHovedmalTilGjeldendeVedtakHovedmalForFilter(FilterModel filterSomSkalOppdateres) {
        // Lag liste over migrerte hovedmål
        List<String> hovedmalFraArenahovedmal = lagGjeldendeVedtakHovedmalFraArenahovedmal(filterSomSkalOppdateres.getFilterValg().getHovedmal());

        // Slå saman nye og gamle HovedmalGjeldendeVedtak-filter
        List<String> hovedmalFraGjeldendeVedtak14a = filterSomSkalOppdateres.getFilterValg().getHovedmalGjeldendeVedtak14a();
        Set<String> alleUnikeHovedmal = new HashSet<>(hovedmalFraGjeldendeVedtak14a);
        alleUnikeHovedmal.addAll(hovedmalFraArenahovedmal);

        List<String> unikeSorterteHovedmal = alleUnikeHovedmal.stream().sorted().toList();

        // Lag oppdatert porteføljefilter
        PortefoljeFilter portefoljeFilterSomSkalOppdateres = filterSomSkalOppdateres.getFilterValg();
        portefoljeFilterSomSkalOppdateres.setHovedmalGjeldendeVedtak14a(unikeSorterteHovedmal);
        portefoljeFilterSomSkalOppdateres.setHovedmal(Collections.emptyList());

        // Lag klart oppdatert filtermodell og skriv tilbake til databasen
        filterSomSkalOppdateres.setFilterValg(portefoljeFilterSomSkalOppdateres);
    }

    private List<String> lagGjeldendeVedtakHovedmalFraArenahovedmal(List<String> arenahovedmal) {
        List<Hovedmal> hovedmalGjeldendeVedtak = arenahovedmal.stream()
                .map(ArenaHovedmal::valueOf)
                .map(ArenaHovedmal::mapTilHovedmalGjeldendeVedtak14a)
                .toList();


        return hovedmalGjeldendeVedtak.stream().map(Hovedmal::name).toList();
    }
}
