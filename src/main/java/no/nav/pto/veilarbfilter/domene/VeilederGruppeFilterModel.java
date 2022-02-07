package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VeilederGruppeFilterModel extends FilterModel {
    private Integer filterId;
    private String filterNavn;
    private PortefoljeFilter filterValg;
    private LocalDateTime opprettetDato;
    private Integer filterCleanup;
    private String enhetId;

    public VeilederGruppeFilterModel(Integer filterId, String filterNavn, PortefoljeFilter filterValg, LocalDateTime opprettetDato, Integer filterCleanup, String enhetId) {
        super(filterId, filterNavn, filterValg, opprettetDato, filterCleanup);
        this.filterId = filterId;
        this.filterNavn = filterNavn;
        this.filterValg = filterValg;
        this.opprettetDato = opprettetDato;
        this.filterCleanup = filterCleanup;
        this.enhetId = enhetId;
    }
}
