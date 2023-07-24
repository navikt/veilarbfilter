package no.nav.pto.veilarbfilter.service;

import no.nav.pto.veilarbfilter.domene.OverblikkVisningModel;
import java.util.List;
import java.util.Optional;

public interface OverblikkVisningService {

    Optional<OverblikkVisningModel> hentVisning(String veilederId);
    void lagreOgOppdater(String veilederId, List<String> detaljerVisning) throws Exception;

    void slettVisning(String veilederId) throws Exception;
}
