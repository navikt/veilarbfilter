package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
@JsonDeserialize
public class NyttFilterModel {
    private String filterNavn;
    private PortefoljeFilter filterValg;
}
