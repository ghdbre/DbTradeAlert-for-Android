package de.dbremes.dbtradealert;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfigHelper {

    private RemoteConfigHelper() {
        // Hide default construcor
    } // ctor()

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
                        } else {
                            PlayStoreHelper.reportException(task.getException());
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

    public static boolean isConnectionErrorLoggingEnabled() {
        return FirebaseRemoteConfig.getInstance().getBoolean("is_connection_error_logging_enabled");
    } // isConnectionErrorLoggingEnabled()

    public static boolean isParsingErrorLoggingEnabled() {
        return FirebaseRemoteConfig.getInstance().getBoolean("is_parsing_error_logging_enabled");
    } // isParsingErrorLoggingEnabled()
} // class RemoteConfigHelper
