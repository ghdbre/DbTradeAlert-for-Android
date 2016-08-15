package de.dbremes.dbtradealert;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

public class PlayStoreHelper {
    public final static String IS_TRACKING_ENABLED_USERPROPERTY = "";
    public final static String REMINDER_COUNT_USERPROPERTY = "";
    public final static String SECURITY_COUNT_USERPROPERTY = "";
    public final static String WATCHLIST_COUNT_USERPROPERTY = "";

    private PlayStoreHelper() {
        // Hide default construcor
    }

    public static void reportAction(
            @NotNull Context context, @NotNull String actionTitle, int actionId) {
        // NOP
    } // reportAction()

    public static void setBooleanUserProperty(
            @NotNull Context context, @NotNull String propertyName, boolean propertyValue) {
        // NOP
    } // setBooleanUserProperty()

    public static void setLongUserProperty(
            @NotNull Context context, @NotNull String propertyName, long propertyValue) {
        // NOP
    } // setLongUserProperty()
} // class PlayStoreHelper
