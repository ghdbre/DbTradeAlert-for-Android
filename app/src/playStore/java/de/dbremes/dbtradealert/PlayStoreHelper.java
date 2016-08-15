package de.dbremes.dbtradealert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;

public class PlayStoreHelper {
    public final static String IS_TRACKING_ENABLED_USERPROPERY = "isTrackingEnabled";

    private PlayStoreHelper() {
        // Hide default construcor
    }

    public static void reportAction(
            @NotNull Context context, @NotNull String actionTitle, int actionId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isTrackingEnabled = sp.getBoolean(SettingsActivity.TRACKING_PREFERENCE_KEY, true);
        if(isTrackingEnabled) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(actionId));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, actionTitle);
            FirebaseAnalytics.getInstance(context).logEvent(
                    FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    } // reportAction()

    public static void setBooleanUserProperty(
            @NotNull Context context, @NotNull String propertyName, boolean propertyValue) {
        FirebaseAnalytics.getInstance(context).setUserProperty(
                propertyName, String.valueOf(propertyValue));
        if (propertyName.equals(IS_TRACKING_ENABLED_USERPROPERY)) {
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(propertyValue);
        }
    } // setBooleanUserProperty()
} // class PlayStoreHelper
