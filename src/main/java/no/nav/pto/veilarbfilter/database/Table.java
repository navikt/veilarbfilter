package no.nav.pto.veilarbfilter.database;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Table {
    public static class OverblikkVisning {
        public static final String TABLE_NAME = "OverblikkVisning";
        public static final String OVERBLIKK_VISNING_ID = "overblikk_visning_id";
        public static final String VEILEDER_ID = "veileder_id";
        public static final String SIST_ENDRET = "sist_endret";
        public static final String VISNING = "visning";
        private OverblikkVisning() { throw new UnsupportedOperationException(); }
    }
}

