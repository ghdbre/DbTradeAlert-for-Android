package de.dbremes.dbtradealert.DbAccess;

public final class SecuritiesInWatchlistsContract {
    // Empty constructor prevents accidentally instantiating the contract class
    public SecuritiesInWatchlistsContract() {}

    // Inner class defines column names
    public static abstract class SecuritiesInWatchlists {
        public final static String TABLE = "securities_in_watchlists";
        public final static String SECURITY_ID = "security_id";
        public final static String WATCHLIST_ID = "watchlist_id";
    }
}
