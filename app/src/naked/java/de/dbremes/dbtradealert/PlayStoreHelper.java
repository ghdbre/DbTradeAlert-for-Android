package de.dbremes.dbtradealert;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class PlayStoreHelper {
    private static final String CLASS_NAME = "PlayStoreHelper";
    public final static String IS_TRACKING_ENABLED_USERPROPERTY = "";
    public final static String REMINDER_COUNT_USERPROPERTY = "";
    public final static String SECURITY_COUNT_USERPROPERTY = "";
    public final static String WATCHLIST_COUNT_USERPROPERTY = "";

    private PlayStoreHelper() {
        // Hide default construcor
    }

    public static void initialize(boolean isDeveloperModeEnabled) {
        // NOP
    } // initialize()

    public static void logConnectionError(String tag, String message) {
        logError(tag, message);
    } // logConnectionError()

    public static void logDebugMessage(String tag, String message) {
        Log.d(tag, message);
    } // logDebugMessage()

    public static void logDebugMessage(Exception e) {
        String message = "";
        String stackTrace = "";
        if (e != null) {
            message = e.getMessage();
            stackTrace = Log.getStackTraceString(e);
            Log.d(message, stackTrace);
        }
    } // logDebugMessage()

    public static void logError(String tag, String message) {
        Log.e(tag, message);
    } // logError()

    public static void logError(Exception e) {
        Log.e(CLASS_NAME, "Exception caught", e);
    } // logError()

    public static void logParsingError(String tag, Exception e) {
        logError(e);
    } // logParsingError()

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
