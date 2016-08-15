package de.dbremes.dbtradealert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String CLASS_NAME = "SettingsActivity";
    private static final String SETTINGS_FRAGMENT_TAG = "SettingsFragmentTag";
    private static final String BUSINESS_DAYS_PREFERENCE_KEY = "business_days_preference";
    private static final String BUSINESS_HOURS_PREFERENCE_KEY = "business_hours_preference";
    public static final String TRACKING_PREFERENCE_KEY = "tracking_preference";

    private void setBusinessTimesPreferenceSummary(String businessTimesPreferenceKey) {
        SettingsFragment settingsFragment
                = (SettingsFragment) getFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        MultiSelectListPreference businessTimesPreference
                = (MultiSelectListPreference) settingsFragment
                .findPreference(businessTimesPreferenceKey);
        Set businessDays = businessTimesPreference.getValues();
        Utils.BusinessTimesPreferenceExtremes
                btpe = Utils.getBusinessTimesPreferenceExtremes(businessDays);
        if (businessTimesPreferenceKey.equals(BUSINESS_DAYS_PREFERENCE_KEY)) {
            String[] shortDayNames = DateFormatSymbols.getInstance(Locale.US).getShortWeekdays();
            businessTimesPreference.setSummary(
                    String.format("Days on which auto refresh is active (%s - %s)",
                            shortDayNames[btpe.getFirstBusinessTime()],
                            shortDayNames[btpe.getLastBusinessTime()]));
        } else {
            businessTimesPreference.setSummary(
                    String.format("Hours on which auto refresh is active (%02d - %02d)",
                            btpe.getFirstBusinessTime(), btpe.getLastBusinessTime()));
        }
    } // setBusinessTimesPreferenceSummary()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(), SETTINGS_FRAGMENT_TAG)
                .commit();
        // Without this findFragmentByTag() would return null!
        getFragmentManager().executePendingTransactions();
        setBusinessTimesPreferenceSummary(BUSINESS_DAYS_PREFERENCE_KEY);
        setBusinessTimesPreferenceSummary(BUSINESS_HOURS_PREFERENCE_KEY);
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
        final String METHOD_NAME = "onSharedPreferenceChanged";
        Log.d(CLASS_NAME, METHOD_NAME + "(): key = " + key);
        switch (key) {
            case "auto_refresh_preference": {
                Intent intent = new Intent(this, QuoteRefreshScheduler.class);
                sendBroadcast(intent);
                break;
            }
            case BUSINESS_DAYS_PREFERENCE_KEY:
            case BUSINESS_HOURS_PREFERENCE_KEY:
                setBusinessTimesPreferenceSummary(key);
                break;
            case "tracking_preference": {
                boolean isEnabled = sharedPreferences.getBoolean(key, true);
                Context context = getApplicationContext();
                PlayStoreHelper.setBooleanUserProperty(
                        context, PlayStoreHelper.IS_TRACKING_ENABLED_USERPROPERTY, isEnabled);
                break;
            }
            default:
                Log.e(CLASS_NAME, METHOD_NAME + "(): Unexpected key = " + key);
                break;
        }
    } // onSharedPreferenceChanged()

} // class SettingsActivity
