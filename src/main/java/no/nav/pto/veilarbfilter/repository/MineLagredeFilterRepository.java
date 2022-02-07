package no.nav.pto.veilarbfilter.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.service.LagredeFilterFeilmeldinger;
import no.nav.pto.veilarbfilter.util.DateUtils;
import no.nav.pto.veilarbfilter.util.JsonUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.pto.veilarbfilter.database.Table.Filter;
import static no.nav.pto.veilarbfilter.database.Table.MineLagredeFilter;

@Service
@Slf4j
@RequiredArgsConstructor
public class MineLagredeFilterRepository implements FilterService {
    private final JdbcTemplate db;


    public Optional<FilterModel> lagreFilter(String veilederId, NyttFilterModel nyttFilterModel) {
        var key = 0;

        validerFilterNavn(nyttFilterModel.getFilterNavn());
        validerFilterValg(nyttFilterModel.getFilterValg());
        validerUnikhet(erUgyldigNavn(veilederId, nyttFilterModel.getFilterNavn(), Optional.empty()),
                erUgyldigFiltervalg(veilederId, nyttFilterModel.getFilterValg(), Optional.empty()));

        KeyHolder keyHolder = new GeneratedKeyHolder();

        String insertSql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                Filter.TABLE_NAME, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET);
        int affectedRows = db.update(insertSql, nyttFilterModel.getFilterNavn(), nyttFilterModel.getFilterValg(), LocalDateTime.now(), keyHolder, new String[]{"FILTER_ID"});

        if (affectedRows > 0) {
            key = keyHolder.getKey().intValue();

            insertSql = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                    MineLagredeFilter.TABLE_NAME, Filter.FILTER_ID, MineLagredeFilter.VEILEDER_ID);

            db.update(insertSql, key, veilederId);
        }

        return hentFilter(key);
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) {
        validerFilterNavn(filter.getFilterNavn());
        validerFilterValg(filter.getFilterValg());

        validerUnikhet(erUgyldigNavn(veilederId, filter.getFilterNavn(), Optional.of(filter.getFilterId())),
                erUgyldigFiltervalg(veilederId, filter.getFilterValg(), Optional.of(filter.getFilterId())));


        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, MineLagredeFilter.FILTER_ID);
        Integer numOfRows = db.queryForObject(sql, Integer.class, veilederId, filter.getFilterId());

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
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME);
            FilterModel mineLagredeFilterModel = db.queryForObject(sql, (rs, rowNum) -> {
                        PortefoljeFilter portefoljeFilter = JsonUtils.deserializeFilterValg(rs.getString(Filter.VALGTE_FILTER));
                        return new MineLagredeFilterModel(rs.getInt(MineLagredeFilter.FILTER_ID),
                                rs.getString(Filter.FILTER_NAVN),
                                portefoljeFilter,
                                DateUtils.toLocalDateTimeOrNull(rs.getString(Filter.OPPRETTET)),
                                rs.getInt(Filter.FILTER_CLEANUP),
                                rs.getString(MineLagredeFilter.VEILEDER_ID),
                                rs.getInt(MineLagredeFilter.SORT_ORDER),
                                rs.getBoolean(MineLagredeFilter.AKTIV),
                                rs.getString(MineLagredeFilter.NOTE));
                    }
                    , filterId);
            return Optional.of(mineLagredeFilterModel);
        } catch (Exception e) {
            log.warn("Can't find filter " + e, e);
            return Optional.empty();
        }
    }

    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {

        String sql = String.format("SELECT * FROM %s as ml, %s as f WHERE ml.filter_id = f.filter_id AND and ml.%s = %s",
                MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, veilederId);

        return db.query(sql, (rs, rowNum) ->
                new MineLagredeFilterModel(rs.getInt(MineLagredeFilter.FILTER_ID),
                        rs.getString(Filter.FILTER_NAVN),
                        JsonUtils.deserializeFilterValg(rs.getString(Filter.VALGTE_FILTER)),
                        DateUtils.toLocalDateTimeOrNull(rs.getString(Filter.OPPRETTET)),
                        rs.getInt(Filter.FILTER_CLEANUP),
                        rs.getString(MineLagredeFilter.VEILEDER_ID),
                        rs.getInt(MineLagredeFilter.SORT_ORDER),
                        rs.getBoolean(MineLagredeFilter.AKTIV),
                        rs.getString(MineLagredeFilter.NOTE)));
    }

    @Override
    public Integer slettFilter(Integer filterId, String veilederId) {
        String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
                MineLagredeFilter.TABLE_NAME, MineLagredeFilter.FILTER_ID, MineLagredeFilter.VEILEDER_ID);

        int numOfRowsUpdated = db.update(sql, filterId, veilederId);

        if (numOfRowsUpdated > 0) {
            sql = String.format("DELETE FROM %s WHERE %s = ?",
                    Filter.FILTER_ID);

            return db.update(sql, filterId);
        }
        return 0;
    }

    @Override
    public List<FilterModel> lagreSortering(String veilederId, List<SortOrder> sortOrder) {
        String filterIdsList = sortOrder.stream().map(x -> String.valueOf(x.getFilterId())).collect(Collectors.joining(",", "(", ")"));

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s IN ? AND %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.FILTER_ID, MineLagredeFilter.VEILEDER_ID);
        Integer numOfAllowedUpdates = db.queryForObject(sql, Integer.class, filterIdsList, veilederId);

        if (numOfAllowedUpdates.equals(sortOrder.size())) {
            sortOrder.forEach(filter -> {
                String updateSql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", MineLagredeFilter.TABLE_NAME, MineLagredeFilter.SORT_ORDER, MineLagredeFilter.FILTER_ID);
                db.update(updateSql, filter.getSortOrder(), filter.getFilterId());
            });
        }
        return finnFilterForFilterBruker(veilederId);
    }


    private boolean erUgyldigNavn(String veilederId, String filterNavn, Optional<Integer> filterIdOptional) {
        String sql;
        Integer count;
        if (filterIdOptional.isPresent()) {
            sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                            "WHERE ml.filter_id = f.filter_id AND f.%s = ? AND f.%s = ? AND %s =  1 AND f.filter_id != ?",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, Filter.FILTER_NAVN, MineLagredeFilter.AKTIV);
            count = db.queryForObject(sql, Integer.class, veilederId, filterNavn, filterIdOptional.get());
        } else {
            sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                            "WHERE ml.filter_id = f.filter_id AND f.%s = ? AND f.%s = ? AND %s =  1",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, Filter.FILTER_NAVN, MineLagredeFilter.AKTIV);
            count = db.queryForObject(sql, Integer.class, veilederId, filterNavn);
        }

        return count > 0;
    }

    private boolean erUgyldigFiltervalg(String veilederId, PortefoljeFilter filterValg, Optional<Integer> filterIdOptional) {
        Integer count;
        String sql;
        if (filterIdOptional.isPresent()) {
            sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                            "WHERE ml.filter_id = f.filter_id AND f.%s = ? AND f.%s = ? AND %s =  1 AND f.filter_id != ?",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, Filter.VALGTE_FILTER, MineLagredeFilter.AKTIV);
            count = db.queryForObject(sql, Integer.class, veilederId, filterValg, filterIdOptional.get());
        } else {
            sql = String.format("SELECT COUNT(*) FROM %s ml, %s f " +
                            "WHERE ml.filter_id = f.filter_id AND f.%s = ? AND f.%s = ? AND %s =  1",
                    MineLagredeFilter.TABLE_NAME, Filter.TABLE_NAME, MineLagredeFilter.VEILEDER_ID, Filter.VALGTE_FILTER, MineLagredeFilter.AKTIV);
            count = db.queryForObject(sql, Integer.class, veilederId, filterValg);
        }

        return count > 0;
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
