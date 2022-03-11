package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class FilterModel {
    private Integer filterId = null;
    private String filterNavn = null;
    private PortefoljeFilter filterValg = null;
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    private LocalDateTime opprettetDato = null;
    private Integer filterCleanup = 0;
}
