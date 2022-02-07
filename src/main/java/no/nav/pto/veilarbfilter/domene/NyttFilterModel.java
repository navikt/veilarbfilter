package no.nav.pto.veilarbfilter.domene;

import lombok.Data;

@Data
public class NyttFilterModel {
    private final String filterNavn;
    private final PortefoljeFilter filterValg;
}
