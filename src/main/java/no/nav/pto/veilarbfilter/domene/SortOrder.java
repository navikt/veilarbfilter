package no.nav.pto.veilarbfilter.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortOrder {
    private Integer filterId;
    private Integer sortOrder;
}
