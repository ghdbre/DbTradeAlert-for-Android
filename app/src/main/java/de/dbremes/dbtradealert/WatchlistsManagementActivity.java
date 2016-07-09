package de.dbremes.dbtradealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class WatchlistsManagementActivity extends AppCompatActivity {
    private Cursor cursor;
    private DbHelper dbHelper;
    private WatchlistsManagementCursorAdapter watchlistsManagementCursorAdapter;

    private BroadcastReceiver watchlistDeletedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    WatchlistsManagementCursorAdapter.WATCHLIST_DELETED_BROADCAST)) {
                refreshWatchlistsListView();
            }
        }
    }; // watchlistDeletedBroadcastReceiver

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
        // Connect Cursor to CursorAdapter and ListView to CursorAdapter
        this.dbHelper = new DbHelper(this);
        this.cursor = dbHelper.readAllWatchlists();
        this.watchlistsManagementCursorAdapter
                = new WatchlistsManagementCursorAdapter(this, this.cursor, false);
        ListView watchlistsListView = (ListView) findViewById(R.id.watchlistsListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        watchlistsListView.setEmptyView(emptyTextView);
        watchlistsListView.setAdapter(watchlistsManagementCursorAdapter);
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

    @Override
    public void onPause() {
        super.onPause();
        // Unregister broadcast receiver for WATCHLIST_DELETED_BROADCAST
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(watchlistDeletedBroadcastReceiver);
    } // onPause()

    @Override
    public void onResume() {
        super.onResume();
        // Register broadcast receiver for WATCHLIST_DELETED_BROADCAST
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WatchlistsManagementCursorAdapter.WATCHLIST_DELETED_BROADCAST);
        broadcastManager.registerReceiver(watchlistDeletedBroadcastReceiver, intentFilter);
    } // onResume()

    private void refreshWatchlistsListView() {
        Cursor cursor = this.dbHelper.readAllWatchlists();
        this.watchlistsManagementCursorAdapter.changeCursor(cursor);
    } // refreshWatchlistsListView()

} // class WatchlistsManagementActivity()
