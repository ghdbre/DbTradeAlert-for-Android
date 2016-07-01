package de.dbremes.dbtradealert;

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
    private WatchListManagementCursorAdapter watchListManagementCursorAdapter;

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlists_management);
        dbHelper = new DbHelper(this);
        this.cursor = dbHelper.readAllWatchlists();
        this.watchListManagementCursorAdapter
                = new WatchListManagementCursorAdapter(this, this.cursor, false);
        ListView watchListsListView = (ListView) findViewById(R.id.watchListsListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        watchListsListView.setEmptyView(emptyTextView);
        watchListsListView.setAdapter(watchListManagementCursorAdapter);
    } // ctor()

    public void onOkButtonClick(View view) {
        setResult(RESULT_OK, getIntent());
        finish();
    } // onOkButtonClick()

} // class WatchlistsManagementActivity()
