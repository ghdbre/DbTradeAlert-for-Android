package de.dbremes.dbtradealert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String CLASS_NAME = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    } // onCreate()

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    } // onPause()

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    } // onResume()

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(CLASS_NAME, "onSharedPreferenceChanged: key = " + key);
        if (key.equals("auto_refresh_preference")) {
            Intent intent = new Intent(this, QuoteRefreshScheduler.class);
            sendBroadcast(intent);
        }
    } // onSharedPreferenceChanged()

} // class SettingsActivity
