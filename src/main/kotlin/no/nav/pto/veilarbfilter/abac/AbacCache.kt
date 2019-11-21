package no.nav.pto.veilarbfilter.abac

//DETTA ÄR GODTYCKLIGT VALGT.
// VILL INTE GÅ MOT ABAC FÖR OFTA DÅ VEILARBFILTER KOMMER KALLAS VARJE GÅNG VI RENDRAR OVERSIKTEN
private const val TEN_MIN_CACHE_EXPIRATION_TIME = 1000 * 60 * 10

data class AbacValue (val timestamp: Long, val harTilgang: Boolean)

class AbacCache() {
    private val cache = HashMap<String, HashMap<String, AbacValue>>()

    fun harTilgangTilEnheten(ident: String, enhetId: String): Boolean? =
        cache[ident]
            ?.let {
                it[enhetId]
                    ?.let {value ->
                        if(cacheHasExpired(value.timestamp)) {
                            it.remove(enhetId)
                            return null
                        }
                        return value.harTilgang
                    }
            }


    fun leggTilEnhetICachen (ident: String, enhetId: String, harTilgang: Boolean) =
        cache[ident]
            ?.put(enhetId, AbacValue(System.currentTimeMillis(), harTilgang))
            ?:cache.put(ident, hashMapOf(enhetId to AbacValue(System.currentTimeMillis(), harTilgang)))



    private fun cacheHasExpired(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) > TEN_MIN_CACHE_EXPIRATION_TIME
    }

}