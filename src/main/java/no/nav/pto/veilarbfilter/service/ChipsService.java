package no.nav.pto.veilarbfilter.service;

import no.nav.pto.veilarbfilter.domene.ChipsModel;
import no.nav.pto.veilarbfilter.domene.NyttChipsModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;

import java.util.List;
import java.util.Optional;

public interface ChipsService {

    Optional<ChipsModel> hentVisning(String veilederId);
    void lagreOgOppdater(String veilederId, List<String> detaljerVisning) throws Exception;

    void slettVisning(String veilederId);
}
