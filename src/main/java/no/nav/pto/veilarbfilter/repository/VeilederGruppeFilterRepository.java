package no.nav.pto.veilarbfilter.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.database.Table.VeilederGrupperFilter;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.util.DateUtils;
import no.nav.pto.veilarbfilter.util.JsonUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.pto.veilarbfilter.database.Table.Filter;
import static no.nav.pto.veilarbfilter.util.DateUtils.fromLocalDateTimeToTimestamp;

@Service
@Slf4j
@RequiredArgsConstructor
public class VeilederGruppeFilterRepository implements FilterService {
    private final JdbcTemplate db;
    private final MineLagredeFilterRepository mineLagredeFilterRepository;


    public Optional<FilterModel> lagreFilter(String enhetId, NyttFilterModel nyttFilterModel) {
        var key = 0;

        String insertSql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, to_json(?::JSON), ?)",
                Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET);

        Integer updateCount = db.execute(insertSql, (PreparedStatementCallback<Integer>) ps -> {
            ps.setString(1, nyttFilterModel.getFilterNavn());
            ps.setString(2, JsonUtils.serializeFilterValg(nyttFilterModel.getFilterValg()));
            ps.setTimestamp(3, fromLocalDateTimeToTimestamp(LocalDateTime.now()));
            return ps.executeUpdate();
        });

        if (updateCount > 0) {
            String lastId = String.format("SELECT MAX(%s) FROM %s",
                    Filter.FILTER_ID, Filter.TABLE_NAME);
            key = db.queryForObject(lastId, Integer.class);

            insertSql = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                    VeilederGrupperFilter.TABLE_NAME, VeilederGrupperFilter.FILTER_ID, VeilederGrupperFilter.ENHET_ID);

            db.update(insertSql, key, enhetId);
        }

        return hentFilter(key);
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String enhetId, FilterModel filter) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", VeilederGrupperFilter.TABLE_NAME, VeilederGrupperFilter.ENHET_ID, VeilederGrupperFilter.FILTER_ID);
        Integer numOfRows = db.queryForObject(sql, Integer.class, enhetId, filter.getFilterId());

        if (numOfRows > 0) {
            sql = String.format("UPDATE %s SET %s = ?, %s = to_json(?::JSON), %s = ? WHERE %s = ?", Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.FILTER_CLEANUP, Filter.FILTER_ID);
            db.update(sql, filter.getFilterNavn(), JsonUtils.serializeFilterValg(filter.getFilterValg()), filter.getFilterCleanup(), filter.getFilterId());
        }

        return hentFilter(filter.getFilterId());
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        try {
            String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.%s = f.%s AND f.filter_id = ?",
                    VeilederGrupperFilter.TABLE_NAME, Filter.TABLE_NAME, VeilederGrupperFilter.FILTER_ID, Filter.FILTER_ID);
            return Optional.of(db.queryForObject(sql, (rs, rowNum) -> {
                        PortefoljeFilter portefoljeFilter = JsonUtils.deserializeFilterValg(rs.getString(Filter.VALGTE_FILTER));
                        return new VeilederGruppeFilterModel(rs.getInt(VeilederGrupperFilter.FILTER_ID),
                                rs.getString(Filter.FILTER_NAVN),
                                portefoljeFilter,
                                DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                                rs.getInt(Filter.FILTER_CLEANUP),
                                rs.getString(VeilederGrupperFilter.ENHET_ID));
                    }
                    , filterId));
        } catch (Exception e) {
            log.warn("Can't find filter " + e, e);
            return Optional.empty();
        }
    }

    public List<FilterModel> finnFilterForFilterBruker(String enhetId) {

        String sql = String.format("SELECT * FROM %s AS ml, %s AS f WHERE ml.%s = f.%s AND ml.%s = \'%o\'",
                VeilederGrupperFilter.TABLE_NAME, Filter.TABLE_NAME, VeilederGrupperFilter.FILTER_ID, Filter.FILTER_ID, VeilederGrupperFilter.ENHET_ID, Integer.parseInt(enhetId));

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
                        Filter.TABLE_NAME, Filter.FILTER_ID);
                int rowsUpdated = db.update(sql, filterId);

                mineLagredeFilterRepository.deactivateMineFilterWithDeletedVeilederGroup(filterModelOptional.get().getFilterNavn(), filterModelOptional.get().getFilterValg().getVeiledere());
                return rowsUpdated;
            }
        }

        return 0;
    }

    public List<String> hentAlleEnheter() {
        String sql = String.format("SELECT DISTINCT(%s) as enhetId  FROM %s", VeilederGrupperFilter.ENHET_ID, VeilederGrupperFilter.TABLE_NAME);
        return db.query(sql, (rs, rowNum) -> rs.getString("enhetId"));
    }

    @Override
    public List<FilterModel> lagreSortering(String enhetId, List<SortOrder> sortOrder) {
        //TODO("Not yet implemented");
        return Collections.emptyList();
    }
}

