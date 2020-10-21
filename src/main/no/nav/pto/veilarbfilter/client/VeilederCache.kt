package no.nav.pto.veilarbfilter.client


//DETTA ÄR GODTYCKLIGT VALGT.
// VILL INTE GÅ MOT ABAC FÖR OFTA DÅ VEILARBFILTER KOMMER KALLAS VARJE GÅNG VI RENDRAR OVERSIKTEN
private const val CACHE_EXPIRATION_TIME = 1000 * 60 * 5

data class VeilederCacheValue(val veiledere: List<String>, val timestamp: Long)

class VeilederCache(private val cacheExpiration: Int = CACHE_EXPIRATION_TIME) {
    private val cache = HashMap<String, VeilederCacheValue>()
    fun veilederePaEnheten(enhetId: String): List<String>? =
        cache[enhetId]
            ?.let {
                if (cacheHasExpired(it.timestamp)) {
                    cache.remove(enhetId)
                    return null;
                }
                return it.veiledere
            }


    fun leggTilEnhetICachen(enhetId: String, veiledere: List<String>) {
        cache[enhetId] = VeilederCacheValue(veiledere, System.currentTimeMillis())
    }

    private fun cacheHasExpired(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) > cacheExpiration
    }

}