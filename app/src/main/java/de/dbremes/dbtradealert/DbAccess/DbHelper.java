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
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String CLASS_NAME = "DbHelper";
    private static final String DB_NAME = "dbtradealert.db";
    private static final int DB_VERSION = 1;
    // strings for logging
    private final static String CURSOR_COUNT_FORMAT = "%s: cursor.getCount() = %d";

    public DbHelper(Context context) {
        super(context, DbHelper.DB_NAME, null, DbHelper.DB_VERSION);
    } // ctor()

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
            Log.d(DbHelper.CLASS_NAME, methodName + ": success!");
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
                "createSecuritiesInWatchListsTable created with SQL = "
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

    public Extremes getQuoteExtremesForWatchlist(long watchlistId) {
        final String methodName = "getQuoteExtremesForWatchlist";
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
        return getExtremesForCursor(columnNames, cursor);
    } // getQuoteExtremesForWatchlist()

    public Extremes getTargetExtremesForWatchlist(long watchlistId) {
        final String methodName = "getTargetExtremesForWatchlist";
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
        return getExtremesForCursor(columnNames, cursor);
    } // getTargetExtremesForWatchlist()

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

