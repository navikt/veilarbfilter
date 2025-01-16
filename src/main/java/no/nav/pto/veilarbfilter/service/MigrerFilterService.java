package no.nav.pto.veilarbfilter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.nav.pto.veilarbfilter.repository.FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY;
import static no.nav.pto.veilarbfilter.util.SecureLogUtils.secureLog;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrerFilterService {
    public static final int BATCH_STORRELSE_ALLE = -1;

    private final FilterRepository filterRepository;
    private final ObjectMapper objectMapper;
    private static final int BATCHSTORRELSE_FOR_JOBB = 2;

    public void migrerFilterJobb() {
        log.info("Filtermigrering: Jobb startet");

        int antallFilterMedFilterverdi = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
        log.info("Filtermigrering: Totalt antall filter med hovedmål = " + antallFilterMedFilterverdi);

        try {
            migrerFilter(BATCHSTORRELSE_FOR_JOBB);
            log.info("Filtermigrering: Jobb fullført");
        } catch (RuntimeException e) {
            log.error("Filtermigrering: Noe gikk galt i migrering, sjå feilmelding i securelogs");
            secureLog.error("Filtermigrering: Noe gikk galt i migrering", e);
        }
    }

    public void migrerFilter(int batchStorrelse) {
        List<FilterModel> filtreSomSkalMigreres = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(ARENA_HOVEDMAL_FILTERVALG_JSON_KEY, batchStorrelse);
        int forsoktMigrerteFilter = filtreSomSkalMigreres.size();
        log.info("Filtermigrering: Antall forsøkt migrert = " + forsoktMigrerteFilter);

        int faktiskMigrerteFilter = erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalgBatch(filtreSomSkalMigreres);
        log.info("Filtermigrering: Antall faktisk migrert = " + faktiskMigrerteFilter);
    }

    public void erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(FilterModel filterSomSkalOppdateres) throws JsonProcessingException {
        // Lag liste over migrerte hovedmål
        migrerArenaHovedmalTilGjeldendeVedtakHovedmalForFilter(filterSomSkalOppdateres);
        filterRepository.oppdaterFilterValg(filterSomSkalOppdateres.getFilterId(), filterSomSkalOppdateres.getFilterValg()); // todo handter feil ved skriving
    }

    public int erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalgBatch(List<FilterModel> filtreSomSkalOppdateres) {
        List<FilterRepository.FilterIdOgFilterValgPar> lagredeFilterMedMigrerteHovedmal = filtreSomSkalOppdateres.stream()
                .peek(this::migrerArenaHovedmalTilGjeldendeVedtakHovedmalForFilter)
                .map(this::mapTilFilterIdOgFilterValgPar)
                .toList();

        return filterRepository.oppdaterFilterValgBatch(lagredeFilterMedMigrerteHovedmal);
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
