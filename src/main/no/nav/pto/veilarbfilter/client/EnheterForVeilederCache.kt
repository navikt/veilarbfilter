package no.nav.pto.veilarbfilter.client

import no.nav.common.types.identer.EnhetId


//DETTA ÄR GODTYCKLIGT VALGT.
// VILL INTE GÅ MOT ABAC FÖR OFTA DÅ VEILARBFILTER KOMMER KALLAS VARJE GÅNG VI RENDRAR OVERSIKTEN
private const val CACHE_EXPIRATION_TIME = 1000 * 60 * 5

data class EnheterForVeilederCacheValue(val enheter: List<EnhetId>, val timestamp: Long)

class EnheterForVeilederCache(private val cacheExpiration: Int = CACHE_EXPIRATION_TIME) {
    private val cache = HashMap<String, EnheterForVeilederCacheValue>()
    fun enheterForVeileder(veilederId: String): List<EnhetId>? =
        cache[veilederId]
            ?.let {
                if (cacheHasExpired(it.timestamp)) {
                    cache.remove(veilederId)
                    return null;
                }
                return it.enheter
            }


    fun leggTilEnheterICachen(veilederId: String, enheter: List<EnhetId>) {
        cache[veilederId] = EnheterForVeilederCacheValue(enheter, System.currentTimeMillis())
    }

    private fun cacheHasExpired(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) > cacheExpiration
    }

}