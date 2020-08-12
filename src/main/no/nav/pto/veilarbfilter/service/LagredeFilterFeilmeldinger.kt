package no.nav.pto.veilarbfilter.service;

enum class LagredeFilterFeilmeldinger(val message: String) {
    NAVN_FOR_LANGT("Lengden på navnet kan ikke være mer enn 255 karakterer"),
    NAVN_TOMT("Navn kan ikke være tomt"),
    FILTERVALG_TOMT("Filtervalg kan ikke være tomt"),
    NAVN_EKSISTERER("Navn eksisterer i et annet lagret filter"),
    FILTERVALG_EKSISTERER("Filterkombinasjon eksisterer i et annet lagret filter");
}
