package de.dbremes.dbtradealert;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class SecurityEditActivity extends AppCompatActivity {
    public final static int CREATE_SECURITY_REQUEST_CODE = 10;
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
//                // Update mode
//                showStockData(_stockId);
//                EditText symbolEditText = (EditText) findViewById(R.id.symbolEditText);
//                symbolEditText.setEnabled(false);
            }
        }
        refreshWatchlistsList(this.securityId);
    } // onCreate()

    private void refreshWatchlistsList(long securityId) {
        String[] fromColumns = {WatchlistContract.Watchlist.NAME};
        int[] toViews = {android.R.id.text1};
        Cursor watchlistsCursor
                = this.dbHelper.getAllWatchlistsAndMarkIfSecurityIsIncluded(securityId);
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
} // class SecurityEditActivity
