package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.ChipsModel;
import no.nav.pto.veilarbfilter.domene.MineLagredeChipsModel;
import no.nav.pto.veilarbfilter.domene.NyttChipsModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;
import no.nav.pto.veilarbfilter.repository.MineLagredeChipsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MineLagredeChipsService implements ChipsService {
    private final MineLagredeChipsRepository mineLagredeChipsRepository;

    @Override
    public Optional<ChipsModel> lagreChips(String veilederId, NyttChipsModel nyttChips) throws IllegalArgumentException {
        try {
            return mineLagredeChipsRepository.lagreChips(veilederId, nyttChips);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<ChipsModel> oppdaterChips(String veilederId, ChipsModel chips) throws IllegalArgumentException {
        try {
            return mineLagredeChipsRepository.oppdaterChips(veilederId, chips);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<ChipsModel> hentChips(Integer chipsId) {
        return mineLagredeChipsRepository.hentChips(chipsId);
    }

    @Override
    public Integer slettChips(Integer chipsId, String veilederId) {
        return mineLagredeChipsRepository.slettChips(chipsId, veilederId);
    }
}
