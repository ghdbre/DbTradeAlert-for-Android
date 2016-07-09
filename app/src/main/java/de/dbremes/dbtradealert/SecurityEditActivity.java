package de.dbremes.dbtradealert;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.SecurityContract;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class SecurityEditActivity extends AppCompatActivity {
    private final static String CLASS_NAME = "SecurityEditActivity";
    public final static int CREATE_SECURITY_REQUEST_CODE = 20;
    public final static int UPDATE_SECURITY_REQUEST_CODE = 21;
    public final static String SECURITY_ID_INTENT_EXTRA = "de.dbremes.dbtradealert.securityId";
    private DbHelper dbHelper;
    private long securityId = DbHelper.NewItemId;

    private void clearEditTextViews() {
        EditText editText = null;
        editText = (EditText) findViewById(R.id.basePriceEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.basePriceDateEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.lowerTargetEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.maxPriceEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.maxPriceDateEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.notesEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.symbolEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.trailingTargetEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.upperTargetEditText);
        editText.setText("");
    } // clearEditTextViews()

    private Date getDateFromEditText(Integer editTextId) {
        Date result = null;
        EditText editText = (EditText) findViewById(editTextId);
        if (editText.length() > 0) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
            String text = editText.getText().toString();
            try {
                result = dateFormat.parse(text);
            } catch (ParseException e) {
                Log.e(CLASS_NAME, Utils.EXCEPTION_CAUGHT, e);
                Toast.makeText(
                        this, "Error: '" + text + "' is not a valid date", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        return result;
    } // getDateFromEditText()

    private String getDateTimeStringFromDbDateTime(
            Cursor cursor, int columnIndex, boolean includeTime) {
        // Return something so missing time stamps can be marked
        String result = "";
        String dateTimeString = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(dateTimeString) == false) {
            SimpleDateFormat databaseFormat;
            SimpleDateFormat localFormat;
            if (includeTime) {
                databaseFormat = new SimpleDateFormat(
                        DbHelper.DATE_TIME_FORMAT_STRING, Locale.getDefault());
                localFormat = (SimpleDateFormat) SimpleDateFormat
                        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            } else {
                databaseFormat = new SimpleDateFormat(
                        DbHelper.DATE_FORMAT_STRING, Locale.getDefault());
                localFormat = (SimpleDateFormat) SimpleDateFormat
                        .getDateInstance(DateFormat.SHORT);
            }
            try {
                Date dateTime = databaseFormat.parse(dateTimeString);
                result = localFormat.format(dateTime);
            } catch (ParseException e) {
                Log.e(CLASS_NAME, Utils.EXCEPTION_CAUGHT, e);
            }
        }
        return result;
    } // getDateTimeStringFromDbDateTime()

    private Float getFloatFromEditText(Integer editTextId) {
        Float result = Float.NaN;
        EditText editText = (EditText) findViewById(editTextId);
        if (editText.length() > 0) {
            String text = editText.getText().toString();
            try {
                result = Float.valueOf(text);
            } catch (NumberFormatException e) {
                Log.e(CLASS_NAME, Utils.EXCEPTION_CAUGHT, e);
                Toast.makeText(
                        this, "Error: '" + text + "' is not a valid number", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        return result;
    } // getFloatFromEditText()

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_edit);
        this.dbHelper = new DbHelper(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.securityId = extras.getLong(SECURITY_ID_INTENT_EXTRA);
            if (this.securityId == DbHelper.NEW_ITEM_ID) {
                // Create mode
                setTitle("Add Security");
                clearEditTextViews();
            } else {
                // Update mode
                setTitle("Edit Security");
                showSecurityData(this.securityId);
                EditText symbolEditText = (EditText) findViewById(R.id.symbolEditText);
                symbolEditText.setEnabled(false);
            }
        }
        refreshWatchlistsList(this.securityId);
    } // onCreate()

    public void onOkButtonClick(View view) {
        Float basePrice = getFloatFromEditText(R.id.basePriceEditText);
        Date basePriceDate = getDateFromEditText(R.id.basePriceDateEditText);
        Float lowerTarget = getFloatFromEditText(R.id.lowerTargetEditText);
        Float maxPrice = getFloatFromEditText(R.id.maxPriceEditText);
        Date maxPriceDate = getDateFromEditText(R.id.maxPriceDateEditText);
        String notes = Utils.getStringFromEditText(this, R.id.notesEditText);
        String symbol = Utils.getStringFromEditText(this, R.id.symbolEditText);
        Float upperTarget = getFloatFromEditText(R.id.upperTargetEditText);
        Float trailingTarget = getFloatFromEditText(R.id.trailingTargetEditText);
        long[] watchlistIds = Utils.getSelectedListViewItemIds(this, R.id.watchlistsListView);
        String errorMessage = this.dbHelper.updateOrCreateSecurity(basePrice, basePriceDate,
                lowerTarget, maxPrice, maxPriceDate, notes, this.securityId,
                symbol, trailingTarget, upperTarget, watchlistIds);
        if (TextUtils.isEmpty(errorMessage)) {
            setResult(RESULT_OK, getIntent());
            finish();
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    } // onOkButtonClick()

    private void refreshWatchlistsList(long securityId) {
        String[] fromColumns = {WatchlistContract.Watchlist.NAME};
        int[] toViews = {android.R.id.text1};
        Cursor watchlistsCursor
                = this.dbHelper.readAllWatchlistsAndMarkIfSecurityIsIncluded(securityId);
        int flags = 0;
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_multiple_choice,
                watchlistsCursor, fromColumns, toViews, flags);
        ListView watchlistsListView = (ListView) findViewById(R.id.watchlistsListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        watchlistsListView.setEmptyView(emptyTextView);
        watchlistsListView.setAdapter(adapter);
        // Mark watchlists that include this security
        int isSecurityIncludedColumnIndex = watchlistsCursor
                .getColumnIndex(DbHelper.IS_SECURITY_IN_WATCHLIST_ALIAS);
        for (int i = 0; i < watchlistsCursor.getCount(); i++) {
            watchlistsCursor.moveToPosition(i);
            if (watchlistsCursor.getInt(isSecurityIncludedColumnIndex) == 1) {
                watchlistsListView.setItemChecked(i, true);
            }
        }
    } // refreshWatchlistsList()

    private void setTextFromDateColumn(Cursor cursor, String columnName, Integer editTextId) {
        EditText editText = (EditText) findViewById(editTextId);
        int columnIndex = cursor.getColumnIndex(columnName);
        String s = getDateTimeStringFromDbDateTime(cursor, columnIndex, false);
        editText.setText(s);
    } // setTextFromDateColumn()

    private void setTextFromFloatColumn(Cursor cursor, String columnName, Integer editTextId) {
        EditText editText = (EditText) findViewById(editTextId);
        Float value = Utils.readFloatRespectingNull(columnName, cursor);
        String valueString = "";
        if (value.isNaN() == false) {
            valueString = String.valueOf(value);
        }
        editText.setText(valueString);
    } // setTextFromFloatColumn()

    private void setTextFromStringColumn(Cursor cursor, String columnName, Integer editTextId) {
        EditText editText = (EditText) findViewById(editTextId);
        String value = cursor.getString(cursor.getColumnIndex(columnName));
        editText.setText(value);
    } // setTextFromStringColumn()

    private void showSecurityData(long securityId) {
        final String methodName = "showSecurityData";
        Cursor securityCursor = this.dbHelper.readSecurity(securityId);
        if (securityCursor.getCount() == 1) {
            securityCursor.moveToFirst();
            setTextFromFloatColumn(securityCursor,
                    SecurityContract.Security.BASE_PRICE, R.id.basePriceEditText);
            setTextFromDateColumn(securityCursor,
                    SecurityContract.Security.BASE_PRICE_DATE, R.id.basePriceDateEditText);
            setTextFromFloatColumn(securityCursor,
                    SecurityContract.Security.LOWER_TARGET, R.id.lowerTargetEditText);
            setTextFromFloatColumn(securityCursor,
                    SecurityContract.Security.MAX_PRICE, R.id.maxPriceEditText);
            setTextFromDateColumn(securityCursor,
                    SecurityContract.Security.MAX_PRICE_DATE, R.id.maxPriceDateEditText);
            setTextFromStringColumn(securityCursor,
                    SecurityContract.Security.NOTES, R.id.notesEditText);
            setTextFromStringColumn(securityCursor,
                    SecurityContract.Security.SYMBOL, R.id.symbolEditText);
            setTextFromFloatColumn(securityCursor,
                    SecurityContract.Security.TRAILING_TARGET, R.id.trailingTargetEditText);
            setTextFromFloatColumn(securityCursor,
                    SecurityContract.Security.UPPER_TARGET, R.id.upperTargetEditText);
        } else {
            Log.e(CLASS_NAME,
                    String.format(
                            "%s: readSecurity() found %d securities with id = %d; expected 1!",
                            methodName, securityCursor.getCount(), securityId));
        }
    } // showSecurityData()
} // class SecurityEditActivity
