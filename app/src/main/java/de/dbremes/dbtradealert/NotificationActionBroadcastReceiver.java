package de.dbremes.dbtradealert;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class NotificationActionBroadcastReceiver extends BroadcastReceiver {
    // Max 23 characters for Log tag
    private final static String CLASS_NAME = "Notif.Act.BroadcastRec.";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String methodName = "onReceive";
        boolean isDeactivateReminderAction = intent.getAction().equals(
                QuoteRefresherService.NOTIFICATION_ACTION_DEACTIVATE_REMINDER_BROADCAST);
        boolean isDeleteReminderAction = intent.getAction().equals(
                QuoteRefresherService.NOTIFICATION_ACTION_DELETE_REMINDER_BROADCAST);
        if (isDeactivateReminderAction || isDeleteReminderAction) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                long reminderId = extras.getLong(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA);
                DbHelper dbHelper = new DbHelper(context);
                if (isDeactivateReminderAction) {
                    dbHelper.deactivateReminder(reminderId);
                    Log.d(CLASS_NAME, methodName + "(): deactivated reminder with Id " + reminderId);
                } else {
                    dbHelper.deleteReminder(reminderId);
                    Log.d(CLASS_NAME, methodName + "(): deleted reminder with Id " + reminderId);
                }
                // Remove notification for deactivated or deleted reminder
                NotificationManager notificationManager
                        = (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);
                notificationManager.cancel((int) reminderId);
            }
        }
    } // onReceive()
} // class NotificationActionBroadcastReceiver
