package de.dbremes.dbtradealert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        int resId = getApplicationContext().getApplicationInfo().labelRes;
        String appName = getApplicationContext().getString(resId);
        String applicationId = BuildConfig.APPLICATION_ID;
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        String s = String.format(
                "appName = %s\napplicationId = %s\nversionCode = %s\nversionName = %s",
                appName, applicationId, versionCode, versionName);
        TextView editTextView = (TextView) findViewById(R.id.editTextView);
        editTextView.setText(s);
        setTitle("About");

        //Log.v("AboutActivity", "onCreate(): Logging errors for Firebase Remote Config test");
        //PlayStoreHelper.logConnectionError("AboutActivity", "No worries, just a test");
        //PlayStoreHelper.logParsingError("AboutActivity", new ParseException("ParseException-test", 0));
    } // onCreate()

    public void onOkButtonClick(View view) {
        finish();
    } // onOkButtonClick()
} // class AboutActivity
