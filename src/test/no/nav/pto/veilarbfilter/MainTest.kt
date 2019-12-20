package no.nav.pto.veilarbfilter

import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.config.Configuration

fun main() {

    val mockConfig = Configuration(
        clustername = "",
        serviceUser = NaisUtils.Credentials("foo", "bar"),
        abac = Configuration.Abac(""),
        veilarbveilederConfig = Configuration.VeilarbveilederConfig("")

    )

    main(configuration = mockConfig)
}
