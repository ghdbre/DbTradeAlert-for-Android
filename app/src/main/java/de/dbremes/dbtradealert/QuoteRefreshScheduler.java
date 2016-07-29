package de.dbremes.dbtradealert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class QuoteRefreshScheduler extends BroadcastReceiver {
    final static String CLASS_NAME = "QuoteRefreshScheduler";

    @Override
    @SuppressWarnings("NewApi")
    public void onReceive(Context context, Intent intent) {
        int requestCode = 0;
        Intent newIntent = new Intent(context, QuoteRefreshAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        boolean isAutoRefreshEnabled = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean("auto_refresh_preference", true);
        if (isAutoRefreshEnabled == false) {
            alarmManager.cancel(pendingIntent);
            Log.d(CLASS_NAME, "onReceive(): quote refresh cancelled");
        } else {
            // Create schedule for quote refresh (every hour, starting 1 hour from now)
            if (Utils.isAndroidBeforeMarshmallow()) {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                        AlarmManager.INTERVAL_HOUR, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR, pendingIntent);
            }
            // Use only for testing Doze and App Standby modes!
//        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                1 * 60 * 1000, 1 * 60 * 1000, pendingIntent);
            // Log what was done
            String scheduleCreationType = "";
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                    scheduleCreationType = "after reboot";
                } else {
                    scheduleCreationType = "by " + intent.getAction();
                }
            }
            Log.d(CLASS_NAME,
                    "onReceive(): quote refresh schedule created " + scheduleCreationType);
        }
    } // onReceive()
} // class QuoteRefreshScheduler
