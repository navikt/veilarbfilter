package no.nav.pto.veilarbfilter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getunleash.DefaultUnleash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.pto.veilarbfilter.config.FeatureToggle;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.domene.value.Innsatsgruppe;
import no.nav.pto.veilarbfilter.mapper.MigrerFilterMapper;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static no.nav.pto.veilarbfilter.repository.FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY;
import static no.nav.pto.veilarbfilter.repository.FilterRepository.ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY;
import static no.nav.pto.veilarbfilter.util.SecureLogUtils.secureLog;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrerFilterService {
    public static final int BATCH_STORRELSE_ALLE = -1;
    private static final int DEFAULT_BATCHSTORRELSE_FOR_JOBB = 50;
    private static final int ET_MINUTT = 60;
    private static final int TI_SEKUND = 10;

    private final LeaderElectionClient leaderElectionClient;
    private final FilterRepository filterRepository;
    private final ObjectMapper objectMapper;
    private final DefaultUnleash defaultUnleash;

    @Scheduled(initialDelay = ET_MINUTT, fixedRate = TI_SEKUND, timeUnit = TimeUnit.SECONDS)
    public void migrerFilterJobb() {
        if (leaderElectionClient.isLeader() && defaultUnleash.isEnabled(FeatureToggle.MIGRER_FILTER_JOBB_ENABLED)) {
            migrerFilter(DEFAULT_BATCHSTORRELSE_FOR_JOBB);
        }
    }

    public Optional<FilterMigreringResultat> migrerFilter(int batchStorrelseForJobb) {
        log.info("Filtermigrering - Batch startet");

        int antallFilterMedUtdaterteRegistreringstyper = filterRepository.hentMineFilterSomInneholderUtdaterteRegistreringstyper().size();

        if (antallFilterMedUtdaterteRegistreringstyper == 0) {
            log.info("Filtermigrering - Ingen filter å migrere");
            log.info("Filtermigrering - Batch fullført");
            return Optional.empty();
        }

        log.info("Filtermigrering - Totalt antall filter med utdaterte registreringstyper: {}", antallFilterMedUtdaterteRegistreringstyper);

        try {
            Optional<Migrert> resultatRegistreringstyperMigrering = antallFilterMedUtdaterteRegistreringstyper > 0 ? Optional.of(migrerFilterMedUtdaterteRegistreringstyper(batchStorrelseForJobb)) : Optional.empty();
            log.info("Filtermigrering - Batch fullført");

            return Optional.of(
                    new FilterMigreringResultat(
                            resultatRegistreringstyperMigrering.flatMap(it -> Optional.of(new FilterMigreringResultat.Resultat(antallFilterMedUtdaterteRegistreringstyper, it.forsokt, it.faktisk)))
                    )
            );
        } catch (RuntimeException e) {
            log.error("Filtermigrering - Noe gikk galt i migrering, se feilmelding i SecureLogs");
            secureLog.error("Filtermigrering - Noe gikk galt i migrering", e);

            return Optional.empty();
        }
    }

    public Migrert migrerFilterMedFiltertype(int batchStorrelse, String filterType) {
        List<FilterModel> filtreSomSkalMigreres = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(filterType, batchStorrelse);
        int forsoktMigrerteFilter = filtreSomSkalMigreres.size();

        int faktiskMigrerteFilter = 0;
        if (ARENA_HOVEDMAL_FILTERVALG_JSON_KEY.equals(filterType)) {
            faktiskMigrerteFilter = erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalgBatch(filtreSomSkalMigreres);
        }

        if (ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY.equals(filterType)) {
            faktiskMigrerteFilter = erstattArenaInnsatsgruppeMedInnsatsgruppeGjeldendeVedtak14aIFiltervalgBatch(filtreSomSkalMigreres);
        }

        log.info("Filtermigrering - Migrerte {} av {} filter i batch for {}.", faktiskMigrerteFilter, forsoktMigrerteFilter, filterType);

        return new Migrert(forsoktMigrerteFilter, faktiskMigrerteFilter);
    }

    public Migrert migrerFilterMedUtdaterteRegistreringstyper(int batchStorrelse) {
        List<FilterModel> filtreSomSkalMigreres = filterRepository.hentMineFilterSomInneholderUtdaterteRegistreringstyper(batchStorrelse);
        int forsoktMigrerteFilter = filtreSomSkalMigreres.size();

        int faktiskMigrerteFilter = 0;
        faktiskMigrerteFilter = erstattUtdaterteRegistreringstyperMedNyeRegistreringstyperFiltervalgBatch(filtreSomSkalMigreres);

        log.info("Filtermigrering - Migrerte {} av {} filter i batch for utdaterte registreringstyper.", faktiskMigrerteFilter, forsoktMigrerteFilter);

        return new Migrert(forsoktMigrerteFilter, faktiskMigrerteFilter);
    }

    public void erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(FilterModel filterSomSkalOppdateres) throws JsonProcessingException {
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

    public int erstattUtdaterteRegistreringstyperMedNyeRegistreringstyperFiltervalgBatch(List<FilterModel> filtreSomSkalOppdateres) {
        List<FilterRepository.FilterIdOgFilterValgPar> lagredeFilterMedMigrerteHovedmal = filtreSomSkalOppdateres.stream()
                .peek(this::migrerUtdaterteRegistreringstyperTilNyeRegistreringstyper)
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
        // Lag liste over migrerte innsatsgrupper
        List<String> innsatsgruppeFraArenaInnsatsgruppe = lagGjeldendeVedtakInnsatsgruppeFraArenaInnsatsgruppe(filterSomSkalOppdateres.getFilterValg().getInnsatsgruppe());

        // Slå saman nye og gamle InnsatsgruppeGjeldendeVedtak-filter
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

    private void migrerUtdaterteRegistreringstyperTilNyeRegistreringstyper(FilterModel filterSomSkalOppdateres) {
        // Lag liste over migrerte innsatsgrupper
        List<String> byttTilNyeRegistreringstyper = lagNyeRegistreringstyper(filterSomSkalOppdateres.getFilterValg().getRegistreringstype());

        // Fjern duplikat og sorter lista
        Set<String> alleUnikeFilter = new HashSet<>(byttTilNyeRegistreringstyper);
        List<String> unikeSorterteFilter = alleUnikeFilter.stream().sorted().toList();

        // Lag oppdatert porteføljefilter
        PortefoljeFilter portefoljeFilterSomSkalOppdateres = filterSomSkalOppdateres.getFilterValg();
        portefoljeFilterSomSkalOppdateres.setRegistreringstype(unikeSorterteFilter);

        // Lag klart oppdatert filtermodell og som skal skrivast tilbake til databasen
        filterSomSkalOppdateres.setFilterValg(portefoljeFilterSomSkalOppdateres);
    }

    private List<String> lagNyeRegistreringstyper(List<String> registreringstyper) {
        return registreringstyper.stream().map(this::mapUtdaterteRegistreringstyperTilNyeRegistreringstyper).toList();
    }

    private String mapUtdaterteRegistreringstyperTilNyeRegistreringstyper(String registreringstype) {
        return switch (registreringstype) {
            case "MISTET_JOBBEN" -> "HAR_BLITT_SAGT_OPP";
            case "JOBB_OVER_2_AAR" -> "IKKE_VAERT_I_JOBB_SISTE_2_AAR";
            case "VIL_FORTSETTE_I_JOBB" -> "ANNET";
            default -> registreringstype;
        };
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

    public record FilterMigreringResultat(
            Optional<Resultat> registreringstyper
    ) {

        public record Resultat(
                int totalt,
                int forsoktMigrert,
                int faktiskMigrert
        ) {
        }
    }

    public record Migrert(
            int forsokt,
            int faktisk
    ) {
    }
}
