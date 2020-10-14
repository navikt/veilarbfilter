package no.nav.pto.veilarbfilter.config

import no.nav.common.auth.oidc.filter.OidcAuthenticator
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig
import no.nav.common.auth.subject.IdentType
import org.mockito.Mockito
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

class AzureAuth(configuration: Configuration) {
    private val configuration = configuration

    fun auth(token: String): Boolean {
        val a = OidcAuthenticator.fromConfig(
            OidcAuthenticatorConfig()
                .withDiscoveryUrl(configuration.azureConfig.adDiscoveryUrl)
                .withClientId(configuration.azureConfig.adClientId)
                .withIdTokenCookieName("isso-idtoken")
                .withIdentType(IdentType.InternBruker)
        )

        val request: HttpServletRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.cookies).thenReturn(arrayOf(Cookie("isso-idtoken", token)))


        return a.findIdToken(request).isPresent
    }
}
