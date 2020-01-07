package no.nav.pto.veilarbfilter.client

import no.nav.pto.veilarbfilter.abac.AbacCache


//DETTA ÄR GODTYCKLIGT VALGT.
// VILL INTE GÅ MOT ABAC FÖR OFTA DÅ VEILARBFILTER KOMMER KALLAS VARJE GÅNG VI RENDRAR OVERSIKTEN
private const val TEN_MIN_CACHE_EXPIRATION_TIME = 1000 * 60 * 10

data class VeilederCacheValue (val veiledere: VeiledereResponse, val timestamp: Long)

class VeilederCache(private val cacheExpiration: Int = TEN_MIN_CACHE_EXPIRATION_TIME) {
    private val cache = HashMap<String, VeilederCacheValue>()
    fun veilederePaEnheten(enhetId: String): VeiledereResponse? =
        cache[enhetId]
            ?.let {
                if(cacheHasExpired(it.timestamp)) {
                    cache.remove(enhetId)
                    return null
                }
                return it.veiledere
            }


    fun leggTilEnhetICachen (enhetId: String, veiledere: VeiledereResponse) {
        cache[enhetId] = VeilederCacheValue(veiledere, System.currentTimeMillis())
    }

    private fun cacheHasExpired(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) > cacheExpiration
    }

}