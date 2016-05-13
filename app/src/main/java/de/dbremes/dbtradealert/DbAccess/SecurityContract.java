package de.dbremes.dbtradealert.DbAccess;

import android.provider.BaseColumns;

public final class SecurityContract {
    // Empty constructor prevents accidentally instantiating the contract class
    public SecurityContract() {}

    // Inner class defines column names
    public static abstract class Security {
        public static final String TABLE = "security";
        public static final String BASE_DATE = "base_date";
        public static final String BASE_VALUE = "base_value";
        public final static String GENERATE_STOP_LOSS_SIGNAL = "generate_stop_loss_signal";
        public final static String ID = BaseColumns._ID;
        public final static String LOWER_TARGET = "lower_target";
        public final static String MAX_HIGH = "max_high";
        public final static String MAX_HIGH_DATE = "max_high_date";
        public final static String NOTES = "notes";
        public final static String SYMBOL = "symbol";
        public final static String UPPER_TARGET = "upper_target";
    }
}
