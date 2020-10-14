package no.nav.pto.veilarbfilter.config

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.*
import no.nav.common.utils.Credentials
import no.nav.common.utils.EnvironmentUtils.requireNamespace
import no.nav.common.utils.NaisUtils.getCredentials
import no.nav.pto.veilarbfilter.JwtUtil

private const val notUsedLocally = ""
private val defaultProperties = ConfigurationMap(
    mapOf(
        "NAIS_CLUSTER_NAME" to notUsedLocally,
        "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
        "ISSO_ISSUER" to "https://isso-q.adeo.no:443/isso/oauth2",
        "AAD_DISCOVERY_URL" to "",
        "VEILARBLOGIN_AAD_CLIENT_ID" to "",
        "veilarbfilter_DB_URL" to "jdbc:postgresql://localhost:54321/veilarbfilter",
        "veilarbfilter_DB_NAME" to "veilarbfilter",
        "veilarbfilter_DB_USERNAME" to "user",
        "veilarbfilter_DB_PASSWORD" to "password",
        "VAULT_MOUNT_PATH" to notUsedLocally,
        "SECURITY_TOKEN_SERVICE_DISCOVERY_URL" to notUsedLocally
    )
)


data class Configuration(
    val clustername: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
    val stsDiscoveryUrl: String = config()[Key("SECURITY_TOKEN_SERVICE_DISCOVERY_URL", stringType)],
    val database: DB = DB(),
    val jwt: Jwt = Jwt(),
    val azureConfig: Azure = Azure(),
    val abac: Abac = Abac(),
    val veilarbveilederConfig: VeilarbveilederConfig = VeilarbveilederConfig(),
    val serviceUser: Credentials = getCredentials("service_user"),
    val httpServerWait: Boolean = true,
    val useAuthentication: Boolean = true
) {

    data class Jwt(
        val jwksUrl: JwkProvider = JwtUtil.makeJwkProvider(config()[Key("ISSO_JWKS_URL", stringType)]),
        val jwtIssuer: String = config()[Key("ISSO_ISSUER", stringType)]
    )

    data class Azure(
        val adDiscoveryUrl: String = config()[Key("AAD_DISCOVERY_URL", stringType)],
        val adClientId: String =  config()[Key("VEILARBLOGIN_AAD_CLIENT_ID", stringType)]
    )

    data class DB(
        val url: String = config()[Key("veilarbfilter_DB_URL", stringType)],
        val name: String = config()[Key("veilarbfilter_DB_NAME", stringType)],
        val username: String = config()[Key("veilarbfilter_DB_USERNAME", stringType)],
        val password: String = config()[Key("veilarbfilter_DB_PASSWORD", stringType)],
        val vaultMountPath: String = config()[Key("VAULT_MOUNT_PATH", stringType)]
    )

    data class Abac(
        val url: String = config()[Key("ABAC_PDP_ENDPOINT_URL", stringType)]
    )

    data class VeilarbveilederConfig(
        val url: String = "http://veilarbveileder.${requireNamespace()}.svc.nais.local/veilarbveileder"
    )
}

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties
