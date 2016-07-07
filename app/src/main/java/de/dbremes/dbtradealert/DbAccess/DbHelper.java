package de.dbremes.dbtradealert.DbAccess;

import de.dbremes.dbtradealert.DbAccess.QuoteContract.Quote;
import de.dbremes.dbtradealert.DbAccess.SecurityContract.Security;
import de.dbremes.dbtradealert.DbAccess.SecuritiesInWatchlistsContract.SecuritiesInWatchlists;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract.Watchlist;
import de.dbremes.dbtradealert.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DbHelper extends SQLiteOpenHelper {
    private static final String CLASS_NAME = "DbHelper";
    public final static String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm";
    private static final String DB_NAME = "dbtradealert.db";
    private static final int DB_VERSION = 1;
    private static final String EXCEPTION_CAUGHT = "Exception caught";
    public final static long NEW_ITEM_ID = -1L;
    // Format strings for logging
    private final static String CURSOR_COUNT_FORMAT = "%s(): cursor.getCount() = %d";
    private final static String DELETE_RESULT_FORMAT = "%s(): result of db.delete() from %s = %d";
    private final static String INSERT_CONTENT_VALUES_FORMAT = "%s(): contentValues for %s: %s";
    private final static String INSERT_RESULT_FORMAT = "%s(): result of db.insert() into %s: %d";
    private final static String UPDATE_RESULT_FORMAT = "%s(): result of db.update() for %s: %d";

    // Alias for generated columns of getAllSecuritiesAndMarkIfInWatchlist() and readAllWatchlists()
    public final static String IS_SYMBOL_IN_WATCHLIST_ALIAS = "isSymbolInWatchlist";

    // region Format parameter values
    // API for Yahoo Finance (see e.g. http://brusdeylins.info/projects/yahoo-finance-api/):
    // a=Ask (0)
    // a2=Average Daily Volume (1)
    // b=Bid (2)
    // c=Change / Percent change
    // c1, c0, c6, c7=Change
    // c4=Currency (3)
    // d1=Last Trade Date (4)
    // g, g0=Day’s Low (5)
    // h, h0=Day’s High (6)
    // j6=Percent Change From 52-week Low
    // k5=Percent Change From 52-week High
    // l1=Last Trade (7)
    // m, m0=Day’s Range
    // m6=Percent Change From 200-day Moving Average
    // m8=Percent Change From 50-day Moving Average
    // n=Name (8)
    // o, o0=Open (9)
    // p, p0, p8=Previous Close (10)
    // p2, p4=Change in Percent (11)
    // s=Symbol (12)
    // s7=Short Ratio
    // t1=Last Trade Time (13)
    // v, v0, v6=Volume (14)
    // x, x0=Stock Exchange (15)
    // (n) = index of column in resulting .csv based on QuoteDownloadFormatParameter
    // watch out for parameters like b6 (Bid Size)!
    // They might return groups of numbers separated by
    // commas which will trip up parseFromQuoteCsvRow()
    // endregion
    public final static String QuoteDownloadFormatParameter = "aa2bc4d1ghl1nopp2st1vx";
    // NewItemId is used as a temporary ID until the database has stored the item
    // and issued an ID for it
    public final static long NewItemId = -1L;

    public DbHelper(Context context) {
        super(context, DbHelper.DB_NAME, null, DbHelper.DB_VERSION);
    } // ctor()

    public static void closeCursor(Cursor cursor) {
        if (cursor != null && cursor.isClosed() == false) {
            cursor.close();
        }
    } // closeCursor()

    // No need to close SQLite db as getReadableDatabase() / getWritableDatabase() always return
    // the same object. See getDatabaseLocked() in
    // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/
    // android/database/sqlite/SQLiteOpenHelper.java
//    private void closeDb(SQLiteDatabase db) {
//        if( db != null && db.isOpen()) {
//            db.close();
//        }
//    } // closeDb()

    private void createQuoteTable(SQLiteDatabase db) {
        String columnDefinitions = (Quote.ASK + " REAL, ") +
                Quote.AVERAGE_DAILY_VOLUME + " INTEGER, " +
                Quote.BID + " REAL, " +
                Quote.CURRENCY + " TEXT, " +
                Quote.DAYS_HIGH + " REAL, " +
                Quote.DAYS_LOW + " REAL, " +
                Quote.ID + " INTEGER PRIMARY KEY, " +
                Quote.LAST_PRICE + " REAL, " +
                Quote.LAST_PRICE_DATE_TIME + " TEXT, " +
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
        Log.d(CLASS_NAME, "createQuoteTable(): created with SQL = " + sql);
    } // createQuoteTable()

    private void createSampleData(SQLiteDatabase db) {
        final String methodName = "createSampleData";
        final String nullColumnHack = null;
        try {
            db.beginTransaction();
            // region Create sample security data
            // region - BAYN.DE
            ContentValues contentValues = new ContentValues();
            contentValues.put(Security.BASE_PRICE_DATE, (String) null);
            contentValues.put(Security.BASE_PRICE, (Float) null);
            contentValues.put(Security.LOWER_TARGET, (Float) null);
            contentValues.put(Security.MAX_PRICE, 138.34);
            contentValues.put(Security.MAX_PRICE_DATE, "2015-07-16T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "BAYN.DE");
            contentValues.put(Security.TRAILING_TARGET, (Float) null);
            contentValues.put(Security.UPPER_TARGET, 96);
            long baydeSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'BAYN.DE' created");
            // endregion - BAYN.DE
            // region - NESN.VX
            contentValues.clear();
            contentValues.put(Security.BASE_PRICE_DATE, (String) null);
            contentValues.put(Security.BASE_PRICE, (Float) null);
            contentValues.put(Security.LOWER_TARGET, (Float) null);
            contentValues.put(Security.MAX_PRICE, 76.95);
            contentValues.put(Security.MAX_PRICE_DATE, "2015-12-02T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "NESN.VX");
            contentValues.put(Security.TRAILING_TARGET, 10);
            contentValues.put(Security.UPPER_TARGET, (Float) null);
            long nesnSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'NESN.VX' created");
            // endregion - NESN.VX
            // region - NOVN.VX
            contentValues.clear();
            contentValues.put(Security.BASE_PRICE_DATE, "2015-01-28T12:00");
            contentValues.put(Security.BASE_PRICE, 77.45);
            contentValues.put(Security.LOWER_TARGET, 65);
            contentValues.put(Security.MAX_PRICE, 102.30);
            contentValues.put(Security.MAX_PRICE_DATE, "2015-07-20T12:00");
            contentValues.put(Security.NOTES, "Sample stock");
            contentValues.put(Security.SYMBOL, "NOVN.VX");
            contentValues.put(Security.TRAILING_TARGET, 10);
            contentValues.put(Security.UPPER_TARGET, (Float) null);
            long novnSecurityID = db.insert(Security.TABLE, nullColumnHack, contentValues);
            Log.v(DbHelper.CLASS_NAME, "Sample stock 'NOVN.VX' created");
            // endregion - NOVN.VX
            // region - SIE.DE
            contentValues.clear();
            contentValues.put(Security.BASE_PRICE_DATE, "2015-01-04T12:00");
            contentValues.put(Security.BASE_PRICE, 96.197);
            contentValues.put(Security.LOWER_TARGET, (Float) null);
            contentValues.put(Security.MAX_PRICE, 96.131);
            contentValues.put(Security.MAX_PRICE_DATE, "2015-04-26T12:00");
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
            contentValues.put(Quote.LAST_PRICE, 96.14);
            contentValues.put(Quote.LAST_PRICE_DATE_TIME, "2016-05-13T17:35");
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
            contentValues.put(Quote.LAST_PRICE, 73);
            contentValues.put(Quote.LAST_PRICE_DATE_TIME, "2016-05-13T17:26");
            contentValues.put(Quote.NAME, "Nestle N");
            contentValues.put(Quote.OPEN, 72.45);
            contentValues.put(Quote.PERCENT_CHANGE, 0.55);
            contentValues.put(Quote.PREVIOUS_CLOSE, 72.60);
            contentValues.put(Quote.STOCK_EXCHANGE_NAME, "VTX");
            contentValues.put(Quote.SECURITY_ID, nesnSecurityID);
            contentValues.put(Quote.SYMBOL, "NESN.VX");
            contentValues.put(Quote.VOLUME, 596900);
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
            contentValues.put(Quote.LAST_PRICE, 73.30);
            contentValues.put(Quote.LAST_PRICE_DATE_TIME, "2016-05-13T17:31");
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
            contentValues.put(Quote.LAST_PRICE, 93.60);
            contentValues.put(Quote.LAST_PRICE_DATE_TIME, "2016-05-13T17:35");
            contentValues.put(Quote.NAME, "Siemens AG");
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
            Log.d(DbHelper.CLASS_NAME, methodName + "(): success!");
        } finally {
            db.endTransaction();
        }
    } // createSampleData()

    private void createSecuritiesInWatchListsTable(SQLiteDatabase db) {
        String columnDefinitions = (SecuritiesInWatchlists.SECURITY_ID
                + " INTEGER REFERENCES " + Security.TABLE + " NOT NULL, ") +
                SecuritiesInWatchlists.WATCHLIST_ID
                + " INTEGER REFERENCES " + Watchlist.TABLE + " NOT NULL";
        String sql = String.format("CREATE TABLE %s (%s);",
                SecuritiesInWatchlists.TABLE,
                columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME,
                "createSecuritiesInWatchListsTable(): created with SQL = "
                        + sql);
    } // createSecuritiesInWatchListsTable()

    private void createSecurityTable(SQLiteDatabase db) {
        String columnDefinitions = (Security.BASE_PRICE_DATE + " TEXT, ") +
                Security.BASE_PRICE + " REAL, " +
                Security.ID + " INTEGER PRIMARY KEY, " +
                Security.MAX_PRICE + " REAL, " +
                Security.MAX_PRICE_DATE + " TEXT, " +
                Security.LOWER_TARGET + " REAL, " +
                Security.NOTES + " TEXT, " +
                Security.SYMBOL + " TEXT UNIQUE, " +
                Security.TRAILING_TARGET + " REAL, " +
                Security.UPPER_TARGET + " REAL";
        String sql = String.format("CREATE TABLE %s (%s);",
                Security.TABLE, columnDefinitions);
        db.execSQL(sql);
        Log.d(CLASS_NAME, "createSecurityTable(): created with SQL = " + sql);
    } // createSecurityTable()

    private void createTables(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            createSecurityTable(db);
            createWatchListTable(db);
            createQuoteTable(db);
            createSecuritiesInWatchListsTable(db);
            db.setTransactionSuccessful();
            Log.d(DbHelper.CLASS_NAME, "createTables(): success!");
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
        Log.d(DbHelper.CLASS_NAME, "createWatchListTable(): created with SQL = " + sql);
    } // createWatchListTable()

    public void deleteWatchlist(long watchlistId) {
        final String methodName = "deleteWatchlist";
        Log.d(CLASS_NAME,
                String.format("%s(): watchlistId = %d", methodName, watchlistId));
        String[] whereArgs = new String[]{String.valueOf(watchlistId)};
        Integer deleteResult;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            // Delete any existing connections to securities
            deleteResult = db.delete(SecuritiesInWatchlists.TABLE,
                    SecuritiesInWatchlists.WATCHLIST_ID + " = ?", whereArgs);
            Log.d(CLASS_NAME, String.format(DELETE_RESULT_FORMAT, methodName,
                    SecuritiesInWatchlists.TABLE, deleteResult));
            // Delete watchlist
            deleteResult = db.delete(Watchlist.TABLE, Watchlist.ID + " = ?", whereArgs);
            Log.d(CLASS_NAME, String.format(DELETE_RESULT_FORMAT, methodName,
                    Watchlist.TABLE, deleteResult));
            db.setTransactionSuccessful();
            Log.d(CLASS_NAME, methodName + "(): success!");
        } finally {
            db.endTransaction();
        }
    } // deleteWatchlist()

    /**
     * Returns the minimum and maximum value of all columns contained in columnNames
     * from all records in cursor.
     * Minimum and maximum value are expressed as percentage of Quote.LAST_PRICE and cursor must
     * include this column as well as all columns in columnNames.
     * If columnNames includes Security.TRAILING_TARGET cursor needs to additionally include
     * Security.MAX_PRICE because the trailing target's current price is calculated from it.
     */
    private Extremes getExtremesForCursor(List<String> columnNames, Cursor cursor) {
        List<Extremes> extremes = new ArrayList<Extremes>();
        // Record extremes for each datarow
        while (cursor.moveToNext()) {
            Float lastPrice = cursor.getFloat(
                    cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE));
            Float maxPrice = lastPrice;
            Float minPrice = lastPrice;
            Float currentPrice;
            for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                if (columnNames.get(columnIndex) == Security.TRAILING_TARGET) {
                    currentPrice = Float.NaN;
                    Float trailingTarget
                            = Utils.readFloatRespectingNull(columnNames.get(columnIndex), cursor);
                    if (trailingTarget.isNaN() == false) {
                        Float max = Utils.readFloatRespectingNull(Security.MAX_PRICE, cursor);
                        if (max.isNaN() == false) {
                            currentPrice = max - max * trailingTarget / 100;
                        }
                    }
                } else {
                    currentPrice
                            = Utils.readFloatRespectingNull(columnNames.get(columnIndex), cursor);
                }
                if (currentPrice.isNaN() == false && currentPrice > maxPrice) {
                    maxPrice = currentPrice;
                } else if (currentPrice.isNaN() == false && currentPrice < minPrice) {
                    minPrice = currentPrice;
                }
            }
            extremes.add(new Extremes(lastPrice, maxPrice, minPrice));
        }
        // Get extremes from recorded extremes
        Float maxPercent = 100f;
        Float minPercent = 100f;
        for (int i = 0; i < extremes.size(); i++) {
            if (extremes.get(i).getMaxPercent() > maxPercent) {
                maxPercent = extremes.get(i).getMaxPercent();
            }
            if (extremes.get(i).getMinPercent() < minPercent) {
                minPercent = extremes.get(i).getMinPercent();
            }
        }
        return new Extremes(null, maxPercent, minPercent);
    } // getExtremesForCursor()

    private String getDataTimeStringFromStrings(String lastTradeDateString,
                                                String lastTradeTimeString) {
        String lastTradeDateTimeString = null;
        Date lastTradeDate = null; // d1
        Date lastTradeTime = null; // t1
        if (lastTradeDateString.compareTo("N/A") != 0) {
            // Step 1: calculate lastTradeDate
            // It seems timezone matches the app's timezone so no conversion needed
            // lastTradeDateString is formatted as US date, e.g. 5/26/2016
            SimpleDateFormat lastTradeDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            try {
                lastTradeDate = lastTradeDateFormat.parse(lastTradeDateString);
            } catch (ParseException e) {
                Log.e(CLASS_NAME, EXCEPTION_CAUGHT, e);
            }
            // Step 2: calculate lastTradeTime
            // SimpleDateFormat can't handle missing space between time and am / pm
            lastTradeTimeString = lastTradeTimeString.replace("am", " am").replace("pm", " pm");
            // lastTradeDate is in 12 hour format, e.g. 1:50pm
            // Need to specify Locale.US to avoid ParseException on am / pm part when that isn't
            // used in the default locale
            SimpleDateFormat lastTradeTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            try {
                lastTradeTime = lastTradeTimeFormat.parse(lastTradeTimeString);
            } catch (ParseException e) {
                Log.e(CLASS_NAME, EXCEPTION_CAUGHT, e);
            }
        }
        // Step 3: combine lastTradeDate and lastTradeTime
        if (lastTradeDate != null && lastTradeTime != null) {
            // not using Calendar class for performance reasons
            @SuppressWarnings("deprecation")
            Date lastTradeDateTime = new Date(lastTradeDate.getTime()
                    + lastTradeTime.getHours() * 60 * 60 * 1000
                    + lastTradeTime.getMinutes() * 60 * 1000);
            // Convert to international format
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
            lastTradeDateTimeString = dateTimeFormat.format(lastTradeDateTime);
        }
        return lastTradeDateTimeString;
    } // getDataTimeStringFromStrings()

    private Float getFloatFromPercentString(String s) {
        s = s.replace("%", "");
        return getFloatFromString(s);
    } // getFloatFromPercentString()

    private Float getFloatFromString(String s) {
        Float result = Float.NaN;
        try {
            result = Float.parseFloat(s);
        } catch (NumberFormatException x) {
            // Probably empty string or "n/a" - return Float.NaN;
            if ("N/A".equals(s) == false) {
                Log.e(CLASS_NAME, EXCEPTION_CAUGHT, x);
            }
        }
        return result;
    } // getFloatFromString()

    private Integer getIntegerFromString(String s) {
        Integer result = null;
        try {
            result = Integer.parseInt(s);
        } catch (NumberFormatException x) {
            // Probably empty string or "n/a" - return null;
            Log.e(CLASS_NAME, EXCEPTION_CAUGHT, x);
        }
        return result;
    } // getIntegerFromString()

    public Extremes getQuoteExtremesForWatchlist(long watchlistId) {
        final String methodName = "getQuoteExtremesForWatchlist";
        Extremes extremes = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT q." + Quote.ASK
                + ", q." + Quote.BID
                + ", q." + Quote.DAYS_HIGH
                + ", q." + Quote.DAYS_LOW
                + ", q." + Quote.LAST_PRICE
                + ", q." + Quote.OPEN
                + ", q." + Quote.PREVIOUS_CLOSE
                + "\nFROM " + Quote.TABLE + " q" + "\n\tINNER JOIN "
                + SecuritiesInWatchlists.TABLE + " siwl"
                + " ON siwl." + SecuritiesInWatchlists.SECURITY_ID
                + " = q." + Quote.SECURITY_ID
                + "\n\tINNER JOIN " + Watchlist.TABLE + " w"
                + " ON w." + Watchlist.ID + " = siwl." + SecuritiesInWatchlists.WATCHLIST_ID
                + "\nWHERE w." + Watchlist.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(watchlistId)};
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        Log.v(DbHelper.CLASS_NAME,
                String.format(DbHelper.CURSOR_COUNT_FORMAT, methodName,
                        cursor.getCount()));
        List<String> columnNames = new ArrayList<String>();
        Collections.addAll(columnNames, Quote.ASK, Quote.BID, Quote.DAYS_HIGH,
                Quote.DAYS_LOW, Quote.OPEN, Quote.PREVIOUS_CLOSE);
        extremes = getExtremesForCursor(columnNames, cursor);
        closeCursor(cursor);
        return extremes;
    } // getQuoteExtremesForWatchlist()

    private Long getQuoteIdFromSymbol(SQLiteDatabase db, String symbol) {
        Long result = NewItemId;
        String table = Quote.TABLE;
        String[] columns = new String[]{Quote.ID};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String selection = Quote.SYMBOL + " = ?";
        String[] selectionArgs = new String[]{symbol};
        Cursor cursor = db.query(
                table, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (cursor.moveToFirst()) {
            result = cursor.getLong(0);
        }
        cursor.close();
        return result;
    } // getQuoteIdFromSymbol()

    private Long getSecurityIdFromSymbol(SQLiteDatabase db, String symbol) {
        final String methodName = "getSecurityIdFromSymbol";
        Long securityId = NewItemId;
        String table = Security.TABLE;
        String[] columns = new String[]{Security.ID};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String selection = Security.SYMBOL + " = ?";
        String[] selectionArgs = new String[]{symbol};
        Cursor cursor = db.query(
                table, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (cursor.moveToFirst()) {
            securityId = cursor.getLong(0);
        } else {
            Log.e(CLASS_NAME, String.format(
                    "%s(): couldn't get securityId for symbol = %s", methodName, symbol));
        }
        cursor.close();
        return securityId;
    } // getSecurityIdFromSymbol()

    public Extremes getTargetExtremesForWatchlist(long watchlistId) {
        final String methodName = "getTargetExtremesForWatchlist";
        Extremes extremes = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT q." + Quote.LAST_PRICE
                + ", s." + Security.BASE_PRICE
                + ", s." + Security.LOWER_TARGET
                + ", s." + Security.MAX_PRICE
                + ", s." + Security.TRAILING_TARGET
                + ", s." + Security.UPPER_TARGET
                + "\nFROM " + Quote.TABLE + " q" + "\n\tINNER JOIN "
                + Security.TABLE + " s"
                + " ON s." + Security.ID + " = q." + Quote.SECURITY_ID
                + "\n\tINNER JOIN "
                + SecuritiesInWatchlists.TABLE + " siwl"
                + " ON siwl." + SecuritiesInWatchlists.SECURITY_ID
                + " = q." + Quote.SECURITY_ID
                + "\n\tINNER JOIN " + Watchlist.TABLE + " w"
                + " ON w." + Watchlist.ID + " = siwl." + SecuritiesInWatchlists.WATCHLIST_ID
                + "\nWHERE w." + Watchlist.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(watchlistId)};
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        Log.v(DbHelper.CLASS_NAME,
                String.format(DbHelper.CURSOR_COUNT_FORMAT, methodName,
                        cursor.getCount()));
        List<String> columnNames = new ArrayList<String>();
        Collections.addAll(columnNames, Quote.LAST_PRICE, Security.BASE_PRICE,
                Security.LOWER_TARGET, Security.TRAILING_TARGET, Security.UPPER_TARGET);
        extremes = getExtremesForCursor(columnNames, cursor);
        closeCursor(cursor);
        return extremes;
    } // getTargetExtremesForWatchlist()

    public long getWatchlistIdByPosition(int position) {
        final String methodName = "getWatchlistIdByPosition";
        int result = -1;
        Cursor cursor = readAllWatchlists();
        if (cursor.getCount() >= position) {
            cursor.moveToPosition(position);
            result = cursor.getInt(cursor.getColumnIndex(Watchlist.ID));
        } else {
            Log.w(DbHelper.CLASS_NAME, String.format(
                    "%s: cannot move to position = %d; cursor.getCount() = %d",
                    methodName, position, cursor.getCount()));
        }
        closeCursor(cursor);
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
                methodName + "(): "
                        + insertSelectionArgs(selection, selectionArgs));
    } // logSql()

    private void logSql(String methodName, String[] columns, String orderBy,
                        String selection, String[] selectionArgs, String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                sb.append(columns[i]);
                if (i < columns.length - 1) {
                    sb.append(", ");
                }
            }
        } else {
            sb.append("*");
        }
        sb.append("\nFROM ");
        sb.append(table);
        if (TextUtils.isEmpty(selection) == false) {
            sb.append("\nWHERE ");
            sb.append(insertSelectionArgs(selection, selectionArgs));
        }
        if (TextUtils.isEmpty(orderBy) == false) {
            sb.append("\nORDER BY ");
            sb.append(orderBy);
        }
        Log.v(CLASS_NAME, methodName + "(): " + sb.toString());
    } // logSql()

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createSampleData(db);
    } // onCreate()

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor readAllQuotesForWatchlist(long watchlistId) {
        final String methodName = "readAllQuotesForWatchlist";
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT q.*, s." + Security.BASE_PRICE + ", s."
                + Security.LOWER_TARGET + ", s." + Security.MAX_PRICE
                + ", s." + Security.TRAILING_TARGET + ", s."
                + Security.UPPER_TARGET + "\nFROM "
                + SecuritiesInWatchlists.TABLE + " siwl" + "\n\tINNER JOIN "
                + Quote.TABLE + " q ON q." + Quote.SECURITY_ID
                + " = " + "siwl." + SecuritiesInWatchlists.SECURITY_ID + "\n\tINNER JOIN "
                + Security.TABLE + " s ON s." + Security.ID + " = " + "q."
                + Quote.SECURITY_ID + "\nWHERE siwl."
                + SecuritiesInWatchlists.WATCHLIST_ID + " = ?" + "\nORDER BY q."
                + Quote.NAME + " ASC";
        String[] selectionArgs = new String[]{String.valueOf(watchlistId)};
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        Log.v(DbHelper.CLASS_NAME,
                String.format(DbHelper.CURSOR_COUNT_FORMAT, methodName,
                        cursor.getCount()));
        return cursor;
    } // readAllQuotesForWatchlist()

    /**
     * Gets a list of all securities with those in the specified watchlist marked,
     * ordered by symbol
     *
     * @param idOfWatchlistToMark If a stock is included in this watchlist,
     *                            is_in_watchlist_included will be 1, otherwise 0
     * @return A list (_id, is_included_in_watchlist, symbol) of all securities
     * with those in the specified watchlist marked, ordered by symbol
     */
    public Cursor getAllSecuritiesAndMarkIfInWatchlist(long idOfWatchlistToMark) {
        final String methodName = "getAllSecuritiesAndMarkIfInWatchlist";
        Cursor cursor = null;
        Log.v(CLASS_NAME, String.format("%s(): idOfWatchlistToMark = %d",
                methodName, idOfWatchlistToMark));
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT tmp._id AS "
                + Security.ID
                + ", tmp.symbol AS "
                + Security.SYMBOL
                + ", q.name AS "
                + Quote.NAME
                + ", MAX(tmp.isInWatchList) AS "
                + IS_SYMBOL_IN_WATCHLIST_ALIAS
                + "\nFROM ("
                + "\n\tSELECT " + Security.ID + ", " + Security.SYMBOL + ", 1 AS isInWatchList"
                + "\n\tFROM " + Security.TABLE + " s"
                + "\n\t\tLEFT JOIN " + SecuritiesInWatchlists.TABLE + " siwl ON "
                + SecuritiesInWatchlists.SECURITY_ID + " = " + Security.ID
                + "\n\tWHERE siwl." + SecuritiesInWatchlists.WATCHLIST_ID + " = ?"
                + "\n\tUNION ALL"
                + "\n\tSELECT " + Security.ID + ", " + Security.SYMBOL + ", 0 AS isInWatchList"
                + "\n\tFROM " + Security.TABLE + " s"
                + "\n) AS tmp"
                + "\n\tLEFT OUTER JOIN " + Quote.TABLE + " q ON q." + Quote.SECURITY_ID + " = tmp._id"
                + "\nGROUP BY tmp._id, tmp.symbol, " + Quote.NAME
                + "\nORDER BY tmp.symbol ASC";
        String[] selectionArgs = new String[]{String.valueOf(idOfWatchlistToMark)};
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        Log.v(CLASS_NAME, String.format(
                CURSOR_COUNT_FORMAT, methodName, cursor.getCount()));
        return cursor;
    } // getAllSecuritiesAndMarkIfInWatchlist()

    public Cursor readAllSecuritySymbols() {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{Security.SYMBOL};
        String groupBy = null;
        String having = null;
        String orderBy = Security.SYMBOL;
        String selection = null;
        String[] selectionArgs = null;
        String table = Security.TABLE;
        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    } // readAllSecuritySymbols()

    public Cursor readAllTriggeredSignals() {
        final String methodName = "readAllTriggeredSignals";
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        // sq.days_high, Quote.DAYS_LOW will be stored as NULL if Yahoo returned n/a
        // SQLite auto-converts Float.NaN to NULL
        String sql = "/* Lower target signal */ "
                + "\nSELECT 'low' AS actual_name, " + Quote.DAYS_LOW + " AS actual_value,"
                + " 'L' AS signal_name, " + Security.LOWER_TARGET + " AS target_value,"
                + " s." + Security.SYMBOL
                + "\nFROM " + Security.TABLE + " s"
                + "\n\tLEFT JOIN " + Quote.TABLE + " q"
                + " ON q." + Quote.SECURITY_ID + " = s." + Security.ID
                // Only evaluate target if quote isn't older than 1 day
                + "\nWHERE (" + Quote.LAST_PRICE_DATE_TIME + " >= date('now','-1 day'))"
                + "\n\tAND ("
                // Only evaluate target if LOWER_TARGET was specified and DAYS_LOW wasn't N/A
                + "\n\t\t" + Security.LOWER_TARGET + " IS NOT NULL"
                + "\n\t\tAND " + Quote.DAYS_LOW + " IS NOT NULL"
                + "\n\t\tAND " + Quote.DAYS_LOW + " <= " + Security.LOWER_TARGET
                + "\n\t) "
                + "\nUNION ALL "
                + "\n/* Upper target signal */ "
                + "\nSELECT 'high' AS actual_name, " + Quote.DAYS_HIGH + " AS actual_value,"
                + " 'U' AS signal_name, " + Security.UPPER_TARGET + " AS target_value,"
                + " s." + Security.SYMBOL
                + "\nFROM " + Security.TABLE + " s"
                + "\n\tLEFT JOIN " + Quote.TABLE + " q"
                + " ON q." + Quote.SECURITY_ID + " = s." + Security.ID
                // Only evaluate target if quote isn't older than 1 day
                + "\nWHERE (" + Quote.LAST_PRICE_DATE_TIME + " >= date('now','-1 day'))"
                + "\n\tAND ("
                // Only evaluate target if UPPER_TARGET was specified and DAYS_HIGH wasn't N/A
                + "\n\t\t" + Security.UPPER_TARGET + " IS NOT NULL"
                + "\n\t\tAND " + Quote.DAYS_HIGH + " IS NOT NULL"
                + "\n\t\tAND " + Quote.DAYS_HIGH + " >= " + Security.UPPER_TARGET
                + "\n\t) "
                + "\nUNION ALL "
                + "\n/* Trailing stop loss signal */"
                + "\nSELECT 'low' AS actual_name, " + Quote.DAYS_LOW + " AS actual_value,"
                + " 'T' AS signal_name, " + Security.MAX_PRICE
                + " * (100 - " + Security.TRAILING_TARGET + ") / 100 AS target_value,"
                + " s." + Security.SYMBOL
                + "\nFROM " + Security.TABLE + " s"
                + "\n\tLEFT JOIN " + Quote.TABLE + " q"
                + " ON q." + Quote.SECURITY_ID + " = s." + Security.ID
                // Only evaluate target if quote isn't older than 1 day
                + "\nWHERE (" + Quote.LAST_PRICE_DATE_TIME + " >= date('now','-1 day'))"
                + "\n\tAND ("
                // Only evaluate target if TRAILING_TARGET was specified and DAYS_LOW wasn't N/A
                + "\n\t\t" + Security.TRAILING_TARGET + " IS NOT NULL"
                + "\n\t\tAND " + Quote.DAYS_LOW + " IS NOT NULL"
                + "\n\t\tAND " + Quote.DAYS_LOW + " <= " + Security.MAX_PRICE + " * (100 - "
                + Security.TRAILING_TARGET + ") / 100"
                + "\n\t) "
                + "\nORDER BY s." + Security.SYMBOL + " ASC";
        String[] selectionArgs = new String[]{};
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        return cursor;
    } // readAllTriggeredSignals()

    public Cursor readAllWatchlists() {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{Watchlist.ID, Watchlist.NAME};
        String groupBy = null;
        String having = null;
        String orderBy = Watchlist.NAME;
        String selection = null;
        String[] selectionArgs = null;
        String table = Watchlist.TABLE;
        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    } // readAllWatchlists()

    public Cursor readWatchlist(long watchlistId) {
        final String methodName = "readWatchlist";
        Cursor cursor = null;
        Log.v(CLASS_NAME,
                String.format("%s(): watchlistId = %d", methodName, watchlistId));
        SQLiteDatabase db = getReadableDatabase();
        String selection = Watchlist.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(watchlistId)};
        String table = Watchlist.TABLE;
        logSql(methodName, null, null, selection, selectionArgs, table);
        cursor = db.query(table, null, selection, selectionArgs, null, null,
                null);
        Log.v(CLASS_NAME, String.format(CURSOR_COUNT_FORMAT, methodName, cursor.getCount()));
        if (cursor.getCount() != 1) {
            Log.e(CLASS_NAME, String.format(
                    "%s(): found %d watchlists with id = %d; expected 1!", methodName,
                    cursor.getCount(), watchlistId));
        }
        return cursor;
    } // readWatchlist()

    public void updateOrCreateQuotes(String quoteCsv) {
        final String methodName = "updateOrCreateQuotes";
        Log.v(CLASS_NAME, String.format("%s: quoteCsv = %s", methodName, quoteCsv));
        // region Example
        // Calling http://download.finance.yahoo.com/d/quotes.csv?s=BAYN.DE+NESN.VX+NOVN.VX+SIE.DE&f=aa2bc4d1ghl1nopp2st1vx
        // gets back a csv file with 4 lines like this:
        // 85.50,3118324,85.47,"EUR","5/27/2016",85.23,85.93,85.49,"BAYER N",85.65,85.65,"-0.19%","BAYN.DE","10:40am",799639,"GER"
        // 74.45,5109847,74.40,"CHF","5/27/2016",73.85,74.50,74.40,"NESTLE N",74.05,74.25,"+0.20%","NESN.VX","10:40am",1360058,"VTX"
        // 79.55,5053093,79.50,"CHF","5/27/2016",79.20,79.80,79.50,"NOVARTIS N",79.30,79.35,"+0.19%","NOVN.VX","10:40am",1263633,"VTX"
        // 97.80,2098357,97.79,"EUR","5/27/2016",97.25,97.94,97.80,"SIEMENS N",97.44,97.70,"+0.10%","SIE.DE","10:40am",308498,"GER"
        // Split lines and parse each according to QuoteDownloadFormatParameter
        // This will break if values include commas, see QuoteDownloadFormatParameter!
        // endregion Example
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            String quoteCsvRow = null;
            String[] quoteCsvRows = quoteCsv.split("\r?\n|\r");
            for (int i = 0; i < quoteCsvRows.length; i++) {
                quoteCsvRow = quoteCsvRows[i];
                String[] values = quoteCsvRow.split(",");
                // Delete any surrounding quotes
                for (int j = 0; j < values.length; j++) {
                    values[j] = values[j].replace("\"", "");
                }
                // Extract values (ordered by index of column in quoteCsv based on QuoteDownloadFormatParameter)
                Float ask = getFloatFromString(values[0]); // a
                Integer averageDailyVolume = getIntegerFromString(values[1]); // a2
                Float bid = getFloatFromString(values[2]); // b
                String currency = values[3]; // c4
                String lastTradeDateTime
                        = getDataTimeStringFromStrings(values[4], values[13]); // d1, t1
                Float daysLow = getFloatFromString(values[5]); // g
                Float daysHigh = getFloatFromString(values[6]); // h
                Float lastTrade = getFloatFromString(values[7]); // l1
                String name = values[8]; // n
                Float open = getFloatFromString(values[9]); // 0
                Float previousClose = getFloatFromString(values[10]); // p
                Float percentChange = getFloatFromPercentString(values[11]); // p2
                String symbol = values[12]; // s
                Integer volume = getIntegerFromString(values[14]); // v
                String stockExchangeName = values[15]; // x
                long securityId = getSecurityIdFromSymbol(db, symbol);
                // Store values (ordered alphabetically)
                ContentValues contentValues = new ContentValues();
                contentValues.put(Quote.ASK, ask);
                contentValues.put(Quote.AVERAGE_DAILY_VOLUME, averageDailyVolume);
                contentValues.put(Quote.BID, bid);
                contentValues.put(Quote.CURRENCY, currency);
                contentValues.put(Quote.DAYS_HIGH, daysHigh);
                contentValues.put(Quote.DAYS_LOW, daysLow);
                contentValues.put(Quote.LAST_PRICE, lastTrade);
                contentValues.put(Quote.LAST_PRICE_DATE_TIME, lastTradeDateTime);
                contentValues.put(Quote.NAME, name);
                contentValues.put(Quote.OPEN, open);
                contentValues.put(Quote.PERCENT_CHANGE, percentChange);
                contentValues.put(Quote.STOCK_EXCHANGE_NAME, stockExchangeName);
                contentValues.put(Quote.PREVIOUS_CLOSE, previousClose);
                contentValues.put(Quote.SECURITY_ID, securityId);
                contentValues.put(Quote.SYMBOL, symbol);
                contentValues.put(Quote.VOLUME, volume);
                // Just try an Update as this will only fail for a newly added security
                int updateResult = db.update(Quote.TABLE,
                        contentValues, Quote.SECURITY_ID + " = ?",
                        new String[]{String.valueOf(securityId)});
                Log.v(CLASS_NAME, String.format(UPDATE_RESULT_FORMAT,
                        methodName, Quote.TABLE, updateResult));
                if (updateResult == 0) {
                    Long insertResult = db.insert(Quote.TABLE,
                            null, contentValues);
                    Log.v(CLASS_NAME, String.format(INSERT_RESULT_FORMAT,
                            methodName, Quote.TABLE, insertResult));
                }
            }
            db.setTransactionSuccessful();
            Log.d(CLASS_NAME, methodName + "(): success!");
        } finally {
            db.endTransaction();
        }
    } // updateOrCreateQuotes()

    public void updateOrCreateWatchlist(String name, long[] securityIds,
                                        long watchlistId) {
        final String methodName = "updateOrCreateWatchlist";
        Long insertResult = null;
        String[] whereArgs = new String[]{String.valueOf(watchlistId)};
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            // Save watchlist data
            boolean isExistingWatchlist = (watchlistId != NEW_ITEM_ID);
            ContentValues contentValues = new ContentValues();
            contentValues.put(Watchlist.NAME, name);
            if (isExistingWatchlist) {
                Integer updateResult = db.update(Watchlist.TABLE,
                        contentValues, Watchlist.ID + " = ?", whereArgs);
                Log.v(CLASS_NAME, String.format(UPDATE_RESULT_FORMAT,
                        methodName, Watchlist.TABLE, updateResult));
            } else {
                insertResult = db.insert(Watchlist.TABLE, null, contentValues);
                Log.v(CLASS_NAME, String.format(INSERT_RESULT_FORMAT,
                        methodName, Watchlist.TABLE, insertResult));
                watchlistId = insertResult;
                Log.v(CLASS_NAME, String.format("%s(): new watchlistId = %d",
                        methodName, watchlistId));
            }
            // Delete existing connections to securities
            if (isExistingWatchlist) {
                Integer deleteResult = db.delete(
                        SecuritiesInWatchlists.TABLE,
                        SecuritiesInWatchlists.WATCHLIST_ID + " = ?", whereArgs);
                Log.v(CLASS_NAME, String.format(DELETE_RESULT_FORMAT,
                        methodName, SecuritiesInWatchlists.TABLE,
                        deleteResult));
            } else {
                Log.v(CLASS_NAME, String.format(
                        "%s(): New watchlist; skipping delete in %s",
                        methodName, SecuritiesInWatchlists.TABLE));
            }
            // Create specified connections to securities
            contentValues = new ContentValues();
            for (int i = 0; i < securityIds.length; i++) {
                contentValues.clear();
                contentValues.put(SecuritiesInWatchlists.SECURITY_ID, securityIds[i]);
                contentValues.put(SecuritiesInWatchlists.WATCHLIST_ID, watchlistId);
                Log.v(CLASS_NAME, String.format(INSERT_CONTENT_VALUES_FORMAT,
                        methodName, SecuritiesInWatchlists.TABLE,
                        contentValues));
                insertResult = db.insert(SecuritiesInWatchlists.TABLE, null, contentValues);
                Log.v(CLASS_NAME, String.format(INSERT_RESULT_FORMAT,
                        methodName, SecuritiesInWatchlists.TABLE, insertResult));
            }
            db.setTransactionSuccessful();
            Log.d(CLASS_NAME, methodName + "(): success!");
        } finally {
            db.endTransaction();
        }
    } // updateOrCreateWatchlist()

    public void updateSecurityMaxPrice() {
        final String methodName = "updateSecurityMaxPrice";
        // Update Security.MAX_PRICE with Quote.DAYS_HIGH if that's higher. Note that both can be
        // NULL. Also update Security.MAX_PRICE_DATE from Quote.LAST_PRICE_DATE_TIME in that case.
        // SQLite doesn't support JOINs in UPDATEs. So find securities to update
        // first, then issue individual UPDATEs.
        Cursor cursor = null;
        SQLiteDatabase db = this.getWritableDatabase();
        // Step #1: find securities to update
        // Caveats for DAYS_HIGH: time not available and volume may have been 0
        String sql = "SELECT s." + Security.ID + ", " + Security.MAX_PRICE
                + ", " + Security.MAX_PRICE_DATE + ", s." + Security.SYMBOL
                + ", " + Quote.DAYS_HIGH + ", SUBSTR(" + Quote.LAST_PRICE_DATE_TIME + ", 0, 11)"
                + "\nFROM " + Security.TABLE + " s"
                + "\n\tLEFT JOIN " + Quote.TABLE + " q ON q." + Quote.SECURITY_ID + " = s." + Security.ID
                + "\nWHERE COALESCE(" + Security.MAX_PRICE + ", 0) < COALESCE(" + Quote.DAYS_HIGH + ", 0)"
                + "\n\tAND COALESCE(" + Security.MAX_PRICE_DATE + ", '') < SUBSTR(" + Quote.LAST_PRICE_DATE_TIME + ", 0, 11)";
        String[] selectionArgs = new String[]{};
        logSql(methodName, sql, selectionArgs);
        cursor = db.rawQuery(sql, selectionArgs);
        Log.v(CLASS_NAME, String.format(CURSOR_COUNT_FORMAT, methodName, cursor.getCount()));
        // Step #2: issue individual UPDATEs
        try {
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            while (cursor.moveToNext()) {
                Long securityId = cursor.getLong(0);
                Float maxPrice = cursor.getFloat(1);
                String maxPriceDate = cursor.getString(2);
                String symbol = cursor.getString(3);
                Float daysHigh = cursor.getFloat(4);
                String lastTradeDate = cursor.getString(5);
                Log.v(CLASS_NAME, String.format(
                        "%s(): symbol=%s; maxPrice=%f; maxPriceDate=%s; daysHigh=%f; lastTradeDate=%s;",
                        methodName, symbol, maxPrice, maxPriceDate, daysHigh, lastTradeDate));
                contentValues.clear();
                contentValues.put(Security.MAX_PRICE, daysHigh);
                contentValues.put(Security.MAX_PRICE_DATE, lastTradeDate);
                String[] whereArgs = new String[]{String.valueOf(securityId)};
                Integer updateResult = db.update(Security.TABLE,
                        contentValues, Security.ID + " = ?", whereArgs);
                Log.v(CLASS_NAME, String.format(UPDATE_RESULT_FORMAT,
                        methodName, Security.TABLE, updateResult));
            }
            db.setTransactionSuccessful();
            Log.d(CLASS_NAME, methodName + "(): success!");
        } finally {
            db.endTransaction();
            closeCursor(cursor);
        }
    } // updateSecurityMaxPrice()

    /**
     * Extremes holds information about the extremes of a quote or the targets of a security.
     * If lastPrice is null then maxValue and minValue are stored as the extremes.
     * If lastPrice is not null the extremes are calculated as percent of last price.
     * Example:
     * - last price = 100
     * - maximum value = 110
     * - minimum value = 90
     * -> maxPercent = 110 and minPercent = 90
     */
    public final class Extremes {
        private final Float maxPercent;
        private final Float minPercent;

        public Extremes(Float lastPrice, Float maxValue, Float minValue) {
            if (lastPrice == null) {
                this.maxPercent = maxValue;
                this.minPercent = minValue;
            } else {
                this.maxPercent = maxValue * 100 / lastPrice;
                this.minPercent = minValue * 100 / lastPrice;
            }
        }

        public Float getMaxPercent() {
            return maxPercent;
        }

        public Float getMinPercent() {
            return minPercent;
        }
    } // class Extremes
}
