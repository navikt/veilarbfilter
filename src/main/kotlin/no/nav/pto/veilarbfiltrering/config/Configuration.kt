package no.nav.pto.veilarbfiltrering.config

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.*
import no.nav.pto.veilarbfiltrering.JwtUtil

private const val secret = ""
private const val notUsedLocally = ""
private val defaultProperties = ConfigurationMap(
        mapOf(
                "NAIS_NAMESPACE" to notUsedLocally,
                "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
                "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
                "VEILARBFILTRERING_DB_URL" to "jdbc:postgresql://localhost:54321/veilarbfiltering",
                "VEILARBFILTRERING_DB_NAME" to "veilarbfiltering",
                "VEILARBFILTRERING_DB_USERNAME" to "user",
                "VEILARBFILTRERING_DB_PASSWORD" to "password",
                "VAULT_MOUNT_PATH" to notUsedLocally
        )
)

data class Configuration (
        val namespace: String = config()[Key("NAIS_NAMESPACE", stringType)],
        val database: DB = DB(),
        val jwt: Jwt = Jwt()
) {

        data class Jwt (
                val jwksUrl: JwkProvider = JwtUtil.makeJwkProvider(config()[Key("ISSO_JWKS_URL", stringType)]),
                val jwtIssuer: String = config()[Key("ISSO_ISSUER", stringType)]
        )

        data class DB (
                val url: String = config()[Key("VEILARBFILTRERING_DB_URL", stringType)],
                val name: String = config()[Key("VEILARBFILTRERING_DB_NAME", stringType)],
                val username: String = config()[Key("VEILARBFILTRERING_DB_USERNAME", stringType)],
                val password: String = config()[Key("VEILARBFILTRERING_DB_PASSWORD", stringType)],
                val vaultMountPath: String = config()[Key("VAULT_MOUNT_PATH", stringType)]
        )

}

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties