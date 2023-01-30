package no.nav.pto.veilarbfilter.auth;

import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class AuthUtils {
    public static VeilederId getInnloggetVeilederIdent() {
        return AuthContextHolderThreadLocal
                .instance().getNavIdent()
                .map(id -> VeilederId.of(id.get()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is missing from subject"));
    }

    public static UUID getInnloggetVeilederUUID() {
        return AuthContextHolderThreadLocal
                .instance().getUid()
                .map(UUID::fromString)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is missing from subject"));
    }
}
