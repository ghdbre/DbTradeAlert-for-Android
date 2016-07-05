package de.dbremes.dbtradealert;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.SecurityContract;

public class WatchlistEditActivity extends AppCompatActivity {
    private final static String CLASS_NAME = "WatchlistEditActivity";
    public final static int CREATE_WATCHLIST_REQUEST_CODE = 0;
    public final static String WATCHLIST_ID_INTENT_EXTRA = "de.dbremes.dbtradealert.watchlistId";
    private DbHelper dbHelper;
    private long watchlistId = DbHelper.NewItemId;

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String methodName = "onCreate";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist_edit);
        this.dbHelper = new DbHelper(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.watchlistId = extras.getLong(WATCHLIST_ID_INTENT_EXTRA);
            EditText nameEditText = (EditText) findViewById(R.id.nameEditText);
            if (this.watchlistId == DbHelper.NewItemId) {
                // Create mode
                nameEditText.setText("");
                setTitle("Create Watchlist");
            } else {
                // Update mode
//                Cursor watchlistCursor = this.dbHelper.readWatchlist(this.watchlistId);
//                if (watchlistCursor.getCount() == 1) {
//                    watchlistCursor.moveToFirst();
//                    nameEditText
//                            .setText(watchlistCursor.getString(watchlistCursor
//                                    .getColumnIndex(WatchlistContract.Watchlist.NAME)));
//                } else {
//                    Log.e(CLASS_NAME, String.format(
//                            "%s(): readWatchlist() found %d watchlists with id = %d; expected 1!",
//                            methodName, watchlistCursor.getCount(),
//                            this.watchlistId));
//                }
            }
        }
        showStockList(this.watchlistId);
    } // onCreate()

    private void showStockList(long watchListId) {
        final String methodName = "showStockList";
        SimpleCursorAdapter adapter;
        // Connect securities list to cursor
        // TODO: add securities name?
        String[] fromColumns = { SecurityContract.Security.SYMBOL };
        int[] toViews = { android.R.id.text1 };
        Cursor securitiesCursor = this.dbHelper.getAllSecuritiesAndMarkIfInWatchlist(watchListId);
        int flags =0;
        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_multiple_choice,
                securitiesCursor, fromColumns, toViews, flags);
        ListView securitiesToIncludeListView
                = (ListView) findViewById(R.id.securitiesToIncludeListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        securitiesToIncludeListView.setEmptyView(emptyTextView);
        securitiesToIncludeListView.setAdapter(adapter);
        // Mark securities that are included in this watchlist
        int isInWatchListIncludedPosition = securitiesCursor
                .getColumnIndex(DbHelper.IS_SYMBOL_IN_WATCHLIST_ALIAS);
        for (int i = 0; i < securitiesCursor.getCount(); i++) {
            securitiesCursor.moveToPosition(i);
            Log.v(CLASS_NAME, String.format("%s(): stocksCursor[%d].%s = %d",
                    methodName, i, DbHelper.IS_SYMBOL_IN_WATCHLIST_ALIAS,
                    securitiesCursor.getInt(isInWatchListIncludedPosition)));
            if (securitiesCursor.getInt(isInWatchListIncludedPosition) == 1) {
                securitiesToIncludeListView.setItemChecked(i, true);
            }
        }
    } // showStockList()
} // class WatchlistEditActivity
