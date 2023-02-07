package no.nav.pto.veilarbfilter.auth;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class AuthUtils {
    public static VeilederId getInnloggetVeilederIdent() {
        return AuthContextHolderThreadLocal
                .instance().getNavIdent()
                .map(id -> VeilederId.of(id.get()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is missing from subject"));
    }

    public static UUID getInnloggetVeilederUUID(AuthContextHolder authContextHolder) {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "oid"))
                .map(UUID::fromString)
                .orElse(null);
    }

    public static Optional<String> getStringClaimOrEmpty(JWTClaimsSet claims, String claimName) {
        try {
            return ofNullable(claims.getStringClaim(claimName));
        } catch (Exception e) {
            return empty();
        }
    }
}
