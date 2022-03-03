package no.nav.pto.veilarbfilter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.database.Table.VeilederGrupperFilter;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.util.DateUtils;
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
    private final ObjectMapper objectMapper;


    public Optional<FilterModel> lagreFilter(String enhetId, NyttFilterModel nyttFilterModel) throws IllegalArgumentException {
        try {
            var key = 0;

            String insertSql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, to_json(?::JSON), ?)",
                    Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET);

            Integer updateCount = db.execute(insertSql, (PreparedStatementCallback<Integer>) ps -> {
                try {
                    ps.setString(1, nyttFilterModel.getFilterNavn());
                    ps.setString(2, objectMapper.writeValueAsString(nyttFilterModel.getFilterValg()));
                    ps.setTimestamp(3, fromLocalDateTimeToTimestamp(LocalDateTime.now()));
                    return ps.executeUpdate();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Can't save filter " + e, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String enhetId, FilterModel filter) throws IllegalArgumentException {
        try {
            String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", VeilederGrupperFilter.TABLE_NAME, VeilederGrupperFilter.ENHET_ID, VeilederGrupperFilter.FILTER_ID);
            Integer numOfRows = db.queryForObject(sql, Integer.class, enhetId, filter.getFilterId());

            if (numOfRows > 0) {
                sql = String.format("UPDATE %s SET %s = ?, %s = to_json(?::JSON), %s = ? WHERE %s = ?", Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.FILTER_CLEANUP, Filter.FILTER_ID);
                db.update(sql, filter.getFilterNavn(), objectMapper.writeValueAsString(filter.getFilterValg()), filter.getFilterCleanup(), filter.getFilterId());
            }

            return hentFilter(filter.getFilterId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Can't update filter " + e, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        try {
            String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.%s = f.%s AND f.filter_id = ?",
                    VeilederGrupperFilter.TABLE_NAME, Filter.TABLE_NAME, VeilederGrupperFilter.FILTER_ID, Filter.FILTER_ID);
            FilterModel veilederGruppeFilterModel = db.queryForObject(sql, (rs, rowNum) -> {
                        try {
                            PortefoljeFilter portefoljeFilter = objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class);
                            return new VeilederGruppeFilterModel(rs.getInt(VeilederGrupperFilter.FILTER_ID),
                                    rs.getString(Filter.FILTER_NAVN),
                                    portefoljeFilter,
                                    DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                                    rs.getInt(Filter.FILTER_CLEANUP),
                                    rs.getString(VeilederGrupperFilter.ENHET_ID));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    , filterId);
            return Optional.of(veilederGruppeFilterModel);
        } catch (Exception e) {
            log.warn("Can't find filter " + e);
            return Optional.empty();
        }
    }

    public List<FilterModel> finnFilterForFilterBruker(String enhetId) {

        String sql = String.format("SELECT * FROM %s AS ml, %s AS f WHERE ml.%s = f.%s AND ml.%s = \'%o\'",
                VeilederGrupperFilter.TABLE_NAME, Filter.TABLE_NAME, VeilederGrupperFilter.FILTER_ID, Filter.FILTER_ID, VeilederGrupperFilter.ENHET_ID, Integer.parseInt(enhetId));

        return db.query(sql, (rs, rowNum) -> {
            try {
                return new VeilederGruppeFilterModel(rs.getInt(VeilederGrupperFilter.FILTER_ID),
                        rs.getString(Filter.FILTER_NAVN),
                        objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class),
                        DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                        rs.getInt(Filter.FILTER_CLEANUP),
                        rs.getString(VeilederGrupperFilter.ENHET_ID));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

