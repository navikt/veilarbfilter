package no.nav.pto.veilarbfilter.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.database.Table;
import no.nav.pto.veilarbfilter.domene.OverblikkVisning;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static no.nav.pto.veilarbfilter.util.DateUtils.fromLocalDateTimeToTimestamp;
import static no.nav.pto.veilarbfilter.util.PostgresqlUtils.mapTilPostgresqlArray;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverblikkVisningRepository {
    private final NamedParameterJdbcTemplate db;

    public Optional<OverblikkVisning> hent(VeilederId veilederId) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource().addValue("veilederid", veilederId.getValue());
        String sql = String.format("SELECT * FROM %s where %s = :veilederid", Table.OverblikkVisning.TABLE_NAME, Table.OverblikkVisning.VEILEDER_ID);

        try {
            OverblikkVisning overblikkVisning = db.queryForObject(
                    sql,
                    paramSource,
                    (rs, rowNum) -> new OverblikkVisning(
                            (UUID) rs.getObject(Table.OverblikkVisning.OVERBLIKK_VISNING_ID),
                            Arrays.asList((String[]) rs.getArray(Table.OverblikkVisning.VISNING).getArray())
                    )
            );
            return Optional.ofNullable(overblikkVisning);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void lagre(VeilederId veilederId, List<String> overblikkVisning) {
        Optional<OverblikkVisning> maybeOverblikkvisning = hent(veilederId);
        MapSqlParameterSource paramSource = new MapSqlParameterSource().addValues(Map.ofEntries(
                Map.entry("overblikkvisningid", maybeOverblikkvisning.isPresent() ? maybeOverblikkvisning.get().overblikkVisningId() : UUID.randomUUID()),
                Map.entry("veilederid", veilederId.getValue()),
                Map.entry("visning", mapTilPostgresqlArray(overblikkVisning)),
                Map.entry("sistendret", fromLocalDateTimeToTimestamp(LocalDateTime.now()))
        ));
        String query = String.format("""
                        INSERT INTO %s (%s, %s, %s, %s) VALUES (:overblikkvisningid, :veilederid, :visning::varchar[], :sistendret)
                        ON CONFLICT (%s)
                        DO UPDATE SET(%s, %s) = (:visning::varchar[], :sistendret)
                        """,
                Table.OverblikkVisning.TABLE_NAME,
                Table.OverblikkVisning.OVERBLIKK_VISNING_ID,
                Table.OverblikkVisning.VEILEDER_ID,
                Table.OverblikkVisning.VISNING,
                Table.OverblikkVisning.SIST_ENDRET,
                Table.OverblikkVisning.OVERBLIKK_VISNING_ID,
                Table.OverblikkVisning.VISNING,
                Table.OverblikkVisning.SIST_ENDRET
        );

        db.update(query, paramSource);
    }

    public void slett(VeilederId veilederId) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource().addValue("veilederid", veilederId.getValue());
        String query = String.format("DELETE FROM %s WHERE %s = :veilederid", Table.OverblikkVisning.TABLE_NAME, Table.OverblikkVisning.VEILEDER_ID);

        db.update(query, paramSource);
    }
}
