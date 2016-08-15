package de.dbremes.dbtradealert;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

public class PlayStoreHelper {
    public final static String IS_TRACKING_ENABLED_USERPROPERY = "";

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
} // class PlayStoreHelper
