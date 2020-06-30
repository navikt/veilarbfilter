package no.nav.pto.veilarbfilter

data class ApiResponse<T>(val responseCode: Int, val responseValue: T)