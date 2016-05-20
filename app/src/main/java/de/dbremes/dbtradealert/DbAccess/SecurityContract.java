package de.dbremes.dbtradealert.DbAccess;

import android.provider.BaseColumns;

public final class SecurityContract {
    // Empty constructor prevents accidentally instantiating the contract class
    public SecurityContract() {}

    // Inner class defines column names
    public static abstract class Security {
        public static final String TABLE = "security";
        public static final String BASE_PRICE = "base_price";
        public static final String BASE_PRICE_DATE = "base_price_date";
        public final static String ID = BaseColumns._ID;
        public final static String LOWER_TARGET = "lower_target";
        public final static String MAX_PRICE = "max_price";
        public final static String MAX_PRICE_DATE = "max_price_date";
        public final static String NOTES = "notes";
        public final static String SYMBOL = "symbol";
        public final static String TRAILING_TARGET = "trailing_target";
        public final static String UPPER_TARGET = "upper_target";
    }
}
