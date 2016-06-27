package de.dbremes.dbtradealert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class QuoteRefreshScheduler extends BroadcastReceiver {
    final static String CLASS_NAME = "QuoteRefreshScheduler";

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode = 0;
        Intent newIntent = new Intent(context, QuoteRefreshAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, pendingIntent);
        // Log what was done
        String scheduleCreationType;
        if (intent.getAction() != null
                && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            scheduleCreationType = "after reboot";
        } else {
            scheduleCreationType = "initially";
        }
        Log.d(CLASS_NAME, "onReceive(): quote refresh schedule created " + scheduleCreationType);
    } // onReceive()
} // class QuoteRefreshScheduler
