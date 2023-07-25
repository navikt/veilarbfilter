package no.nav.pto.veilarbfilter.domene;

import java.util.List;
import java.util.UUID;

public record OverblikkVisning(
        UUID overblikkVisningId,
        List<String> visning
) {
}
