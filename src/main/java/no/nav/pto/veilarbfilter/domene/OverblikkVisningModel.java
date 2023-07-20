package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverblikkVisningModel {

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String veilederID = null;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String overblikkVisning = null;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private LocalDateTime opprettet = null;
}
