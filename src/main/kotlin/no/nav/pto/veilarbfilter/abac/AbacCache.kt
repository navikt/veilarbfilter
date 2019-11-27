package no.nav.pto.veilarbfilter.abac

//DETTA ÄR GODTYCKLIGT VALGT.
// VILL INTE GÅ MOT ABAC FÖR OFTA DÅ VEILARBFILTER KOMMER KALLAS VARJE GÅNG VI RENDRAR OVERSIKTEN
private const val TEN_MIN_CACHE_EXPIRATION_TIME = 1000 * 60 * 10

data class AbacValue (val enhetId: String, val timestamp: Long, val harTilgang: Boolean)
typealias VeilederIdent = String;

class AbacCache(cacheExpiration: Int = TEN_MIN_CACHE_EXPIRATION_TIME) {
    private val cache = HashMap<VeilederIdent, MutableList<AbacValue>>()
    private val cacheExpiration = cacheExpiration;

    fun harTilgangTilEnheten(ident: String, enhetId: String): Boolean? =
        cache[ident]
            ?.let {
                it.find { abacvalue -> abacvalue.enhetId == enhetId }
                    ?.let {value ->
                        if(cacheHasExpired(value.timestamp)) {
                            it.filter { abacvalue -> abacvalue.enhetId == enhetId }
                            return null
                        }
                        return value.harTilgang
                    }
            }


    fun leggTilEnhetICachen (ident: String, enhetId: String, harTilgang: Boolean) {
        cache.getOrPut(ident){ mutableListOf()} += AbacValue(enhetId, System.currentTimeMillis(), harTilgang)
    }

    private fun cacheHasExpired(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) > cacheExpiration
    }

}