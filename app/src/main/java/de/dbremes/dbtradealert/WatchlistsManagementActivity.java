package de.dbremes.dbtradealert;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class WatchlistsManagementActivity extends AppCompatActivity {
    private Cursor cursor;
    private DbHelper dbHelper;
    private WatchlistManagementCursorAdapter watchlistManagementCursorAdapter;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshWatchlistsListView();
        }
    } // onActivityResult()

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlists_management);
        setTitle("Manage Watchlists");
        dbHelper = new DbHelper(this);
        this.cursor = dbHelper.readAllWatchlists();
        this.watchlistManagementCursorAdapter
                = new WatchlistManagementCursorAdapter(this, this.cursor, false);
        ListView watchListsListView = (ListView) findViewById(R.id.watchlistsListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        watchListsListView.setEmptyView(emptyTextView);
        watchListsListView.setAdapter(watchlistManagementCursorAdapter);
    } // onCreate()

    public void onOkButtonClick(View view) {
        setResult(RESULT_OK, getIntent());
        finish();
    } // onOkButtonClick()

    public void onNewButtonClick(View view) {
        Intent intent = new Intent(this, WatchlistEditActivity.class);
        intent.putExtra(WatchlistEditActivity.WATCHLIST_ID_INTENT_EXTRA,
                DbHelper.NEW_ITEM_ID);
        startActivityForResult(intent,
                WatchlistEditActivity.CREATE_WATCHLIST_REQUEST_CODE);
    } // onNewButtonClick()

    public void refreshWatchlistsListView() {
        // public so it can be called from WatchlistManagementCursorAdapter
        Cursor cursor = this.dbHelper.readAllWatchlists();
        this.watchlistManagementCursorAdapter.changeCursor(cursor);
    } // refreshWatchlistsListView()

} // class WatchlistsManagementActivity()
