package no.nav.pto.veilarbfilter.database;

public class Table {
    public static final class Filter {
        public static final String TABLE_NAME = "Filter";
        public static final String FILTER_ID = "filter_id";
        public static final String FILTER_NAVN = "filter_navn";
        public static final String VALGTE_FILTER = "valgte_filter";
        public static final String OPPRETTET = "opprettet";
        public static final String FILTER_CLEANUP = "filter_cleanup";
    }

    public static final class MineLagredeFilter {
        public static final String TABLE_NAME = "MineLagredeFilter";
        public static final String FILTER_ID = "filter_id";
        public static final String VEILEDER_ID = "veileder_id";
        public static final String AKTIV = "aktiv";
        public static final String NOTE = "note";
        public static final String SORT_ORDER = "sort_order";
    }

    public static final class VeilederGrupperFilter {
        public static final String TABLE_NAME = "EnhetensLagredeFilter";
        public static final String FILTER_ID = "filter_id";
        public static final String ENHET_ID = "enhet_id";
        public static final String SORT_ORDER = "sort_order";

    }

    public static final class MetricsReporterInfo {
        public static final String TABLE_NAME = "MetricsReporterInfo";
        public static final String REPORTER_ID = "reporter_id";
        public static final String OPPRETTET = "opprettet";
    }
}
