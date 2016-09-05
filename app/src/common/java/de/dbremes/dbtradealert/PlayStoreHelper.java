package de.dbremes.dbtradealert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import org.jetbrains.annotations.NotNull;

public class PlayStoreHelper {
    private static final String CLASS_NAME = "PlayStoreHelper";
    public final static String IS_TRACKING_ENABLED_USERPROPERTY = "isTrackingEnabled";
    public final static String REMINDER_COUNT_USERPROPERTY = "reminderCount";
    public final static String SECURITY_COUNT_USERPROPERTY = "securityCount";
    public final static String WATCHLIST_COUNT_USERPROPERTY = "watchlistCount";

    private PlayStoreHelper() {
        // Hide default construcor
    }

    public static void logConnectionError(String tag, String message) {
        boolean isConnectionErrorLoggingEnabled
                = RemoteConfigHelper.isConnectionErrorLoggingEnabled();
        if (isConnectionErrorLoggingEnabled) {
            logDebugMessage(tag, message);
        } else {
            Log.e(tag, message);
        }
    } // logConnectionError()

    public static void logDebugMessage(String tag, String message) {
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
        FirebaseCrash.logcat(Log.ERROR, tag, message);
    } // logError()

    public static void logError(Exception e) {
        FirebaseCrash.report(e);
    } // logError()

    public static void logParsingError(String tag, Exception e) {
        final String EXCEPTION_CAUGHT = "Exception caught";
        boolean isParsingErrorLoggingEnabled
                = RemoteConfigHelper.isParsingErrorLoggingEnabled();
        if (isParsingErrorLoggingEnabled) {
            logDebugMessage(e);
        } else {
            Log.e(tag, EXCEPTION_CAUGHT, e);
        }
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
