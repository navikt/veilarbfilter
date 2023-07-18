package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.ChipsModel;
import no.nav.pto.veilarbfilter.domene.NyttChipsModel;
import no.nav.pto.veilarbfilter.repository.MineLagredeChipsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChipsServiceImpl implements ChipsService {

    private final MineLagredeChipsRepository mineLagredeChipsRepository;

    @Override
    public Optional<ChipsModel> hentVisning(String veilederId) {
        return mineLagredeChipsRepository.hentVisning(veilederId);
    }

    public void lagreOgOppdater(String veilederId, List<String> detaljerVisning) {
        try{
            Optional<ChipsModel> lagretVisning = mineLagredeChipsRepository.hentVisning(veilederId);
            if (lagretVisning.isEmpty()) {
                mineLagredeChipsRepository.lagreVisning(veilederId, detaljerVisning);
            } else {
                mineLagredeChipsRepository.oppdaterVisning(veilederId, detaljerVisning);
            }
        }
        catch (Exception e) {
            log.error("Kan ikke lagre og oppdatere visning " + e, e);
            throw e;
        }
    }

    @Override
    public void slettVisning(String veilederId) {
        mineLagredeChipsRepository.slettVisning(veilederId);
    }

}
