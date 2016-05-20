package de.dbremes.dbtradealert.DbAccess;

import android.provider.BaseColumns;

public final class QuoteContract {
    // Empty constructor prevents accidentally instantiating the contract class
    public QuoteContract() {}

    // Inner class defines column names
    public static abstract class Quote {
        public final static String TABLE = "quote";
        public final static String ASK = "ask";
        public final static String AVERAGE_DAILY_VOLUME = "average_daily_volume";
        public final static String BID = "bid";
        public final static String CURRENCY = "currency";
        public final static String DAYS_HIGH = "days_high";
        public final static String DAYS_LOW = "days_low";
        public final static String ID = BaseColumns._ID;
        public final static String LAST_PRICE = "last_price";
        public final static String LAST_PRICE_DATE_TIME = "last_price_date_time";
        public final static String NAME = "name";
        public final static String OPEN = "open";
        public final static String PERCENT_CHANGE = "percent_change";
        public final static String PREVIOUS_CLOSE = "previous_close";
        public final static String STOCK_EXCHANGE_NAME = "stock_exchange_name";
        public final static String SECURITY_ID = "security_id";
        public final static String SYMBOL = "symbol";
        public final static String VOLUME = "volume";
    }
}
