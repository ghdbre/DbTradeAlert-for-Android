package de.dbremes.dbtradealert;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {
    private static final String CLASS_NAME = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if (BuildConfig.FLAVOR.equals("naked")) {
            CheckBoxPreference trackingPreference
                    = (CheckBoxPreference) findPreference("tracking_preference");
            if (trackingPreference != null) {
                PreferenceScreen ps = getPreferenceScreen();
                ps.removePreference(trackingPreference);
                Log.d(CLASS_NAME, "onCreate(): tracking_preference removed");
            } else {
                Log.v(CLASS_NAME, "onCreate(): tracking_preference already removed");
            }
        }
    } // onCreate()
} // class SettingsFragment
