package de.dbremes.dbtradealert.DbAccess;

import de.dbremes.dbtradealert.DbAccess.QuoteContract.Quote;
import de.dbremes.dbtradealert.DbAccess.SecurityContract.Security;
import de.dbremes.dbtradealert.DbAccess.SecuritiesInWatchlistsContract.SecuritiesInWatchlists;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract.Watchlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static final String CLASS_NAME = "DbHelper";
    private static final String DB_NAME = "dbtradealert.db";
    private static final int DB_VERSION = 1;
    // strings for logging
    private final static String CURSOR_COUNT_FORMAT = "%s: cursor.getCount() = %d";

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
                + " INTEGER REFERENCES " + Security.TABLE + " NOT NULL, " +
                Quote.STOCK_EXCHANGE_NAME + " TEXT, " +
                Quote.SYMBOL + " TEXT, " +
                Quote.VOLUME + " INTEGER";
        String sql = String.format("CREATE TABLE %s (%s);",
                Quote.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME, "createQuoteTable created with SQL = " + sql);
    } // createQuoteTable()

    private void createSampleData(SQLiteDatabase db) {
        final String methodName = "createSampleData";
        final String nullColumnHack = null;
        try {
            db.beginTransaction();
            // region Create sample security data
            // region - BAYN.DE
            ContentValues contentValues = new ContentValues();
            contentValues.put(Security.BASE_DATE, (String) null);
            contentValues.put(Security.BASE_VALUE, (Float) null);
            contentValues.put(Security.LOWER_TARGET, (Float) null);
            contentValues.put(Security.MAX_HIGH, 138.34);
            contentValues.put(Security.MAX_HIGH_DATE, "2015-07-16T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "BAYN.DE");
            contentValues.put(Security.TRAILING_TARGET, (Float) null);
            contentValues.put(Security.UPPER_TARGET, 96);
            long baydeSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'BAYN.DE' created");
            // endregion - BAYN.DE
            // region - NESN.VX
            contentValues.clear();
            contentValues.put(Security.BASE_DATE, (String) null);
            contentValues.put(Security.BASE_VALUE, (Float) null);
            contentValues.put(Security.LOWER_TARGET, (Float) null);
            contentValues.put(Security.MAX_HIGH, 76.95);
            contentValues.put(Security.MAX_HIGH_DATE, "2015-12-02T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "NESN.VX");
            contentValues.put(Security.TRAILING_TARGET, 10);
            contentValues.put(Security.UPPER_TARGET, (Float) null);
            long nesnSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'NESN.VX' created");
            // endregion - NESN.VX
            // region - NOVN.VX
            contentValues.clear();
            contentValues.put(Security.BASE_DATE, "2015-01-28T12:00");
            contentValues.put(Security.BASE_VALUE, 77.45);
            contentValues.put(Security.LOWER_TARGET, 65);
            contentValues.put(Security.MAX_HIGH, 102.30);
            contentValues.put(Security.MAX_HIGH_DATE, "2015-07-20T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "NOVN.VX");
            contentValues.put(Security.TRAILING_TARGET, 10);
            contentValues.put(Security.UPPER_TARGET, (Float) null);
            long novnSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'NOVN.VX' created");
            // endregion - NOVN.VX
            // region - SIE.DE
            contentValues.clear();
            contentValues.put(Security.BASE_DATE, "2015-01-04T12:00");
            contentValues.put(Security.BASE_VALUE, 96.197);
            contentValues.put(Security.LOWER_TARGET, (Float) null);
            contentValues.put(Security.MAX_HIGH, 96.131);
            contentValues.put(Security.MAX_HIGH_DATE, "2015-04-26T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "SIE.DE");
            contentValues.put(Security.TRAILING_TARGET, (Float) null);
            contentValues.put(Security.UPPER_TARGET, 100);
            long siedeSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'SIE.DE' created");
            // endregion - SIE.DE
            // endregion Create sample security data

            // region Create sample watchlist data
            // - CH Stocks
            contentValues.clear();
            contentValues.put(Watchlist.NAME, "CH Stocks");
            long chWatchListID = db.insert(Watchlist.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample watchlist 'CH Stocks' created");
            // - D Stocks
            contentValues.clear();
            contentValues.put(Watchlist.NAME, "D Stocks");
            long dWatchListID = db.insert(Watchlist.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample watchlist 'D Stocks' created");
            // endregion Create sample watchlist data

            // region Include stocks in watchlists
            // region - Include BAYN.DE in D Stocks
            contentValues.clear();
            contentValues.put(SecuritiesInWatchlists.SECURITY_ID, baydeSecurityID);
            contentValues.put(SecuritiesInWatchlists.WATCHLIST_ID, dWatchListID);
            db.insert(SecuritiesInWatchlists.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Stock 'BAYN.DE' included in watchlist 'D Stocks'");
            // endregion - Include BAYN.DE in D Stocks
            // region - Include NESN.VX in CH Stocks
            contentValues.clear();
            contentValues.put(SecuritiesInWatchlists.SECURITY_ID, nesnSecurityID);
            contentValues.put(SecuritiesInWatchlists.WATCHLIST_ID, chWatchListID);
            db.insert(SecuritiesInWatchlists.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Stock 'NESN.VX' included in watchlist 'CH Stocks'");
            // endregion - Include NESN.VX in CH Stocks
            // region - Include NOVN.VX in CH Stocks
            contentValues.clear();
            contentValues.put(SecuritiesInWatchlists.SECURITY_ID, novnSecurityID);
            contentValues.put(SecuritiesInWatchlists.WATCHLIST_ID, chWatchListID);
            db.insert(SecuritiesInWatchlists.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Stock 'NOVN.VX' included in watchlist 'CH Stocks'");
            // endregion - Include NOVN.VX in CH Stocks
            // region - Include SIE.DE in D Stocks
            contentValues.clear();
            contentValues.put(SecuritiesInWatchlists.SECURITY_ID, siedeSecurityID);
            contentValues.put(SecuritiesInWatchlists.WATCHLIST_ID, dWatchListID);
            db.insert(SecuritiesInWatchlists.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Stock 'SIE.DE' included in watchlist 'D Stocks'");
            // endregion - Include SIE.DE in D Stocks
            // endregion Include stocks in watchlists

            // region Create sample quote data
            // region - BAYN.DE
            contentValues.clear();
            contentValues.put(Quote.ASK, 96.13);
            contentValues.put(Quote.AVERAGE_DAILY_VOLUME, 2524950);
            contentValues.put(Quote.BID, 96.10);
            contentValues.put(Quote.CURRENCY, "EUR");
            contentValues.put(Quote.DAYS_HIGH, 96.36);
            contentValues.put(Quote.DAYS_LOW, 93.83);
            contentValues.put(Quote.LAST_TRADE, 96.14);
            contentValues.put(Quote.LAST_TRADE_DATE_TIME, "2016-05-13T17:35");
            contentValues.put(Quote.NAME, "Bayer AG");
            contentValues.put(Quote.OPEN, 94.50);
            contentValues.put(Quote.PERCENT_CHANGE, 1.04);
            contentValues.put(Quote.PREVIOUS_CLOSE, 95.15);
            contentValues.put(Quote.STOCK_EXCHANGE_NAME, "XETRA");
            contentValues.put(Quote.SECURITY_ID, baydeSecurityID);
            contentValues.put(Quote.SYMBOL, "BAYN.DE");
            contentValues.put(Quote.VOLUME, 3682711);
            db.insert(Quote.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample quote for 'BAYN.DE' created");
            // endregion - BAYN.DE
            // region - NESN.VX
            contentValues.clear();
            contentValues.put(Quote.ASK, 73);
            contentValues.put(Quote.AVERAGE_DAILY_VOLUME, 666356);
            contentValues.put(Quote.BID, 72.95);
            contentValues.put(Quote.CURRENCY, "CHF");
            contentValues.put(Quote.DAYS_HIGH, 73.15);
            contentValues.put(Quote.DAYS_LOW, 71.95);
            contentValues.put(Quote.LAST_TRADE, 73);
            contentValues.put(Quote.LAST_TRADE_DATE_TIME, "2016-05-13T17:26");
            contentValues.put(Quote.NAME, "Nestle N");
            contentValues.put(Quote.OPEN, 72.45);
            contentValues.put(Quote.PERCENT_CHANGE, 0.55);
            contentValues.put(Quote.PREVIOUS_CLOSE, 72.60);
            contentValues.put(Quote.STOCK_EXCHANGE_NAME, "VTX");
            contentValues.put(Quote.SECURITY_ID, nesnSecurityID);
            contentValues.put(Quote.SYMBOL, "NESN.VX");
            contentValues.put(Quote.VOLUME, 4678462);
            db.insert(Quote.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample quote for 'NESN.VX' created");
            // endregion - NESN.VX
            // region - NOVN.VX
            contentValues.clear();
            contentValues.put(Quote.ASK, 73.35);
            contentValues.put(Quote.AVERAGE_DAILY_VOLUME, 5210000);
            contentValues.put(Quote.BID, 73.30);
            contentValues.put(Quote.CURRENCY, "CHF");
            contentValues.put(Quote.DAYS_HIGH, 73.50);
            contentValues.put(Quote.DAYS_LOW, 72.05);
            contentValues.put(Quote.LAST_TRADE, 73.30);
            contentValues.put(Quote.LAST_TRADE_DATE_TIME, "2016-05-13T17:31");
            contentValues.put(Quote.NAME, "Novartis N");
            contentValues.put(Quote.OPEN, 72.30);
            contentValues.put(Quote.PERCENT_CHANGE, 0.96);
            contentValues.put(Quote.PREVIOUS_CLOSE, 72.60);
            contentValues.put(Quote.STOCK_EXCHANGE_NAME, "VTX");
            contentValues.put(Quote.SECURITY_ID, novnSecurityID);
            contentValues.put(Quote.SYMBOL, "NOVN.VX");
            contentValues.put(Quote.VOLUME, 5016792);
            db.insert(Quote.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample quote for 'NESN.VX' created");
            // endregion - NOVN.VX
            // region - SIE.DE
            contentValues.clear();
            contentValues.put(Quote.ASK, 93.65);
            contentValues.put(Quote.AVERAGE_DAILY_VOLUME, 2317120);
            contentValues.put(Quote.BID, 93.61);
            contentValues.put(Quote.CURRENCY, "EUR");
            contentValues.put(Quote.DAYS_HIGH, 93.94);
            contentValues.put(Quote.DAYS_LOW, 91.89);
            contentValues.put(Quote.LAST_TRADE, 93.60);
            contentValues.put(Quote.LAST_TRADE_DATE_TIME, "2016-05-13T17:35");
            contentValues.put(Quote.NAME, "Bayer AG");
            contentValues.put(Quote.OPEN, 92.10);
            contentValues.put(Quote.PERCENT_CHANGE, 0.39);
            contentValues.put(Quote.PREVIOUS_CLOSE, 93.24);
            contentValues.put(Quote.STOCK_EXCHANGE_NAME, "XETRA");
            contentValues.put(Quote.SECURITY_ID, siedeSecurityID);
            contentValues.put(Quote.SYMBOL, "SIE.DE");
            contentValues.put(Quote.VOLUME, 1959164);
            db.insert(Quote.TABLE, null, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample quote for 'SIE.DE' created");
            // endregion - SIE.DE
            db.setTransactionSuccessful();
            // endregion Create sample quote data
            Log.d(DbHelper.CLASS_NAME, methodName + ": success!");
        } finally {
            db.endTransaction();
        }
    } // createSampleData()

    private void createSecuritiesInWatchListsTable(SQLiteDatabase db) {
        String columnDefinitions = (SecuritiesInWatchlists.SECURITY_ID
                + " INTEGER REFERENCES " + Security.TABLE + " NOT NULL, ") +
                SecuritiesInWatchlists.WATCHLIST_ID
                + " INTEGER REFERENCES " + Watchlist.TABLE  + " NOT NULL";
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
                Security.ID + " INTEGER PRIMARY KEY, " +
                Security.MAX_HIGH + " REAL, " +
                Security.MAX_HIGH_DATE + " TEXT, " +
                Security.LOWER_TARGET + " REAL, " +
                Security.NOTES + " TEXT, " +
                Security.SYMBOL + " TEXT UNIQUE, " +
                Security.TRAILING_TARGET + " REAL, " +
                Security.UPPER_TARGET + " REAL";
        String sql = String.format("CREATE TABLE %s (%s);",
                Security.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME, "createSecurityTable created with SQL = " + sql);
    } // createSecurityTable()

    private void createTables(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            createSecurityTable(db);
            createWatchListTable(db);
            createQuoteTable(db);
            createSecuritiesInWatchListsTable(db);
            db.setTransactionSuccessful();
            Log.d(DbHelper.CLASS_NAME, "createTables: success!");
        } finally {
            db.endTransaction();
        }
    } // createTables()

    private void createWatchListTable(SQLiteDatabase db) {
        String columnDefinitions = (Watchlist.ID + " INTEGER PRIMARY KEY, ") +
                Watchlist.NAME + " TEXT";
        String sql = String.format("CREATE TABLE %s (%s);",
                Watchlist.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(DbHelper.CLASS_NAME, "createWatchListTable created with SQL = " + sql);
    } // createWatchListTable()

    public long getWatchlistIdByPosition(int position) {
        final String methodName = "getWatchlistIdByPosition";
        int result = -1;
        Cursor c = readAllWatchlists();
        if (c.getCount() >= position) {
            c.moveToPosition(position);
            result = c.getInt(c.getColumnIndex(Watchlist.ID));
        } else {
            Log.w(DbHelper.CLASS_NAME, String.format(
                    "%s: cannot move to position = %d; cursor.getCount() = %d",
                    methodName, position, c.getCount()));
        }
        c.close();
        return result;
    } // getWatchlistIdByPosition()

    private String insertSelectionArgs(String selection, String[] selectionArgs) {
         final String methodName = "insertSelectionArgs";
         String result = "";
         StringBuilder sb = new StringBuilder();
         String[] selectionArray = selection.split("\\?");
//         if (selectionArray.length != selectionArgs.length){
//            Log.w(DbHelper.CLASS_NAME,
//                    String.format("%s: selectionArray.length = %d, selectionArgs.length = %d; ",
//                            methodName, selectionArray.length, selectionArgs.length));
//         }
         for (int i = 0; i < selectionArgs.length; i++) {
             sb.append(selectionArray[i] + selectionArgs[i]);
         }
         if (selectionArray.length > selectionArgs.length) {
             sb.append(selectionArray[selectionArray.length - 1]);
         }
         result = sb.toString();
         return result;
     } // insertSelectionArgs()

    private void logSql(String methodName, String selection,
                        String[] selectionArgs) {
         Log.v(DbHelper.CLASS_NAME,
         methodName + ": "
         + insertSelectionArgs(selection, selectionArgs));
    } // logSql()

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor readAllQuotesForWatchlist(long watchlistId) {
        final String methodName = "readAllQuotesForWatchlist";
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT q.*, s." + Security.BASE_VALUE + ", s."
                + Security.LOWER_TARGET + ", s." + Security.MAX_HIGH
                + ", s." + Security.TRAILING_TARGET + ", s."
                + Security.UPPER_TARGET + "\nFROM "
                + SecuritiesInWatchlists.TABLE + " siwl" + "\n\tINNER JOIN "
                + Quote.TABLE + " q ON q." + Quote.SECURITY_ID
                + " = " + "siwl." + SecuritiesInWatchlists.SECURITY_ID + "\n\tINNER JOIN "
                + Security.TABLE + " s ON s." + Security.ID + " = " + "q."
                + Quote.SECURITY_ID + "\nWHERE siwl."
                + SecuritiesInWatchlists.WATCHLIST_ID + " = ?" + "\nORDER BY q."
                + Quote.NAME + " ASC";
        String[] selectionArgs = new String[] { String.valueOf(watchlistId) };
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        Log.v(DbHelper.CLASS_NAME,
                String.format(DbHelper.CURSOR_COUNT_FORMAT, methodName,
                        cursor.getCount()));
        return cursor;
    } // readAllQuotesForWatchlist()

    public Cursor readAllWatchlists() {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[] { Watchlist.ID, Watchlist.NAME };
        String groupBy = null;
        String having = null;
        String orderBy = Watchlist.NAME;
        String selection = null;
        String[] selectionArgs = null;
        String table = Watchlist.TABLE;
        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    } // readAllWatchlists()

}
