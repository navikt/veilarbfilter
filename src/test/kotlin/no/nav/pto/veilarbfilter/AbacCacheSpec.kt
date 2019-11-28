package no.nav.pto.veilarbfilter

import junit.framework.Assert.assertEquals
import no.nav.pto.veilarbfilter.abac.AbacCache
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on


object AbacCacheSpec: Spek({

    describe("Test innskudd i cachen") {
        given("Abac cache") {
            val abacCache = AbacCache()
            on("legg til veileder og besluttning") {
                val ident = "Z007";
                val enhetId = "NAV 001"
                val harTilgang = false
                abacCache.leggTilEnhetICachen(ident, enhetId, harTilgang)
                it("should return the result of adding the first number to the second number") {
                    assertEquals(false, abacCache.harTilgangTilEnheten(ident, enhetId))
                }
            }
        }
    }

    describe("Test sletting av cache etter utløpstid") {
        given("Abac cache") {
            val abacCache = AbacCache(0)
            on("legg til veileder og besluttning") {
                val ident = "Z007";
                val enhetId = "NAV 001"
                val harTilgang = false
                abacCache.leggTilEnhetICachen(ident, enhetId, harTilgang)
                it("should return the result of adding the first number to the second number") {
                    assertEquals(null, abacCache.harTilgangTilEnheten(ident, enhetId))
                }
            }
        }
    }
})