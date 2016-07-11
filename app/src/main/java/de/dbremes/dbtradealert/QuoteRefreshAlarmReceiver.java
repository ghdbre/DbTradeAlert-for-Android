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
        Log.d(CLASS_NAME,
                "onReceive(): quote refresh alarm received; starting QuoteRefresherService");
        Intent service = new Intent(context, QuoteRefresherService.class);
        startWakefulService(context, service);
    } // onReceive()
} // class QuoteRefreshAlarmReceiver
