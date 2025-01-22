package no.nav.pto.veilarbfilter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.database.Table.Filter;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.util.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilterRepository {
    public static final String ARENA_HOVEDMAL_FILTERVALG_JSON_KEY = "hovedmal";
    public static final String ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY = "innsatsgruppe";
    public static final String GJELDENDE_VEDTAK_HOVEDMAL_FILTERVALG_JSON_KEY = "hovedmalGjeldendeVedtak14a";
    public static final String GJELDENDE_VEDTAK_INNSATSGRUPPE_FILTERVALG_JSON_KEY = "innsatsgruppeGjeldendeVedtak14a";
    public static final String REGISTRERINGSTYPE_FILTERVALG_JSON_KEY = "registreringstype";

    private final JdbcTemplate db;
    private final ObjectMapper objectMapper;

    private final int HENT_ALLE = -1;

    public void oppdaterFilterValg(Integer filterId, PortefoljeFilter filterValg) throws JsonProcessingException {
        //language=postgresql
        String sql = String.format("""
                UPDATE %s SET %s = to_json(?::JSON)
                WHERE %s = ?
                """, Filter.TABLE_NAME, Filter.VALGTE_FILTER, Filter.FILTER_ID);

        db.update(sql, objectMapper.writeValueAsString(filterValg), filterId);
    }

    public int oppdaterFilterValgBatch(List<FilterIdOgFilterValgPar> filterBatch) {
        //language=postgresql
        String sql = String.format("""
                UPDATE %s SET %s = to_json(?::JSON)
                WHERE %s = ?
                """, Filter.TABLE_NAME, Filter.VALGTE_FILTER, Filter.FILTER_ID);

        int[] affectedRows =  db.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
                Integer filterID = filterBatch.get(i).filterId;
                String filterValg = filterBatch.get(i).filterValg;

                ps.setString(1, filterValg);
                ps.setInt(2, filterID);
            }

            @Override
            public int getBatchSize() {
                return filterBatch.size();
            }
        });

        return Arrays.stream(affectedRows).sum();
    }

    public Integer tellMineFilterSomInneholderEnBestemtFiltertype(String filtervalg) {
        // TODO feilhåndtering ved ugyldig filtervalg i input, evt betre typesikring

        String sql = String.format("SELECT count(filter_som_skal_telles) " +
                "FROM (" +
                "SELECT %s ->> ? AS liste_for_filtertype FROM %s" +
                ") AS filter_som_skal_telles " +
                "WHERE liste_for_filtertype != '[]';", Filter.VALGTE_FILTER, Filter.TABLE_NAME);
        return db.queryForObject(sql, Integer.class, filtervalg);
    }

    public List<FilterModel> hentMineFilterSomInneholderEnBestemtFiltertype(String filtervalg) {
        return hentMineFilterSomInneholderEnBestemtFiltertype(filtervalg, HENT_ALLE);
    }

    public List<FilterModel> hentMineFilterSomInneholderEnBestemtFiltertype(String filtervalg, int antallSomSkalHentes) {
        if (antallSomSkalHentes != HENT_ALLE && antallSomSkalHentes <= 0) throw new IllegalArgumentException("Antall som skal hentes må enten vere et positivt heltall, eller " + HENT_ALLE ) ;

        // TODO feilhåndtering ved ugyldig filtervalg i input, evt betre typesikring

        // language=postgresql
        String sqlHentAntall = String.format("""
                        SELECT %s, %s, %s, %s, %s
                        FROM (SELECT *, %s ->> ? AS liste_for_filtertype
                            FROM %s) AS filter_som_skal_telles
                        WHERE liste_for_filtertype != '[]'
                        LIMIT ?;""",
                Filter.FILTER_ID, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET, Filter.FILTER_CLEANUP,
                Filter.VALGTE_FILTER, Filter.TABLE_NAME);

        // language=postgresql
        String sqlHentAlle = String.format("""
                        SELECT %s, %s, %s, %s, %s
                        FROM (SELECT *, %s ->> ? AS liste_for_filtertype
                            FROM %s) AS filter_som_skal_telles
                        WHERE liste_for_filtertype != '[]';""",
                Filter.FILTER_ID, Filter.FILTER_NAVN, Filter.VALGTE_FILTER, Filter.OPPRETTET, Filter.FILTER_CLEANUP,
                Filter.VALGTE_FILTER, Filter.TABLE_NAME);

        if (antallSomSkalHentes == HENT_ALLE) {
            return db.query(sqlHentAlle, this::mapTilFilterModel, filtervalg);
        }
        return db.query(sqlHentAntall, this::mapTilFilterModel, filtervalg, antallSomSkalHentes);
    }

    // Denne funksjonen bør kun brukast ifm. migrering av filter
    private FilterModel mapTilFilterModel(ResultSet rs, int rowNum) {
        try {
            PortefoljeFilter portefoljeFilter = objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class);

            return new FilterModel(
                    rs.getInt(Filter.FILTER_ID),
                    rs.getString(Filter.FILTER_NAVN),
                    portefoljeFilter,
                    DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Filter.OPPRETTET)),
                    rs.getInt(Filter.FILTER_CLEANUP));
        } catch (Exception e) {
            log.error("Can't load filter " + e, e);
            throw new RuntimeException(e);
        }
    }

    public record FilterIdOgFilterValgPar(Integer filterId, String filterValg) {}
}
