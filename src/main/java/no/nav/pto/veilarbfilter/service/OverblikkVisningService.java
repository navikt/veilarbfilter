package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.OverblikkVisning;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import no.nav.pto.veilarbfilter.repository.OverblikkVisningRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverblikkVisningService {

    private final OverblikkVisningRepository overblikkVisningRepository;

    public Optional<OverblikkVisning> hentOverblikkVisning(VeilederId veilederId) {
        return overblikkVisningRepository.hent(veilederId);
    }

    public void lagreOverblikkVisning(VeilederId veilederId, List<String> overblikkVisning) {
        overblikkVisningRepository.lagre(veilederId, overblikkVisning);
    }

    public void slettOverblikkVisning(VeilederId veilederId) {
        overblikkVisningRepository.slett(veilederId);
    }
}
