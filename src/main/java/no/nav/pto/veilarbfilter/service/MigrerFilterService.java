package no.nav.pto.veilarbfilter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.domene.value.Innsatsgruppe;
import no.nav.pto.veilarbfilter.mapper.MigrerFilterMapper;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import org.springframework.stereotype.Service;

import java.util.*;

import static no.nav.pto.veilarbfilter.repository.FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY;
import static no.nav.pto.veilarbfilter.repository.FilterRepository.ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY;
import static no.nav.pto.veilarbfilter.util.SecureLogUtils.secureLog;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrerFilterService {
    public static final int BATCH_STORRELSE_ALLE = -1;
    private static final int BATCHSTORRELSE_FOR_JOBB = 2;

    private final FilterRepository filterRepository;
    private final ObjectMapper objectMapper;

    public void migrerFilterJobb() {
        log.info("Filtermigrering: Jobb startet");

        int antallFilterMedFilterverdi = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
        log.info("Filtermigrering: Totalt antall filter med hovedmål = " + antallFilterMedFilterverdi);

        try {
            migrerFilter(BATCHSTORRELSE_FOR_JOBB, ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
            log.info("Filtermigrering: Jobb fullført");
        } catch (RuntimeException e) {
            log.error("Filtermigrering: Noe gikk galt i migrering, sjå feilmelding i securelogs");
            secureLog.error("Filtermigrering: Noe gikk galt i migrering", e);
        }
    }

    public void migrerFilter(int batchStorrelse, String filterType) {
        List<FilterModel> filtreSomSkalMigreres = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(filterType, batchStorrelse);
        int forsoktMigrerteFilter = filtreSomSkalMigreres.size();
        log.info("Filtermigrering: Antall forsøkt migrert = " + forsoktMigrerteFilter);

        int faktiskMigrerteFilter = 0;
        if (ARENA_HOVEDMAL_FILTERVALG_JSON_KEY.equals(filterType)) {
            faktiskMigrerteFilter = erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalgBatch(filtreSomSkalMigreres);
        }

        if (ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY.equals(filterType)) {
            faktiskMigrerteFilter = erstattArenaInnsatsgruppeMedInnsatsgruppeGjeldendeVedtak14aIFiltervalgBatch(filtreSomSkalMigreres);
        }

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

    public int erstattArenaInnsatsgruppeMedInnsatsgruppeGjeldendeVedtak14aIFiltervalgBatch(List<FilterModel> filtreSomSkalOppdateres) {
        List<FilterRepository.FilterIdOgFilterValgPar> lagredeFilterMedMigrerteInnsatsgruppe = filtreSomSkalOppdateres.stream()
                .peek(this::migrerArenaInnsatsgruppeTilGjeldendeVedtakInnsatsgruppeForFilter)
                .map(this::mapTilFilterIdOgFilterValgPar)
                .toList();

        return filterRepository.oppdaterFilterValgBatch(lagredeFilterMedMigrerteInnsatsgruppe);
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

    private void migrerArenaInnsatsgruppeTilGjeldendeVedtakInnsatsgruppeForFilter(FilterModel filterSomSkalOppdateres) {
        // Lag liste over migrerte hovedmål
        List<String> innsatsgruppeFraArenaInnsatsgruppe = lagGjeldendeVedtakInnsatsgruppeFraArenaInnsatsgruppe(filterSomSkalOppdateres.getFilterValg().getInnsatsgruppe());

        // Slå saman nye og gamle HovedmalGjeldendeVedtak-filter
        List<String> innsatsgruppeFraGjeldendeVedtak14a = filterSomSkalOppdateres.getFilterValg().getInnsatsgruppeGjeldendeVedtak14a();
        Set<String> alleUnikeInnsatsgrupper = new HashSet<>(innsatsgruppeFraGjeldendeVedtak14a);
        alleUnikeInnsatsgrupper.addAll(innsatsgruppeFraArenaInnsatsgruppe);

        List<String> unikeSorterteInnsatsgruppe = alleUnikeInnsatsgrupper.stream().sorted().toList();

        // Lag oppdatert porteføljefilter
        PortefoljeFilter portefoljeFilterSomSkalOppdateres = filterSomSkalOppdateres.getFilterValg();
        portefoljeFilterSomSkalOppdateres.setInnsatsgruppeGjeldendeVedtak14a(unikeSorterteInnsatsgruppe);
        portefoljeFilterSomSkalOppdateres.setInnsatsgruppe(Collections.emptyList());

        // Lag klart oppdatert filtermodell og skriv tilbake til databasen
        filterSomSkalOppdateres.setFilterValg(portefoljeFilterSomSkalOppdateres);
    }

    private List<String> lagGjeldendeVedtakHovedmalFraArenahovedmal(List<String> arenahovedmal) {
        List<Hovedmal> hovedmalGjeldendeVedtak = arenahovedmal.stream()
                .map(ArenaHovedmal::valueOf)
                .map(MigrerFilterMapper::mapTilHovedmalGjeldendeVedtak14a)
                .toList();

        return hovedmalGjeldendeVedtak.stream().map(Hovedmal::name).toList();
    }

    private List<String> lagGjeldendeVedtakInnsatsgruppeFraArenaInnsatsgruppe(List<String> arenaInnsatsgruppe) {
        List<Innsatsgruppe> innsatsgruppeGjeldendeVedtak = arenaInnsatsgruppe.stream()
                .map(ArenaInnsatsgruppe::valueOf)
                .map(MigrerFilterMapper::mapTilInnsatsgruppeGjeldendeVedtak14a)
                .flatMap(Collection::stream)
                .toList();

        return innsatsgruppeGjeldendeVedtak.stream().map(Innsatsgruppe::name).toList();
    }
}
