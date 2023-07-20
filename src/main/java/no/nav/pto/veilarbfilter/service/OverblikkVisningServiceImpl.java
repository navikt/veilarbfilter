package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningModel;
import no.nav.pto.veilarbfilter.repository.OverblikkVisningRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverblikkVisningServiceImpl implements OverblikkVisningService {

    private final OverblikkVisningRepository overblikkVisningRepository;

    @Override
    public Optional<OverblikkVisningModel> hentVisning(String veilederId) {
        return overblikkVisningRepository.hentVisning(veilederId);
    }

    public void lagreOgOppdater(String veilederId, List<String> detaljerVisning) {
        try{
            Optional<OverblikkVisningModel> lagretVisning = overblikkVisningRepository.hentVisning(veilederId);
            if (lagretVisning.isEmpty()) {
                overblikkVisningRepository.lagreVisning(veilederId, detaljerVisning);
            } else {
                overblikkVisningRepository.oppdaterVisning(veilederId, detaljerVisning);
            }
        }
        catch (Exception e) {
            log.error("Kan ikke lagre og oppdatere visning " + e, e);
            throw e;
        }
    }

    @Override
    public void slettVisning(String veilederId) {
        overblikkVisningRepository.slettVisning(veilederId);
    }

}
