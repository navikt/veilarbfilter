package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VeilederGrupperService implements FilterService {
    private final VeilederGruppeFilterRepository veilederGruppeFilterRepository;
    private final VeilarbveilederClient veilarbveilederClient;

    @Override
    public Optional<FilterModel> lagreFilter(String enhetId, NyttFilterModel nyttFilter) throws IllegalArgumentException {
        try {
            return veilederGruppeFilterRepository.lagreFilter(enhetId, nyttFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String enhetId, FilterModel filter) throws IllegalArgumentException {
        try {
            return veilederGruppeFilterRepository.oppdaterFilter(enhetId, filter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public Optional<FilterModel> hentFilter(Integer filterId) {
        return veilederGruppeFilterRepository.hentFilter(filterId);
    }

    @Override
    @SneakyThrows
    public List<FilterModel> finnFilterForFilterBruker(String enhetId) {
        return veilederGruppeFilterRepository.finnFilterForFilterBruker(enhetId);
    }

    @Override
    @SneakyThrows
    public Integer slettFilter(Integer filterId, String enhetId) {
        return veilederGruppeFilterRepository.slettFilter(filterId, enhetId);
    }

    @Override
    @SneakyThrows
    public List<FilterModel> lagreSortering(String enhetId, List<SortOrder> sortOrder) {
        return veilederGruppeFilterRepository.lagreSortering(enhetId, sortOrder);
    }

    public List<String> hentAlleEnheter() {
        return veilederGruppeFilterRepository.hentAlleEnheter();
    }

    public void slettVeiledereSomIkkeErAktivePaEnheten(String enhetId) {
        List<String> veilederePaEnheten = veilarbveilederClient.hentVeilederePaaEnhet(EnhetId.of(enhetId));
        List<FilterModel> filterForBruker = finnFilterForFilterBruker(enhetId);

        filterForBruker.forEach(filter -> {
            // aktive valgte filtere
            String aktiveFilterValgJson = filter.getAktiveFilterValg();
            if (aktiveFilterValgJson == null || aktiveFilterValgJson.isEmpty()) {
                return;
            }

            JsonNode aktiveFilterValgNode = JsonUtils.getMapper().valueToTree(
                    JsonUtils.fromJson(aktiveFilterValgJson, Object.class));
            List<String> alleVeiledere = hentVeiledereFraJson(aktiveFilterValgNode);
            List<String> aktiveVeileder = alleVeiledere.stream().filter(veilederePaEnheten::contains).collect(Collectors.toList());

            String removedVeileder = getRemovedVeiledere(alleVeiledere, aktiveVeileder);

            if (aktiveVeileder.isEmpty()) {
                log.warn("Removed veiledere: " + removedVeileder);
                slettFilter(filter.getFilterId(), enhetId);
                log.warn("Removed veiledergruppe: " + filter.getFilterNavn() + " from enhet: " + enhetId);
            } else if (aktiveVeileder.size() < alleVeiledere.size()) {
                log.warn("Removed veiledere: " + removedVeileder);

                // opprinnelige valgte filtre - slettes senere:
                PortefoljeFilter filterValg = filter.getFilterValg();
                filterValg.setVeiledere(aktiveVeileder);

                //aktive filtre
                String oppdatertAktiveFilterValg = medOppdaterteVeiledere(aktiveFilterValgNode, aktiveVeileder);
                VeilederGruppeFilterModel updatedVeilederGruppeFilterModel = new VeilederGruppeFilterModel(
                        filter.getFilterId(),
                        filter.getFilterNavn(),
                        filterValg,
                        oppdatertAktiveFilterValg,
                        filter.getOpprettetDato(),
                        1,
                        enhetId);
                oppdaterFilter(enhetId, updatedVeilederGruppeFilterModel);
                log.warn("Updated veiledergruppe: " + filter.getFilterNavn() + " from enhet: " + enhetId);
            }
        });
    }

    private List<String> hentVeiledereFraJson(JsonNode aktiveFilterValgNode) {
        List<String> veiledere = new ArrayList<>();
        JsonNode veiledereNode = aktiveFilterValgNode.get("veiledere");
        if (veiledereNode != null && veiledereNode.isArray()) {
            veiledereNode.forEach(node -> veiledere.add(node.stringValue()));
        }
        return veiledere;
    }

    private String medOppdaterteVeiledere(JsonNode aktiveFilterValgNode, List<String> aktiveVeileder) {
        ObjectNode objectNode = (ObjectNode) aktiveFilterValgNode;
        ArrayNode veiledereArray = JsonUtils.getMapper().createArrayNode();
        aktiveVeileder.forEach(veiledereArray::add);
        objectNode.set("veiledere", veiledereArray);
        return JsonUtils.toJson(objectNode);
    }

    private String getRemovedVeiledere(List<String> alleVeiledere, List<String> aktiveVeileder) {
        return alleVeiledere.stream()
                .filter(veilederIdent -> !aktiveVeileder.contains(veilederIdent))
                .collect(Collectors.joining(", "));
    }

}
