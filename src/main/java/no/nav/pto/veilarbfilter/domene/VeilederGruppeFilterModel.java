package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Data
public class VeilederGruppeFilterModel extends FilterModel {
    private String enhetId;

    public VeilederGruppeFilterModel(Integer filterId, String filterNavn, PortefoljeFilter filterValg, LocalDateTime opprettetDato, Integer filterCleanup, String enhetId) {
        super(filterId, filterNavn, filterValg, opprettetDato, filterCleanup);
        this.enhetId = enhetId;
    }
}
