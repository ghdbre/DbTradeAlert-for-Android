package de.dbremes.dbtradealert.DbAccess;

import android.provider.BaseColumns;

public final class WatchlistContract {
    // Empty constructor prevents accidentally instantiating the contract class
    public WatchlistContract() {}

    // Inner class defines column names
    public static abstract class Watchlist {
        public final static String TABLE = "watchlist";
        public final static String ID = BaseColumns._ID;
        public final static String NAME = "name";
    }
}
