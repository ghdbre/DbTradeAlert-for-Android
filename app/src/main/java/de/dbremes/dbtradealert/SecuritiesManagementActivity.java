package de.dbremes.dbtradealert;

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

    public void onOkButtonClick(View view) {
        setResult(RESULT_OK, getIntent());
        finish();
    } // onOkButtonClick()

} // class SecuritiesManagementActivity
