package de.dbremes.dbtradealert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WatchlistsManagementActivity extends AppCompatActivity {

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlists_management);
    } // ctor()

    public void onOkButtonClick(View view) {
        setResult(RESULT_OK, getIntent());
        finish();
    } // onOkButtonClick()

} // class WatchlistsManagementActivity()
