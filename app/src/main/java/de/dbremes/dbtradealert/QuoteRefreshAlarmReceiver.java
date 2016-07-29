package de.dbremes.dbtradealert;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class QuoteRefreshAlarmReceiver extends WakefulBroadcastReceiver {
    // Logging tag can have at most 23 characters
    final static String CLASS_NAME = "QuoteRefreshAlarmRec.";

    @Override
    public void onReceive(Context context, Intent intent) {
        scheduleNextQuoteRefresh(context);
        Log.d(CLASS_NAME,
                "onReceive(): quote refresh alarm received; starting QuoteRefresherService");
        Intent service = new Intent(context, QuoteRefresherService.class);
        startWakefulService(context, service);
    } // onReceive()

    private void scheduleNextQuoteRefresh(Context context) {
        // Starting with Android Marshmallow only
        // AlarmManager.setAndAllowWhileIdle() works in Doze / App Standby mode
        // and unlike setInexactRepeating() cannot set repeating alarms. So apps
        // need to set the next alarm themselves each time an alarm goes off.
        if (Utils.isAndroidBeforeMarshmallow() == false) {
            Log.d(CLASS_NAME,
                    "scheduleNextQuoteRefresh(): ");
            Intent intent = new Intent(context, QuoteRefreshScheduler.class);
            intent.setAction("QuoteRefreshAlarmReceiver.scheduleNextQuoteRefresh()");
            context.sendBroadcast(intent);
        }
    } // scheduleNextQuoteRefresh()

} // class QuoteRefreshAlarmReceiver
