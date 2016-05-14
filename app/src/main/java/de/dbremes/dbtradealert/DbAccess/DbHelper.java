package de.dbremes.dbtradealert.DbAccess;

import de.dbremes.dbtradealert.DbAccess.QuoteContract.Quote;
import de.dbremes.dbtradealert.DbAccess.SecurityContract.Security;
import de.dbremes.dbtradealert.DbAccess.SecuritiesInWatchlistsContract.SecuritiesInWatchlists;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract.Watchlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static final String CLASS_NAME = "DbHelper";
    private static final String DB_NAME = "dbtradealert.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase dummyDb;

    public DbHelper(Context context) {
        super(context, DbHelper.DB_NAME, null, DbHelper.DB_VERSION);
        // onCreate() will be called on 1st use of the database
        // this will use it for testing purposes:
        dummyDb = getWritableDatabase();
    } // ctor()

    private void createQuoteTable(SQLiteDatabase db) {
        String columnDefinitions = (Quote.ASK + " REAL, ") +
                Quote.AVERAGE_DAILY_VOLUME + " INTEGER, " +
                Quote.BID + " REAL, " +
                Quote.CURRENCY + " TEXT, " +
                Quote.DAYS_HIGH + " REAL, " +
                Quote.DAYS_LOW + " REAL, " +
                Quote.ID + " INTEGER PRIMARY KEY, " +
                Quote.LAST_TRADE + " REAL, " +
                Quote.LAST_TRADE_DATE_TIME + " TEXT, " +
                Quote.NAME + " TEXT, " +
                Quote.OPEN + " REAL, " +
                Quote.PERCENT_CHANGE + " REAL, " +
                Quote.PREVIOUS_CLOSE + " REAL, " +
                Quote.SECURITY_ID
                + " INTEGER REFERENCES " + Security.TABLE + ", " +
                Quote.STOCK_EXCHANGE_NAME + " TEXT, " +
                Quote.SYMBOL + " TEXT, " +
                Quote.VOLUME + " INTEGER";
        String sql = String.format("CREATE TABLE %s (%s);",
                Quote.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME, "createQuoteTable created with SQL = " + sql);
    } // createQuoteTable()

    private void createSecuritiesInWatchListsTable(SQLiteDatabase db) {
        String columnDefinitions = (SecuritiesInWatchlists.SECURITY_ID
                + " INTEGER REFERENCES " + Security.TABLE + ", ") +
                SecuritiesInWatchlists.WATCHLIST_ID
                + " INTEGER REFERENCES " + Watchlist.TABLE;
        String sql = String.format("CREATE TABLE %s (%s);",
                SecuritiesInWatchlists.TABLE,
                columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME,
                "createSecuritiesInWatchListsTable created with SQL = "
                        + sql);
    } // createSecuritiesInWatchListsTable()

    private void createSecurityTable(SQLiteDatabase db) {
        String columnDefinitions = (Security.BASE_DATE + " TEXT, ") +
                Security.BASE_VALUE + " REAL, " +
                Security.GENERATE_STOP_LOSS_SIGNAL + " INTEGER, " +
                Security.ID + " INTEGER PRIMARY KEY, " +
                Security.MAX_HIGH + " REAL, " +
                Security.MAX_HIGH_DATE + " TEXT, " +
                Security.LOWER_TARGET + " REAL, " +
                Security.NOTES + " TEXT, " +
                Security.SYMBOL + " TEXT UNIQUE, " +
                Security.UPPER_TARGET + " REAL";
        String sql = String.format("CREATE TABLE %s (%s);",
                Security.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME, "createSecurityTable created with SQL = " + sql);
    } // createSecurityTable()

    private void createTables(SQLiteDatabase db) {
        createSecurityTable(db);
        createWatchListTable(db);
        createQuoteTable(db);
        createSecuritiesInWatchListsTable(db);
    } // createTables()

    private void createWatchListTable(SQLiteDatabase db) {
        String columnDefinitions = (Watchlist.ID + " INTEGER PRIMARY KEY, ") +
                Watchlist.NAME + " TEXT";
        String sql = String.format("CREATE TABLE %s (%s);",
                Watchlist.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME, "createWatchListTable created with SQL = " + sql);
    } // createWatchListTable()

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
