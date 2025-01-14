package no.nav.pto.veilarbfilter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.database.Table.Filter;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.util.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilterRepository {
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

    private String mapSituasjonTilBeskrivelse(String situasjon) {
        return switch (situasjon) {
            case "MISTET_JOBBEN", "OPPSIGELSE" -> "HAR_BLITT_SAGT_OPP";
            case "JOBB_OVER_2_AAR" -> "IKKE_VAERT_I_JOBB_SISTE_2_AAR";
            case "VIL_FORTSETTE_I_JOBB" -> "ANNET";
            case "INGEN_SVAR", "INGEN_VERDI" -> "UDEFINERT";
            case "ENDRET_PERMITTERINGSPROSENT", "TILBAKE_TIL_JOBB" -> "ER_PERMITTERT";
            case "SAGT_OPP" -> "HAR_SAGT_OPP";
            default -> situasjon;
        };
    }

    private FilterModel mapTilFilterModel(ResultSet rs, int rowNum) {
        try {
            PortefoljeFilter portefoljeFilter = objectMapper.readValue(rs.getString(Filter.VALGTE_FILTER), PortefoljeFilter.class);
            // Treng vi registreringstyper? Koden her er kopiert frå finnFilterForFilterBruker.
            List<String> registreringstyper = portefoljeFilter.getRegistreringstype().stream().map(this::mapSituasjonTilBeskrivelse).toList();
            portefoljeFilter.setRegistreringstype(registreringstyper);

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
}
