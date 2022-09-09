package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsReporter {
    private final MineLagredeFilterService mineLagredeFilterService;
    private final MetricsClient metricsClient;

    public void reportLagradeFilter() {
        log.info("Reporting metrics...");

        mineLagredeFilterService.hentAllLagredeFilter().forEach(filterModel -> {
            Event metrikk = new Event("portefolje.metrikker.lagredefilter.veileder-filter-counter");
            metrikk.addFieldToReport("navn-lengde", filterModel.getFilterNavn().length());
            metrikk.addTagToReport("id", getHash(filterModel.getVeilederId()));
            metrikk.addFieldToReport("filterId", filterModel.getFilterId());
            PortefoljeFilter filterValg = filterModel.getFilterValg();
            var antallFiltre = 0;

            if (filterValg.getAktiviteter() != null) {
                metrikk.addTagToReport("aktiviteter", "1");
                if (!"NA".equals(filterValg.getAktiviteter().getBEHANDLING())) {
                    metrikk.addTagToReport("BEHANDLING", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getEGEN())) {
                    metrikk.addTagToReport("EGEN", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getGRUPPEAKTIVITET())) {
                    metrikk.addTagToReport("GRUPPEAKTIVITET", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getIJOBB())) {
                    metrikk.addTagToReport("IJOBB", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getMOTE())) {
                    metrikk.addTagToReport("MOTE", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getSOKEAVTALE())) {
                    metrikk.addTagToReport("SOKEAVTALE", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getSTILLING())) {
                    metrikk.addTagToReport("STILLING", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getTILTAK())) {
                    metrikk.addTagToReport("TILTAK", "1");
                    antallFiltre++;
                }
                if (!"NA".equals(filterValg.getAktiviteter().getUTDANNINGAKTIVITET())) {
                    metrikk.addTagToReport("UTDANNINGAKTIVITET", "1");
                    antallFiltre++;
                }
            }
            if (filterValg.getAlder() != null && !filterValg.getAlder().isEmpty()) {
                metrikk.addTagToReport("alder", "1");
                addValuesAsTags(metrikk, filterValg.getAlder());
                antallFiltre += filterValg.getAlder().size();
            }
            if (filterValg.getFerdigfilterListe() != null && !filterValg.getFerdigfilterListe().isEmpty()) {
                metrikk.addTagToReport("ferdigfilterListe", "1");
                addValuesAsTags(metrikk, filterValg.getFerdigfilterListe());
                antallFiltre += filterValg.getFerdigfilterListe().size();
            }
            if (filterValg.getFodselsdagIMnd() != null && !filterValg.getFodselsdagIMnd().isEmpty()) {
                metrikk.addTagToReport("fodselsdagIMnd", "1");
                antallFiltre += filterValg.getFodselsdagIMnd().size();
            }
            if (filterValg.getFormidlingsgruppe() != null && !filterValg.getFormidlingsgruppe().isEmpty()) {
                metrikk.addTagToReport("formidlingsgruppe", "1");
                addValuesAsTags(metrikk, filterValg.getFormidlingsgruppe());
                antallFiltre += filterValg.getFormidlingsgruppe().size();
            }
            if (filterValg.getHovedmal() != null && !filterValg.getHovedmal().isEmpty()) {
                metrikk.addTagToReport("hovedmal", "1");
                addValuesAsTags(metrikk, filterValg.getHovedmal());
                antallFiltre += filterValg.getHovedmal().size();
            }
            if (filterValg.getInnsatsgruppe() != null && !filterValg.getInnsatsgruppe().isEmpty()) {
                metrikk.addTagToReport("innsatsgruppe", "1");
                addValuesAsTags(metrikk, filterValg.getInnsatsgruppe());
                antallFiltre += filterValg.getInnsatsgruppe().size();
            }
            if (filterValg.getKjonn() != null && !filterValg.getKjonn().isEmpty()) {
                metrikk.addTagToReport("kjonn", "1");
                metrikk.addTagToReport(filterValg.getKjonn(), "1");
                antallFiltre++;
            }
            if (filterValg.getManuellBrukerStatus() != null && !filterValg.getManuellBrukerStatus().isEmpty()) {
                metrikk.addTagToReport("manuellBrukerStatus", "1");
                addValuesAsTags(metrikk, filterValg.getManuellBrukerStatus());
                antallFiltre += filterValg.getManuellBrukerStatus().size();
            }
            if (filterValg.getRettighetsgruppe() != null && !filterValg.getRettighetsgruppe().isEmpty()) {
                metrikk.addTagToReport("rettighetsgruppe", "1");
                addValuesAsTags(metrikk, filterValg.getRettighetsgruppe());
                antallFiltre += filterValg.getRettighetsgruppe().size();
            }
            if (filterValg.getServicegruppe() != null && !filterValg.getServicegruppe().isEmpty()) {
                metrikk.addTagToReport("servicegruppe", "1");
                addValuesAsTags(metrikk, filterValg.getServicegruppe());
                antallFiltre += filterValg.getServicegruppe().size();
            }
            if (filterValg.getTiltakstyper() != null && !filterValg.getTiltakstyper().isEmpty()) {
                metrikk.addTagToReport("tiltakstyper", "1");
                addValuesAsTags(metrikk, filterValg.getTiltakstyper());
                antallFiltre += filterValg.getTiltakstyper().size();
            }
            if (filterValg.getVeilederNavnQuery() != null && !filterValg.getVeilederNavnQuery().isEmpty()) {
                metrikk.addTagToReport("veilederNavnQuery", "1");
                antallFiltre++;
            }
            if (filterValg.getVeiledere() != null && !filterValg.getVeiledere().isEmpty()) {
                metrikk.addTagToReport("veiledere", "1");
                antallFiltre += filterValg.getVeiledere().size();
            }
            if (filterValg.getYtelse() != null && !filterValg.getYtelse().isEmpty()) {
                metrikk.addTagToReport("ytelse", "1");
                metrikk.addTagToReport(filterValg.getYtelse(), "1");
                antallFiltre++;
            }
            if (filterValg.getRegistreringstype() != null && !filterValg.getRegistreringstype().isEmpty()) {
                metrikk.addTagToReport("registreringstype", "1");
                addValuesAsTags(metrikk, filterValg.getRegistreringstype());
                antallFiltre += filterValg.getRegistreringstype().size();
            }
            if (filterValg.getCvJobbprofil() != null && !filterValg.getCvJobbprofil().isEmpty()) {
                metrikk.addTagToReport("cvJobbprofil", "1");
                metrikk.addTagToReport(filterValg.getCvJobbprofil(), "1");
                antallFiltre++;
            }
            if (filterValg.getArbeidslisteKategori() != null && !filterValg.getArbeidslisteKategori().isEmpty()) {
                metrikk.addTagToReport("arbeidslisteKategori", "1");
                antallFiltre += filterValg.getArbeidslisteKategori().size();
            }
            if (filterValg.getLandgruppe() != null && !filterValg.getLandgruppe().isEmpty()) {
                metrikk.addTagToReport("landgruppe", "1");
                addValuesAsTags(metrikk, filterValg.getLandgruppe());
                antallFiltre += filterValg.getLandgruppe().size();
            }
            if (filterValg.getTolkebehov() != null && !filterValg.getTolkebehov().isEmpty()) {
                metrikk.addTagToReport("tolkbehov", "1");
                addValuesAsTags(metrikk, filterValg.getTolkebehov());
                antallFiltre += filterValg.getTolkebehov().size();
            }
            metrikk.addFieldToReport("antallFiltre", antallFiltre);
            metricsClient.report(metrikk);

        });
    }

    private void addValuesAsTags(Event metrikk, List<String> listValues) {
        listValues.forEach(x -> metrikk.addTagToReport(x, "1"));
    }

    private String getHash(String veilederId) {
        return DigestUtils.md5Hex(veilederId);
    }
}
