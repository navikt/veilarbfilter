package no.nav.pto.veilarbfilter.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.database.Table.VeilederGrupperFilter;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.util.DateUtils;
import no.nav.pto.veilarbfilter.util.JsonUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.pto.veilarbfilter.database.Table.Filter;
import static no.nav.pto.veilarbfilter.database.Table.MineLagredeFilter;

@Service
@Slf4j
@RequiredArgsConstructor
public class VeilederGruppeFilterRepository implements FilterService {
    private final JdbcTemplate db;
    private final MineLagredeFilterRepository mineLagredeFilterRepository;
    private final VeilarbveilederClient veilarbveilederClient;


    public Optional<FilterModel> lagreFilter(String enhetId, NyttFilterModel nyttFilterModel) {
        var key = 0;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        String insertSql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET);
        int affectedRows = db.update(insertSql, nyttFilterModel.getFilterNavn(), nyttFilterModel.getFilterValg(), LocalDateTime.now(), keyHolder, new String[]{"FILTER_ID"});

        if (affectedRows > 0) {
            key = keyHolder.getKey().intValue();

            insertSql = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                    VeilederGrupperFilter.TABLE_NAME, Filter.FILTER_ID, VeilederGrupperFilter.ENHET_ID);

            db.update(insertSql, key, enhetId);
        }

        return hentFilter(key);
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String enhetId, FilterModel filter) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", VeilederGrupperFilter.TABLE_NAME, VeilederGrupperFilter.ENHET_ID, VeilederGrupperFilter.FILTER_ID);
        Integer numOfRows = db.queryForObject(sql, Integer.class, enhetId, filter.getFilterId());

        if (numOfRows > 0) {
            sql = String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?", Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.FILTER_ID);
            db.update(sql, filter.getFilterNavn(), JsonUtils.serializeFilterValg(filter.getFilterValg()), filter.getFilterId());
        }

        return hentFilter(filter.getFilterId());
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        try {
            String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.filter_id = f.filter_id AND f.filter_id = ?",
                    VeilederGrupperFilter.TABLE_NAME, Filter.TABLE_NAME);
            FilterModel veilederGruppeFilterModel = db.queryForObject(sql, (rs, rowNum) -> {
                        PortefoljeFilter portefoljeFilter = JsonUtils.deserializeFilterValg(rs.getString(Filter.VALGTE_FILTER));
                        return new VeilederGruppeFilterModel(rs.getInt(VeilederGrupperFilter.FILTER_ID),
                                rs.getString(Filter.FILTER_NAVN),
                                portefoljeFilter,
                                DateUtils.toLocalDateTimeOrNull(rs.getString(Filter.OPPRETTET)),
                                rs.getInt(Filter.FILTER_CLEANUP),
                                rs.getString(VeilederGrupperFilter.ENHET_ID));
                    }
                    , filterId);
            return Optional.of(veilederGruppeFilterModel);
        } catch (Exception e) {
            log.warn("Can't find filter " + e, e);
            return Optional.empty();
        }
    }

    public List<FilterModel> finnFilterForFilterBruker(String enhetId) {

        String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.filter_id = f.filter_id AND and ml.%s = %s",
                MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, VeilederGrupperFilter.ENHET_ID, enhetId);

        return db.query(sql, (rs, rowNum) ->
                new VeilederGruppeFilterModel(rs.getInt(VeilederGrupperFilter.FILTER_ID),
                        rs.getString(Filter.FILTER_NAVN),
                        JsonUtils.deserializeFilterValg(rs.getString(Filter.VALGTE_FILTER)),
                        DateUtils.toLocalDateTimeOrNull(rs.getString(Filter.OPPRETTET)),
                        rs.getInt(Filter.FILTER_CLEANUP),
                        rs.getString(VeilederGrupperFilter.ENHET_ID)));
    }

    @Override
    public Integer slettFilter(Integer filterId, String enhetId) {
        Optional<FilterModel> filterModelOptional = hentFilter(filterId);

        if (filterModelOptional.isPresent()) {
            String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
                    VeilederGrupperFilter.TABLE_NAME, VeilederGrupperFilter.FILTER_ID, VeilederGrupperFilter.ENHET_ID);

            int numOfRowsUpdated = db.update(sql, filterId, enhetId);

            if (numOfRowsUpdated > 0) {
                sql = String.format("DELETE FROM %s WHERE %s = ?",
                        Filter.FILTER_ID);
                int rowsUpdated = db.update(sql, filterId);

                mineLagredeFilterRepository.deactivateMineFilterWithDeletedVeilederGroup(filterModelOptional.get().getFilterNavn(), filterModelOptional.get().getFilterValg().getVeiledere());
                return rowsUpdated;
            }
        }

        return 0;
    }

    @Override
    public List<FilterModel> lagreSortering(String enhetId, List<SortOrder> sortOrder) {
        //TODO("Not yet implemented");
        return Collections.emptyList();
    }

    public void slettVeiledereSomIkkeErAktivePaEnheten(String enhetId) {
        List<String> veilederePaEnheten = veilarbveilederClient.hentVeilederePaaEnhet(EnhetId.of(enhetId));

        List<FilterModel> filterForBruker = finnFilterForFilterBruker(enhetId);

        filterForBruker.stream().forEach(filter -> {
            List<String> alleVeiledere = filter.getFilterValg().getVeiledere();
            List<String> aktiveVeileder = alleVeiledere.stream().filter(veilederIdent -> veilederePaEnheten.contains(veilederIdent)).collect(Collectors.toList());

            String removedVeileder = getRemovedVeiledere(alleVeiledere, aktiveVeileder);

            if (aktiveVeileder.isEmpty()) {
                log.warn("Removed veiledere: " + removedVeileder);
                slettFilter(filter.getFilterId(), enhetId);
                log.warn("Removed veiledergruppe: " + filter.getFilterNavn() + " from enhet: " + enhetId);
            } else if (aktiveVeileder.size() < alleVeiledere.size()) {
                log.warn("Removed veiledere: " + removedVeileder);

                PortefoljeFilter filterValg = filter.getFilterValg();
                filterValg.setVeiledere(aktiveVeileder);
                VeilederGruppeFilterModel updatedVeilederGruppeFilterModel = new VeilederGruppeFilterModel(filter.getFilterId(), filter.getFilterNavn(), filterValg, filter.getOpprettetDato(), 1, enhetId);
                oppdaterFilter(enhetId, updatedVeilederGruppeFilterModel);
                log.warn("Updated veiledergruppe: " + filter.getFilterNavn() + " from enhet: " + enhetId);
            }
        });
    }

    private String getRemovedVeiledere(List<String> alleVeiledere, List<String> aktiveVeileder) {
        return alleVeiledere.stream()
                .filter(veilederIdent -> !aktiveVeileder.contains(veilederIdent))
                .collect(Collectors.joining(", "));
    }
}

