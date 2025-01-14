package no.nav.pto.veilarbfilter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.database.Table;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.util.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilterRepository {
    private final JdbcTemplate db;
    private final ObjectMapper objectMapper;

    public Integer tellMineFilterSomInneholderEnBestemtFiltertype(String filtervalg) {
        // TODO feilhåndtering ved ugyldig filtervalg i input, evt betre typesikring

        String sql = String.format("SELECT count(filter_som_skal_telles) " +
                "FROM (" +
                "SELECT %s ->> ? AS liste_for_filtertype FROM %s" +
                ") AS filter_som_skal_telles " +
                "WHERE liste_for_filtertype != '[]';", Table.Filter.VALGTE_FILTER, Table.Filter.TABLE_NAME);
        return db.queryForObject(sql, Integer.class, filtervalg);
    }

    public List<FilterModel> hentMineFilterSomInneholderEnBestemtFiltertype(String filtervalg) {
        // TODO feilhåndtering ved ugyldig filtervalg i input, evt betre typesikring

        String sql = String.format("SELECT %s, %s, %s, %s, %s\n" +
                        "FROM (SELECT *, %s ->> ? AS liste_for_filtertype\n" +
                        "      FROM %s) AS filter_som_skal_telles\n" +
                        "WHERE liste_for_filtertype != '[]';",
                Table.Filter.FILTER_ID, Table.Filter.FILTER_NAVN, Table.Filter.VALGTE_FILTER, Table.Filter.OPPRETTET, Table.Filter.FILTER_CLEANUP,
                Table.Filter.VALGTE_FILTER, Table.Filter.TABLE_NAME);

        // TODO vurder å skrive denne litt ryddigare, for eksempel med å lage ein mapFromResultSet-funksjon.
        return db.query(sql, (rs, rowNum) -> {
                    try {
                        PortefoljeFilter portefoljeFilter = objectMapper.readValue(rs.getString(Table.Filter.VALGTE_FILTER), PortefoljeFilter.class);
                        // Treng vi registreringstyper? Koden her er kopiert frå finnFilterForFilterBruker.
                        List<String> registreringstyper = portefoljeFilter.getRegistreringstype().stream().map(this::mapSituasjonTilBeskrivelse).toList();
                        portefoljeFilter.setRegistreringstype(registreringstyper);

                        return new FilterModel(
                                rs.getInt(Table.Filter.FILTER_ID),
                                rs.getString(Table.Filter.FILTER_NAVN),
                                portefoljeFilter,
                                DateUtils.fromTimestampToLocalDateTime(rs.getTimestamp(Table.Filter.OPPRETTET)),
                                rs.getInt(Table.Filter.FILTER_CLEANUP));
                    } catch (Exception e) {
                        log.error("Can't load filter " + e, e);
                        throw new RuntimeException(e);
                    }
                }, filtervalg
        );
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
}
