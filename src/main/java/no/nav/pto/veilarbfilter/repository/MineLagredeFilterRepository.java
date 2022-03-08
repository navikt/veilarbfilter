package no.nav.pto.veilarbfilter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.service.LagredeFilterFeilmeldinger;
import no.nav.pto.veilarbfilter.util.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.pto.veilarbfilter.database.Table.Filter;
import static no.nav.pto.veilarbfilter.database.Table.MineLagredeFilter;
import static no.nav.pto.veilarbfilter.util.DateUtils.fromLocalDateTimeToTimestamp;

@Service
@Slf4j
@RequiredArgsConstructor
public class MineLagredeFilterRepository implements FilterService {
    private final JdbcTemplate db;
    private final ObjectMapper objectMapper;


    public Optional<FilterModel> lagreFilter(String veilederId, NyttFilterModel nyttFilterModel) throws IllegalArgumentException {
        try {
            var key = 0;

            validerFilterNavn(nyttFilterModel.getFilterNavn());
            validerFilterValg(nyttFilterModel.getFilterValg());
            validerUnikhet(erUgyldigNavn(veilederId, nyttFilterModel.getFilterNavn(), Optional.empty()),
                    erUgyldigFiltervalg(veilederId, nyttFilterModel.getFilterValg(), Optional.empty()));

            String insertSql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, to_json(?::JSON), ?)",
                    Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET);
            int affectedRows = db.update(insertSql, nyttFilterModel.getFilterNavn(), objectMapper.writeValueAsString(nyttFilterModel.getFilterValg()), fromLocalDateTimeToTimestamp(LocalDateTime.now()));

            if (affectedRows > 0) {
                String lastId = String.format("SELECT MAX(%s) FROM %s",
                        Filter.FILTER_ID, Filter.TABLE_NAME);
                key = db.queryForObject(lastId, Integer.class);

                insertSql = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                        MineLagredeFilter.TABLE_NAME, MineLagredeFilter.FILTER_ID, MineLagredeFilter.VEILEDER_ID);

                db.update(insertSql, key, veilederId);
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
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) throws IllegalArgumentException {
        try {
            validerFilterNavn(filter.getFilterNavn());
            validerFilterValg(filter.getFilterValg());

            validerUnikhet(erUgyldigNavn(veilederId, filter.getFilterNavn(), Optional.of(filter.getFilterId())),
                    erUgyldigFiltervalg(veilederId, filter.getFilterValg(), Optional.of(filter.getFilterId())));


            String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, MineLagredeFilter.FILTER_ID);
            Integer numOfRows = db.queryForObject(sql, Integer.class, veilederId, filter.getFilterId());

            if (numOfRows > 0) {
                sql = String.format("UPDATE %s SET %s = ?, %s = to_json(?::JSON) WHERE %s = ?", Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.FILTER_ID);
                db.update(sql, filter.getFilterNavn(), objectMapper.writeValueAsString(filter.getFilterValg()), filter.getFilterId());
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
            String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.%s = f.%s AND f.%s = ?",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID, Filter.FILTER_ID);
            FilterModel mineLagredeFilterModel = db.queryForObject(sql, (rs, rowNum) -> {
                        try {
                            PortefoljeFilter portefoljeFilter = objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class);
                            return new MineLagredeFilterModel(rs.getInt(MineLagredeFilter.FILTER_ID),
                                    rs.getString(Filter.FILTER_NAVN),
                                    portefoljeFilter,
                                    DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                                    rs.getInt(Filter.FILTER_CLEANUP),
                                    rs.getString(MineLagredeFilter.VEILEDER_ID),
                                    rs.getInt(MineLagredeFilter.SORT_ORDER),
                                    rs.getBoolean(MineLagredeFilter.AKTIV),
                                    rs.getString(MineLagredeFilter.NOTE));
                        } catch (Exception e) {
                            log.error("Error while reading filter " + e, e);
                            throw new RuntimeException(e);
                        }
                    }
                    , filterId);
            return Optional.of(mineLagredeFilterModel);
        } catch (Exception e) {
            log.warn("Can't find filter " + e, e);
            return Optional.empty();
        }
    }

    public List<MineLagredeFilterModel> hentAllLagredeFilter() {
        String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.%s = f.%s",
                MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID);

        return db.query(sql, (rs, rowNum) -> {
            try {
                return new MineLagredeFilterModel(rs.getInt(MineLagredeFilter.FILTER_ID),
                        rs.getString(Filter.FILTER_NAVN),
                        objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class),
                        DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                        rs.getInt(Filter.FILTER_CLEANUP),
                        rs.getString(MineLagredeFilter.VEILEDER_ID),
                        rs.getInt(MineLagredeFilter.SORT_ORDER),
                        rs.getBoolean(MineLagredeFilter.AKTIV),
                        rs.getString(MineLagredeFilter.NOTE));
            } catch (Exception e) {
                log.error("Can't load filters " + e, e);
                throw new RuntimeException(e);
            }
        });
    }

    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {
        try {
            String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.%s = f.%s AND ml.%s = \'%s\'",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID, MineLagredeFilter.VEILEDER_ID, veilederId);
            log.info(sql);

            return db.query(sql, (rs, rowNum) -> {
                try {
                    return new MineLagredeFilterModel(rs.getInt(MineLagredeFilter.FILTER_ID),
                            rs.getString(Filter.FILTER_NAVN),
                            objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class),
                            DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                            rs.getInt(Filter.FILTER_CLEANUP),
                            rs.getString(MineLagredeFilter.VEILEDER_ID),
                            rs.getInt(MineLagredeFilter.SORT_ORDER),
                            rs.getBoolean(MineLagredeFilter.AKTIV),
                            rs.getString(MineLagredeFilter.NOTE));
                } catch (Exception e) {
                    log.error("Can't load filter " + e, e);
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error("Can't load filters " + e, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Integer slettFilter(Integer filterId, String veilederId) {
        String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
                MineLagredeFilter.TABLE_NAME, MineLagredeFilter.FILTER_ID, MineLagredeFilter.VEILEDER_ID);

        int numOfRowsUpdated = db.update(sql, filterId, veilederId);

        if (numOfRowsUpdated > 0) {
            sql = String.format("DELETE FROM %s WHERE %s = ?", Filter.TABLE_NAME, Filter.FILTER_ID);

            return db.update(sql, filterId);
        }
        return 0;
    }

    @Override
    public List<FilterModel> lagreSortering(String veilederId, List<SortOrder> sortOrder) {
        String filterIdsList = sortOrder.stream().map(x -> x.getFilterId() + "::int").collect(Collectors.joining(",", "(", ")"));

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s IN %s AND %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.FILTER_ID, filterIdsList, MineLagredeFilter.VEILEDER_ID);
        Integer numOfAllowedUpdates = db.queryForObject(sql, Integer.class, veilederId);

        if (numOfAllowedUpdates.equals(sortOrder.size())) {
            sortOrder.forEach(filter -> {
                String updateSql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.SORT_ORDER, MineLagredeFilter.FILTER_ID);
                db.update(updateSql, filter.getSortOrder(), filter.getFilterId());
            });
        }
        return finnFilterForFilterBruker(veilederId);
    }

    public void deactivateMineFilterWithDeletedVeilederGroup(String
                                                                     veilederGroupName, List<String> veiledereInDeletedGroup) {
        List<MineLagredeFilterModel> alleMineFilter = hentAllLagredeFilter();
        alleMineFilter.stream().forEach(mineFilter -> {
            if (!mineFilter.getFilterValg().getVeiledere().isEmpty() &&
                    erVeiledereListeErLik(mineFilter.getFilterValg().getVeiledere(), veiledereInDeletedGroup)) {
                deactiveMineFilter(mineFilter.getFilterId(), veilederGroupName);
            }
        });
    }

    private void deactiveMineFilter(Integer filterId, String note) {
        try {
            String sql = String.format("UPDATE %s SET %s = 0, %s = ? WHERE %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.AKTIV, MineLagredeFilter.NOTE, MineLagredeFilter.FILTER_ID);
            db.update(sql, note, filterId);
        } catch (Exception e) {
            log.error("Error while deactivating mine filter", e);
        }
    }

    private Boolean erVeiledereListeErLik(List<String> veiledereList1, List<String> veiledereList2) {
        if (veiledereList1.size() != veiledereList2.size()) return false;

        Collections.sort(veiledereList1);
        Collections.sort(veiledereList2);
        return veiledereList1.equals(veiledereList2);
    }


    private boolean erUgyldigNavn(String veilederId, String filterNavn, Optional<Integer> filterIdOptional) {
        String sql;
        Integer count;
        if (filterIdOptional.isPresent()) {
            sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                            "WHERE ml.%s = f.%s AND ml.%s = ? AND f.%s = ? AND %s =  1 AND f.filter_id != ?",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID, MineLagredeFilter.VEILEDER_ID, Filter.FILTER_NAVN, MineLagredeFilter.AKTIV);
            count = db.queryForObject(sql, Integer.class, veilederId, filterNavn, filterIdOptional.get());
        } else {
            sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                            "WHERE ml.%s = f.%s AND ml.%s = ? AND f.%s = ? AND %s =  1",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID, MineLagredeFilter.VEILEDER_ID, Filter.FILTER_NAVN, MineLagredeFilter.AKTIV);
            count = db.queryForObject(sql, Integer.class, veilederId, filterNavn);
        }

        return count > 0;
    }

    private boolean erUgyldigFiltervalg(String veilederId, PortefoljeFilter
            filterValg, Optional<Integer> filterIdOptional) {
        try {
            Integer count;
            String sql;
            ObjectMapper objectMapper = new ObjectMapper();
            if (filterIdOptional.isPresent()) {
                sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                                "WHERE ml.%s = f.%s AND ml.%s = ? AND f.%s::jsonb = to_json(?::JSON)::jsonb AND %s =  1 AND f.filter_id != ?",
                        MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID, MineLagredeFilter.VEILEDER_ID, Filter.VALGTE_FILTER, MineLagredeFilter.AKTIV);
                count = db.queryForObject(sql, Integer.class, veilederId, objectMapper.writeValueAsString(filterValg), filterIdOptional.get());
            } else {
                sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                                "WHERE ml.%s = f.%s AND ml.%s = ? AND f.%s::jsonb = to_json(?::JSON)::jsonb AND %s =  1",
                        MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.FILTER_ID, Filter.FILTER_ID, MineLagredeFilter.VEILEDER_ID, Filter.VALGTE_FILTER, MineLagredeFilter.AKTIV);
                count = db.queryForObject(sql, Integer.class, veilederId, objectMapper.writeValueAsString(filterValg));
            }

            return count > 0;
        } catch (Exception e) {
            log.warn("Error while checking if filter is valid " + e, e);
            return false;
        }
    }

    private void validerFilterNavn(String navn) {
        Assert.hasLength(navn, LagredeFilterFeilmeldinger.NAVN_TOMT.message);
        Assert.isTrue(navn.length() < 255, LagredeFilterFeilmeldinger.NAVN_FOR_LANGT.message);
    }

    private void validerFilterValg(PortefoljeFilter valg) {
        Assert.isTrue(valg.isNotEmpty(), LagredeFilterFeilmeldinger.FILTERVALG_TOMT.message);
    }

    private void validerUnikhet(Boolean navn, Boolean valg) {
        Assert.isTrue(!navn, LagredeFilterFeilmeldinger.NAVN_EKSISTERER.message);
        Assert.isTrue(!valg, LagredeFilterFeilmeldinger.FILTERVALG_EKSISTERER.message);
    }
}
