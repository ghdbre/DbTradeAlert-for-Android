package de.dbremes.dbtradealert;

public class RemoteConfigHelper {

    private RemoteConfigHelper() {
        // Hide default construcor
    } // ctor()

    public static void initialize(boolean isDeveloperModeEnabled) {
        // NOP
    } // initialize()

    public static boolean isConnectionErrorLoggingEnabled() {
        return true;
    } // isConnectionErrorLoggingEnabled()

    public static boolean isParsingErrorLoggingEnabled() {
        return true;
    } // isParsingErrorLoggingEnabled()
} // class RemoteConfigHelper
