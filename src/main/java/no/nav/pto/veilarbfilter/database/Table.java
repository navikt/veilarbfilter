package no.nav.pto.veilarbfilter.database;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Table {
    public static class Filter {
        public static final String TABLE_NAME = "Filter";
        public static final String FILTER_ID = "filter_id";
        public static final String FILTER_NAVN = "filter_navn";
        public static final String VALGTE_FILTER = "valgte_filter";
        public static final String OPPRETTET = "opprettet";
        public static final String FILTER_CLEANUP = "filter_cleanup";
        public static final String FILTER_ID_SEQUENCE = "filter_filter_id_seq";
        private Filter() { throw new UnsupportedOperationException(); }
    }

    public static class MineLagredeFilter {
        public static final String TABLE_NAME = "minelagredefilter";
        public static final String FILTER_ID = "mine_filter_id";
        public static final String VEILEDER_ID = "veileder_id";
        public static final String AKTIV = "aktiv";
        public static final String NOTE = "note";
        public static final String SORT_ORDER = "sort_order";
        private MineLagredeFilter() { throw new UnsupportedOperationException(); }
    }

    public static class VeilederGrupperFilter {
        public static final String TABLE_NAME = "veiledergrupperfilter";
        public static final String FILTER_ID = "veiledergruppe_filter_id";
        public static final String ENHET_ID = "enhet_id";
        private VeilederGrupperFilter() { throw new UnsupportedOperationException(); }
    }

    public static class OverblikkVisning {
        public static final String TABLE_NAME = "OverblikkVisning";
        public static final String OVERBLIKK_VISNING_ID = "overblikk_visning_id";
        public static final String VEILEDER_ID = "veileder_id";
        public static final String SIST_ENDRET = "sist_endret";
        public static final String VISNING = "visning";
        private OverblikkVisning() { throw new UnsupportedOperationException(); }
    }
}

