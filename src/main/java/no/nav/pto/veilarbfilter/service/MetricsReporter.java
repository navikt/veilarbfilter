package no.nav.pto.veilarbfilter.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsReporter implements MeterBinder {
    private final MineLagredeFilterService mineLagredeFilterService;

    private MultiGauge lagredeFilterStats;

    @Override
    public void bindTo(@NotNull MeterRegistry meterRegistry) {
        if (lagredeFilterStats == null) {
            lagredeFilterStats = MultiGauge.builder("portefolje_lagredefilter")
                    .description("Stats for lagrede filter")
                    .register(meterRegistry);
        }
    }

    public void reportLagradeFilter() {
        try {
            Map<String, Integer> stats = new HashMap<>();

            log.info("Reporting metrics...");

            mineLagredeFilterService.hentAllLagredeFilter().forEach(filterModel -> {

                PortefoljeFilter filterValg = filterModel.getFilterValg();
                if (filterValg.getAktiviteter() != null) {
                    incrementFilterStats(stats, "aktiviteter");
                    if (!"NA".equals(filterValg.getAktiviteter().getBEHANDLING())) {
                        incrementFilterStats(stats, "behandling");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getEGEN())) {
                        incrementFilterStats(stats, "egen");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getGRUPPEAKTIVITET())) {
                        incrementFilterStats(stats, "gruppeaktivitet");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getIJOBB())) {
                        incrementFilterStats(stats, "ijobb");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getMOTE())) {
                        incrementFilterStats(stats, "mote");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getSOKEAVTALE())) {
                        incrementFilterStats(stats, "sokeavtale");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getSTILLING())) {
                        incrementFilterStats(stats, "stilling");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getTILTAK())) {
                        incrementFilterStats(stats, "tiltak");
                    }
                    if (!"NA".equals(filterValg.getAktiviteter().getUTDANNINGAKTIVITET())) {
                        incrementFilterStats(stats, "utdanningaktivitet");
                    }
                }
                if (filterValg.getAlder() != null && !filterValg.getAlder().isEmpty()) {
                    incrementFilterStats(stats, "alder");
                }
                if (filterValg.getFerdigfilterListe() != null && !filterValg.getFerdigfilterListe().isEmpty()) {
                    incrementFilterStats(stats, "ferdigfilterliste");
                    filterValg.getFerdigfilterListe().forEach(x -> incrementFilterStats(stats, x));
                }
                if (filterValg.getFodselsdagIMnd() != null && !filterValg.getFodselsdagIMnd().isEmpty()) {
                    incrementFilterStats(stats, "fodselsdagimnd");
                }
                if (filterValg.getFormidlingsgruppe() != null && !filterValg.getFormidlingsgruppe().isEmpty()) {
                    incrementFilterStats(stats, "formidlingsgruppe");
                }
                if (filterValg.getKjonn() != null && !filterValg.getKjonn().isEmpty()) {
                    incrementFilterStats(stats, "kjonn");
                }
                if (filterValg.getManuellBrukerStatus() != null && !filterValg.getManuellBrukerStatus().isEmpty()) {
                    incrementFilterStats(stats, "manuellbrukerstatus");
                }
                if (filterValg.getRettighetsgruppe() != null && !filterValg.getRettighetsgruppe().isEmpty()) {
                    incrementFilterStats(stats, "rettighetsgruppe");
                }
                if (filterValg.getServicegruppe() != null && !filterValg.getServicegruppe().isEmpty()) {
                    incrementFilterStats(stats, "servicegruppe");
                }
                if (filterValg.getTiltakstyper() != null && !filterValg.getTiltakstyper().isEmpty()) {
                    incrementFilterStats(stats, "tiltakstyper");
                }
                if (filterValg.getVeilederNavnQuery() != null && !filterValg.getVeilederNavnQuery().isEmpty()) {
                    incrementFilterStats(stats, "veiledernavnquery");
                }
                if (filterValg.getVeiledere() != null && !filterValg.getVeiledere().isEmpty()) {
                    incrementFilterStats(stats, "veiledere");
                }
                if (filterValg.getRegistreringstype() != null && !filterValg.getRegistreringstype().isEmpty()) {
                    incrementFilterStats(stats, "registreringstype");
                }
                if (filterValg.getCvJobbprofil() != null && !filterValg.getCvJobbprofil().isEmpty()) {
                    incrementFilterStats(stats, "cvjobbprofil");
                }
                if (filterValg.getLandgruppe() != null && !filterValg.getLandgruppe().isEmpty()) {
                    incrementFilterStats(stats, "landgruppe");
                }
                if (filterValg.getFoedeland() != null && !filterValg.getFoedeland().isEmpty()) {
                    incrementFilterStats(stats, "foedeland");
                }
                if (filterValg.getBarnUnder18Aar() != null && !filterValg.getBarnUnder18Aar().isEmpty()) {
                    incrementFilterStats(stats, "barnunder18aar");
                }
                if (filterValg.getTolkebehov() != null && !filterValg.getTolkebehov().isEmpty()) {
                    incrementFilterStats(stats, "tolkbehov");
                }
                if (filterValg.getEnsligeForsorgere() != null && !filterValg.getEnsligeForsorgere().isEmpty()) {
                    incrementFilterStats(stats, "ensligeforsorgere");
                }
                if (filterValg.getFargekategorier() != null && !filterValg.getFargekategorier().isEmpty()) {
                    incrementFilterStats(stats, "fargekategorier");
                }
            });
            lagredeFilterStats.register(stats.entrySet().stream().map(entry -> MultiGauge.Row.of(Tags.of("filterNavn", entry.getKey()), entry.getValue())).collect(Collectors.toList()), true);
        } catch (Exception e) {
            log.error("Can not report metrics " + e, e);
        }
    }

    private void incrementFilterStats(Map<String, Integer> stats, String statName) {
        if (stats.containsKey(statName)) {
            stats.put(statName, stats.get(statName) + 1);
        } else {
            stats.put(statName, 1);
        }
    }
}
