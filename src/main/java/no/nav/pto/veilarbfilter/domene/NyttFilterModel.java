package no.nav.pto.veilarbfilter.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
public class NyttFilterModel {
    private String filterNavn;
    private PortefoljeFilter filterValg;
}
