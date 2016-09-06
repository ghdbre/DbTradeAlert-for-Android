package de.dbremes.dbtradealert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayStoreHelper {
    private static final String CLASS_NAME = "PlayStoreHelper";
    public final static String IS_TRACKING_ENABLED_USERPROPERTY = "isTrackingEnabled";
    public final static String REMINDER_COUNT_USERPROPERTY = "reminderCount";
    public final static String SECURITY_COUNT_USERPROPERTY = "securityCount";
    public final static String WATCHLIST_COUNT_USERPROPERTY = "watchlistCount";

    private PlayStoreHelper() {
        // Hide default construcor
    }

    private static void fetchRemoteConfigValues(FirebaseRemoteConfig firebaseRemoteConfig) {
        // Parameterless fetch() uses this default value for cacheExpirationSeconds:
        long cacheExpirationSeconds = 43200; // 12 hours

        // Expire the cache immediately if in development mode
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpirationSeconds = 0;
        }

        firebaseRemoteConfig.fetch(cacheExpirationSeconds)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseRemoteConfig.getInstance().activateFetched();
                            Log.v(CLASS_NAME,
                                    "fetchRemoteConfigValues(): Firebase Remote Config values have been fetched");
                        } else {
                            PlayStoreHelper.logError(task.getException());
                        }
                    }
                });
    } // fetchRemoteConfigValues()

    public static void initialize(boolean isDeveloperModeEnabled) {
        // Create a FirebaseRemoteConfig instance and initialize it with local default values
        // Note that the initial getInstance() call on app creation reads from a local file.
        // To avoid StrictMode disk read errors, this initial call should not be made on the
        // UI thread.
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        FirebaseRemoteConfigSettings frcs = new FirebaseRemoteConfigSettings.Builder()
                // setDeveloperModeEnabled() only controls minimum cacheExpirationSeconds?
                .setDeveloperModeEnabled(isDeveloperModeEnabled)
                .build();
        firebaseRemoteConfig.setConfigSettings(frcs);
        // Only now remote values are fetched - if cached values have expired
        fetchRemoteConfigValues(firebaseRemoteConfig);
    } // initialize()

    public static void logConnectionError(String tag, String message) {
        boolean isConnectionErrorLoggingEnabled = FirebaseRemoteConfig.getInstance()
                .getBoolean("is_connection_error_logging_enabled");
        if (isConnectionErrorLoggingEnabled) {
            logError(tag, message);
        } else {
            Log.e(tag, message);
        }
//        logFirebaseRemoteConfig();
    } // logConnectionError()

    public static void logDebugMessage(String tag, String message) {
        FirebaseCrash.log(message);
        FirebaseCrash.logcat(Log.DEBUG, tag, message);
    } // logDebugMessage()

    public static void logDebugMessage(Exception e) {
        String message = "";
        String stackTrace = "";
        if (e != null) {
            message = e.getMessage();
            stackTrace = Log.getStackTraceString(e);
            FirebaseCrash.logcat(Log.DEBUG, message, stackTrace);
        }
    } // logDebugMessage()

    public static void logError(String tag, String message) {
        FirebaseCrash.log(message);
        FirebaseCrash.logcat(Log.ERROR, tag, message);
    } // logError()

    public static void logError(Exception e) {
        FirebaseCrash.report(e);
    } // logError()

    private static void logFirebaseRemoteConfig() {
        final String methodName = "logFirebaseRemoteConfig";
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigInfo remoteConfigInfo = remoteConfig.getInfo();
        FirebaseRemoteConfigSettings frcs = remoteConfigInfo.getConfigSettings();
        // isDeveloperModeEnabled
        boolean isDeveloperModeEnabled = frcs.isDeveloperModeEnabled();
        Log.v(CLASS_NAME, String.format(
                "%s(): isDeveloperModeEnabled = %b", methodName, isDeveloperModeEnabled));
        // fetchTimeMillis - timestamp in milliseconds of last successful fetch
        long fetchTimeMillis = remoteConfigInfo.getFetchTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fetchTimestamp = sdf.format(new Date(fetchTimeMillis));
        Log.v(CLASS_NAME, String.format(
                "%s(): fetchTimeMillis = %d (%s)", methodName, fetchTimeMillis, fetchTimestamp));
        // lastFetchStatus
        int lastFetchStatus = remoteConfigInfo.getLastFetchStatus();
        String lastFetchStatusString = "?";
        switch (lastFetchStatus) {
            case FirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE:
                lastFetchStatusString = "LAST_FETCH_STATUS_FAILURE";
                break;
            case FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET:
                lastFetchStatusString = "LAST_FETCH_STATUS_NO_FETCH_YET";
                break;
            case FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS:
                lastFetchStatusString = "LAST_FETCH_STATUS_SUCCESS";
                break;
            case FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED:
                lastFetchStatusString = "LAST_FETCH_STATUS_THROTTLED";
                break;
        }
        Log.v(CLASS_NAME, String.format(
                "%s(): lastFetchStatus = %d (%s)",
                methodName, lastFetchStatus, lastFetchStatusString));
        // valueSource
        FirebaseRemoteConfigValue v = remoteConfig.getValue("is_parsing_error_logging_enabled");
        int valueSource = v.getSource();
        String valueSourceString = "?";
        switch (valueSource) {
            case FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT:
                valueSourceString = "VALUE_SOURCE_DEFAULT";
                break;
            case FirebaseRemoteConfig.VALUE_SOURCE_REMOTE:
                valueSourceString = "VALUE_SOURCE_REMOTE";
                break;
            case FirebaseRemoteConfig.VALUE_SOURCE_STATIC:
                valueSourceString = "VALUE_SOURCE_STATIC";
                break;
        }
        Log.v(CLASS_NAME, String.format(
                "%s(): valueSource for is_parsing_error_logging_enabled = %d (%s)",
                methodName, valueSource, valueSourceString));
        Log.v(CLASS_NAME, String.format(
                "%s(): value for is_parsing_error_logging_enabled = %b",
                methodName, v.asBoolean()));
    } // logFirebaseRemoteConfig()

    public static void logParsingError(String tag, Exception e) {
        final String EXCEPTION_CAUGHT = "Exception caught";
        boolean isParsingErrorLoggingEnabled = FirebaseRemoteConfig.getInstance()
                .getBoolean("is_parsing_error_logging_enabled");
        if (isParsingErrorLoggingEnabled) {
            logError(e);
        } else {
            Log.e(tag, EXCEPTION_CAUGHT, e);
        }
        //logFirebaseRemoteConfig();
    } // logParsingError()

    public static void reportAction(
            @NotNull Context context, @NotNull String actionTitle, int actionId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isTrackingEnabled = sp.getBoolean(SettingsActivity.TRACKING_PREFERENCE_KEY, true);
        if (isTrackingEnabled) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(actionId));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, actionTitle);
            FirebaseAnalytics.getInstance(context).logEvent(
                    FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            Log.v(CLASS_NAME, String.format("%s(): %s=%s; %s=%s", "reportAction",
                    "ITEM_ID", actionId, "CONTENT_TYPE", actionTitle));
        }
    } // reportAction()

    public static void setBooleanUserProperty(
            @NotNull Context context, @NotNull String propertyName, boolean propertyValue) {
        if (propertyName.equals(IS_TRACKING_ENABLED_USERPROPERTY)) {
            // Disabled tracking will ignore updates of user properties
            if (propertyValue) {
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(propertyValue);
                FirebaseAnalytics.getInstance(context).setUserProperty(
                        propertyName, String.valueOf(propertyValue));
            } else {
                FirebaseAnalytics.getInstance(context).setUserProperty(
                        propertyName, String.valueOf(propertyValue));
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(propertyValue);
            }
        } else {
            FirebaseAnalytics.getInstance(context).setUserProperty(
                    propertyName, String.valueOf(propertyValue));
        }
        Log.v(CLASS_NAME, String.format("%s(): %s=%s; %s=%b", "setBooleanUserProperty",
                "propertyName", propertyName, "propertyValue", propertyValue));
    } // setBooleanUserProperty()

    public static void setLongUserProperty(
            @NotNull Context context, @NotNull String propertyName, long propertyValue) {
        FirebaseAnalytics.getInstance(context).setUserProperty(
                propertyName, String.valueOf(propertyValue));
        Log.v(CLASS_NAME, String.format("%s(): %s=%s; %s=%d", "setLongUserProperty",
                "propertyName", propertyName, "propertyValue", propertyValue));
    } // setLongUserProperty()
} // class PlayStoreHelper

