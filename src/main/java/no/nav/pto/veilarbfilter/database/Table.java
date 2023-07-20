package no.nav.pto.veilarbfilter.database;

public class Table {
    public static class Filter {
        public static String TABLE_NAME = "Filter";
        public static String FILTER_ID = "filter_id";
        public static String FILTER_NAVN = "filter_navn";
        public static String VALGTE_FILTER = "valgte_filter";
        public static String OPPRETTET = "opprettet";
        public static String FILTER_CLEANUP = "filter_cleanup";
    }

    public static class MineLagredeFilter {
        public static String TABLE_NAME = "minelagredefilter";
        public static String FILTER_ID = "mine_filter_id";
        public static String VEILEDER_ID = "veileder_id";
        public static String AKTIV = "aktiv";
        public static String NOTE = "note";
        public static String SORT_ORDER = "sort_order";
    }

    public static class VeilederGrupperFilter {
        public static String TABLE_NAME = "veiledergrupperfilter";
        public static String FILTER_ID = "veiledergruppe_filter_id";
        public static String ENHET_ID = "enhet_id";

    }

    public static class MetricsReporterInfo {
        public static String TABLE_NAME = "MetricsReporterInfo";
        public static String REPORTER_ID = "reporter_id";
        public static String OPPRETTET = "opprettet";
    }

    public static class OverblikkVisning {
        public static String TABLE_NAME = "OverblikkVisning";
        public static String VEILEDER_ID = "veileder_id";
        public static String OPPRETTET = "opprettet";
        public static String OVERBLIKK_VISNING = "overblikk_visning";
    }
}

