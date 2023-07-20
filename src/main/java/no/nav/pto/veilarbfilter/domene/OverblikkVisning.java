package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import static java.util.Collections.emptyList;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverblikkVisning {

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Setter
    private List<String> overblikkVisning = emptyList();
    private String veilederID = "";

    @JsonIgnore
    public Boolean isNotEmpty() {
        return  (veilederID != null && !veilederID.isEmpty()) ||
                (overblikkVisning != null && !overblikkVisning.isEmpty());
    }
}
