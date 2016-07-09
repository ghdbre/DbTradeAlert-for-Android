package de.dbremes.dbtradealert;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class SecuritiesManagementActivity extends AppCompatActivity {
    private Cursor cursor;
    private DbHelper dbHelper;
    private SecuritiesManagementCursorAdapter securitiesManagementCursorAdapter;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // securitiesListView shows only symbols and these are immutable
        // so don't re-query after updating a security
        if (requestCode == SecurityEditActivity.CREATE_SECURITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            refreshSecuritiesListView();
        }
    } // onActivityResult()

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securities_management);
        setTitle("Manage Securities");
        this.dbHelper = new DbHelper(this);
        this.cursor = dbHelper.getAllSecuritiesAndMarkIfInWatchlist(DbHelper.NEW_ITEM_ID);
        this.securitiesManagementCursorAdapter
                = new SecuritiesManagementCursorAdapter(this, this.cursor, false);
        ListView securitiesListView = (ListView) findViewById(R.id.securitiesListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        securitiesListView.setEmptyView(emptyTextView);
        securitiesListView.setAdapter(securitiesManagementCursorAdapter);
    } // onCreate()

    public void onNewButtonClick(View view) {
        Intent intent = new Intent(this, SecurityEditActivity.class);
        intent.putExtra(SecurityEditActivity.SECURITY_ID_INTENT_EXTRA, DbHelper.NEW_ITEM_ID);
        startActivityForResult(intent, SecurityEditActivity.CREATE_SECURITY_REQUEST_CODE);
    } // onNewButtonClick()

    public void onOkButtonClick(View view) {
        setResult(RESULT_OK, getIntent());
        finish();
    } // onOkButtonClick()

    private void refreshSecuritiesListView() {
        Cursor cursor = dbHelper.getAllSecuritiesAndMarkIfInWatchlist(DbHelper.NEW_ITEM_ID);
        securitiesManagementCursorAdapter.changeCursor(cursor);
    } // refreshSecuritiesListView()

} // class SecuritiesManagementActivity
