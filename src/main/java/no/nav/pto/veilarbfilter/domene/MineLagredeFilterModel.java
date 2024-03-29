package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Data
public class MineLagredeFilterModel extends FilterModel {
    private String veilederId = null;
    private Integer sortOrder = null;
    private Boolean aktiv = null;
    private String note = null;

    public MineLagredeFilterModel(Integer filterId, String filterNavn, PortefoljeFilter filterValg, LocalDateTime opprettetDato, Integer filterCleanup, String veilederId, Integer sortOrder, Boolean aktiv, String note) {
        super(filterId, filterNavn, filterValg, opprettetDato, filterCleanup);
        this.veilederId = veilederId;
        this.sortOrder = sortOrder;
        this.aktiv = aktiv;
        this.note = note;
    }
}
