package no.nav.pto.veilarbfilter

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.ApplicationCall
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.auth.HttpAuthHeader
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("veilarbfilter.JwtConfig")

class JwtUtil {
    companion object {
        fun useJwtFromCookie(call: ApplicationCall): HttpAuthHeader? {
            return try {
                val token = call.request.cookies["ID_token"]
                io.ktor.http.auth.parseAuthorizationHeader("Bearer $token")
            } catch (ex: Throwable) {
                log.error("Illegal HTTP auth header", ex)
                null
            }
        }

        fun getSubject(call: ApplicationCall): String {
            return try {
                useJwtFromCookie(call)
                    ?.getBlob()
                    ?.let { blob -> JWT.decode(blob).parsePayload().subject }
                    ?: "Unauthenticated"
            } catch (e: Throwable) {
                "JWT not found"
            }
        }

        fun makeJwkProvider(jwksUrl: String): JwkProvider =
            JwkProviderBuilder(URL(jwksUrl))
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        fun validateJWT(credentials: JWTCredential): Principal? {
            return try {
                requireNotNull(credentials.payload.audience) { "Audience not present" }
                JWTPrincipal(credentials.payload)
            } catch (e: Exception) {
                log.error("Failed to validateJWT token", e)
                null
            }
        }

        private fun HttpAuthHeader.getBlob() = when {
            this is HttpAuthHeader.Single -> blob
            else -> null
        }

        private fun DecodedJWT.parsePayload(): Payload {
            val payloadString = String(Base64.getUrlDecoder().decode(payload))
            return JWTParser().parsePayload(payloadString)
        }
    }
}


class JwtUtilAzure {
    companion object {
        fun useJwtFromCookie(call: ApplicationCall): HttpAuthHeader? {
            return try {
                val azureValidation = call.request.cookies["isso-idtoken"]
                val header = io.ktor.http.auth.parseAuthorizationHeader("Bearer $azureValidation")
                return header
            } catch (ex: Throwable) {
                null
            }
        }

        fun getSubject(call: ApplicationCall): String {
            return try {
                useJwtFromCookie(call)
                    ?.getBlob()
                    ?.let { blob -> JWT.decode(blob).parsePayload().subject }
                    ?: "Unauthenticated"
            } catch (e: Throwable) {
                "JWT not found"
            }
        }

        fun makeJwkProvider(jwksUrl: String): JwkProvider =
            JwkProviderBuilder(URL(jwksUrl))
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        fun validateJWT(credentials: JWTCredential): Principal? {
            return try {
                requireNotNull(credentials.payload.audience) { "Audience not present" }
                JWTPrincipal(credentials.payload)
            } catch (e: Exception) {
                log.error("Failed to validateJWT token", e)
                null
            }
        }

        private fun HttpAuthHeader.getBlob() = when {
            this is HttpAuthHeader.Single -> blob
            else -> null
        }

        private fun DecodedJWT.parsePayload(): Payload {
            val payloadString = String(Base64.getUrlDecoder().decode(payload))
            return JWTParser().parsePayload(payloadString)
        }
    }
}

class MockPayload(val staticSubject: String) : Payload {
    override fun getSubject(): String {
        return staticSubject
    }

    override fun getExpiresAt(): Date {
        TODO("not implemented")
    }

    override fun getIssuer(): String {
        TODO("not implemented")
    }

    override fun getAudience(): MutableList<String> {
        TODO("not implemented")
    }

    override fun getId(): String {
        TODO("not implemented")
    }

    override fun getClaims(): MutableMap<String, Claim> {
        TODO("not implemented")
    }

    override fun getIssuedAt(): Date {
        TODO("not implemented")
    }

    override fun getClaim(name: String?): Claim {
        TODO("not implemented")
    }

    override fun getNotBefore(): Date {
        TODO("not implemented")
    }
}
