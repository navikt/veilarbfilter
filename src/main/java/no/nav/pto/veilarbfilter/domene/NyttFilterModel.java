package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
@JsonDeserialize
@NoArgsConstructor
public class NyttFilterModel {
    private String filterNavn;
    private PortefoljeFilter filterValg;
}
