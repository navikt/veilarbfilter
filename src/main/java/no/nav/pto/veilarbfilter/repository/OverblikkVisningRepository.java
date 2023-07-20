package no.nav.pto.veilarbfilter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.pto.veilarbfilter.database.Table.OverblikkVisning;
import static no.nav.pto.veilarbfilter.util.DateUtils.fromLocalDateTimeToTimestamp;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverblikkVisningRepository {
    private final JdbcTemplate db;
    private final ObjectMapper objectMapper;

    public Optional<OverblikkVisningModel> hentVisning(String veilederId) {
        try {
            String selectSql = String.format("SELECT * FROM %s where %s = ?", OverblikkVisning.TABLE_NAME, OverblikkVisning.VEILEDER_ID);
            OverblikkVisningModel minLagretDetaljerVisning = db.queryForObject(selectSql, (rs, rowNum) -> {
                        try {
                            return new OverblikkVisningModel(rs.getString(OverblikkVisning.VEILEDER_ID), rs.getString(OverblikkVisning.OVERBLIKK_VISNING), null);
                        } catch (Exception e) {
                            log.error("Error ved å hente visningen " + e, e);
                            throw new RuntimeException(e);
                        }
                    }, veilederId);

            return Optional.of(minLagretDetaljerVisning);
        } catch (Exception e) {
            log.warn("Finner ikke visningen " + e, e);
            return Optional.empty();
        }
    }

    public void lagreVisning(String veilederId, List<String> detaljerVisning) throws IllegalArgumentException {
        try {
            String insertSql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?,  to_json(?::JSON), ?)",
                    OverblikkVisning.TABLE_NAME, OverblikkVisning.VEILEDER_ID, OverblikkVisning.OVERBLIKK_VISNING, OverblikkVisning.OPPRETTET);
            db.update(insertSql, veilederId, objectMapper.writeValueAsString(detaljerVisning), fromLocalDateTimeToTimestamp(LocalDateTime.now()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kan ikke lagre visningen " + e, e);
        }
    }

    public void oppdaterVisning(String veilederId, List<String> detaljerVisning) throws IllegalArgumentException {
        try {

            String updateSql = String.format("UPDATE %s, %s = to_json(?::JSON) WHERE %s = ?", OverblikkVisning.TABLE_NAME, OverblikkVisning.OVERBLIKK_VISNING, OverblikkVisning.VEILEDER_ID);
            db.update(updateSql, objectMapper.writeValueAsString(detaljerVisning), veilederId);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kan ikke oppdatere visning " + e, e);
        }
    }

    public void slettVisning(String veilederId) throws IllegalArgumentException {
        try {
        String deleteSql = String.format("DELETE FROM %s WHERE %s = ?",
                OverblikkVisning.TABLE_NAME, OverblikkVisning.VEILEDER_ID);

       db.update(deleteSql, veilederId);

       } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kan ikke slette visning " + e, e);
        }
    }
}
