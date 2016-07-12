package de.dbremes.dbtradealert;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Locale;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class QuoteRefresherService extends IntentService {
    private static final String CLASS_NAME = "QuoteRefresherService";
    public static final String NOTIFICATION_ACTION_DEACTIVATE_REMINDER_BROADCAST
            = "DeactivateReminderBroadcast";
    public static final String NOTIFICATION_ACTION_DELETE_REMINDER_BROADCAST
            = "DeleteReminderBroadcast";
    public static final String QUOTE_REFRESHER_BROADCAST = "QuoteRefresherBroadcast";
    public static final String QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA = "Error: ";
    public static final String QUOTE_REFRESHER_BROADCAST_NAME_EXTRA = "Message";
    public static final String QUOTE_REFRESHER_BROADCAST_REFRESH_COMPLETED_EXTRA
            = "Refresh completed";
    public static final String QUOTE_REFRESHER_BROADCAST_IS_MANUAL_REFRESH_INTENT_EXTRA
            = "isManualRefresh";
    private static final String exceptionMessage = "Exception caught";

    public QuoteRefresherService() {
        super("QuoteRefresherService");
    } // ctor()

    private void addActionToNotification(
            String action, NotificationCompat.Builder builder, long reminderId) {
        Intent actionIntent
                = new Intent(this, NotificationActionBroadcastReceiver.class);
        actionIntent.setAction(action + "_" + String.valueOf(reminderId));
        //actionIntent.setAction(action);
        actionIntent.putExtra(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA, reminderId);
//        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(
//                this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(
                this, (int)reminderId, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        int icon;
        String title;
        if (action.equals(NOTIFICATION_ACTION_DEACTIVATE_REMINDER_BROADCAST)) {
            icon = android.R.drawable.ic_menu_close_clear_cancel;
            title = "Deactivate";
        } else {
            icon = android.R.drawable.ic_delete;
            title = "Delete";
        }
        builder.addAction(icon, title, actionPendingIntent);
    } // addActionToNotification()

    private boolean areExchangesOpenNow() {
        final String methodName = "areExchangesOpenNow";
        boolean result = false;
        Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        if (hourOfDay >= 9 && hourOfDay <= 18) {
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                result = true;
            } else {
                Log.d(CLASS_NAME, String.format(
                        "%s(): Exchanges closed on weekends (day = %d)",
                        methodName, dayOfWeek));
            }
        } else {
            Log.d(CLASS_NAME, String.format(
                    "%s(): Exchanges closed after hours (hour = %d)",
                    methodName, hourOfDay));
        }
        if (result) {
            Log.d(CLASS_NAME, String.format(
                    "%s(): Exchanges open", methodName));
        }
        return result;
    }// areExchangesOpenNow()

    private String buildNotificationLineFromCursor(Cursor cursor) {
        String result = "";
        String actualName = cursor.getString(0);
        float actualValue = cursor.getFloat(1);
        String signalName = cursor.getString(2);
        float signalValue = cursor.getFloat(3);
        String symbol = cursor.getString(4);
        // Example: NOVN.VX: low = 79.55; T = 92.07
        result = String.format(Locale.getDefault(),
                "%s: %s = %01.2f; %s = %01.2f", symbol, actualName,
                actualValue, signalName, signalValue);
        return result;
    } // buildNotificationLineFromCursor()

    private void configureIntents(NotificationCompat.Builder builder, Class activityClass,
                                  Context context, long reminderId) {
        // Specify which intent to show when user taps notification
        Intent intent = new Intent(this, activityClass);
        if (activityClass.equals(ReminderEditActivity.class)) {
            intent.putExtra(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA, reminderId);
            // Hack to create a new intent and not just overwrite an existing one with new data
            // Assumes an explicit intent. Use Category otherwise?
            intent.setAction(String.valueOf(reminderId));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) reminderId, intent, 0);
        builder.setContentIntent(pendingIntent);
        // Build back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);
    } // configureIntents()

    private NotificationCompat.Builder configureNotificationBuilder(Context context, Integer count) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setColor(Color.GREEN)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.emo_im_money_mouth);
        if (count != null) {
            builder.setNumber(count);
        }
        return builder;
    } // configureNotificationBuilder()

    @Override
    protected void onHandleIntent(Intent intent) {
        String baseUrl = "http://download.finance.yahoo.com/d/quotes.csv";
        String url = baseUrl
                + "?f=" + DbHelper.QuoteDownloadFormatParameter
                + "&s=" + getSymbolParameterValue();
        String quoteCsv = "";
        try {
            boolean isManualRefresh = intent.getBooleanExtra(
                    QUOTE_REFRESHER_BROADCAST_IS_MANUAL_REFRESH_INTENT_EXTRA, false);
            if (isManualRefresh || areExchangesOpenNow()) {
                if (isConnected()) {
                    quoteCsv = downloadQuotes(url);
                    DbHelper dbHelper = new DbHelper(this);
                    dbHelper.updateOrCreateQuotes(quoteCsv);
                    // Notify user of triggered signals and reminders even if app is sleeping
                    dbHelper.updateSecurityMaxPrice();
                    sendNotificationForTriggeredSignals(dbHelper);
                    sendNotificationsForDueReminders(dbHelper);
                    Log.d(CLASS_NAME,
                            "onHandleIntent(): quotes updated - initiating screen refresh");
                    sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_REFRESH_COMPLETED_EXTRA);
                } else {
                    sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "no Internet!");
                    Log.d(CLASS_NAME, QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "no Internet!");
                }
            } else {
                Log.d(CLASS_NAME,
                        "onHandleIntent(): exchanges closed and not a manual refresh - skipping alarm");
            }
        } catch (IOException e) {
            Log.e(CLASS_NAME, exceptionMessage, e);
            if (e instanceof UnknownHostException) {
                // java.net.UnknownHostException:
                // Unable to resolve host "download.finance.yahoo.com":
                // No address associated with hostname
                sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "broken Internet connection!");
                Log.e(CLASS_NAME, QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "broken Internet connection!");
            }
            // TODO: cannot rethrow in else case as that doesn't match overridden methods signature?
        } finally {
            QuoteRefreshAlarmReceiver.completeWakefulIntent(intent);
        }
    } // onHandleIntent()

    private String downloadQuotes(String urlString) throws IOException {
        String result = "";
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                inputStream = conn.getInputStream();
                result = getStringFromStream(inputStream);
                Log.d(CLASS_NAME, "downloadQuotes(): got " + result.length() + " characters");
            } else {
                sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA
                        + "download failed (response code " + responseCode + ")!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return result;
    } // downloadQuotes()

    private String getStringFromStream(InputStream inputStream) throws IOException {
        // Elaborate solution as this won't work because inputStream.available() always returns 0:
        // byte[] data = new byte[inputStream.available()];
        // inputStream.read(data);
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    } // getStringFromStream()

    private String getSymbolParameterValue() {
        String result = "";
        DbHelper dbHelper = new DbHelper(this);
        Cursor cursor = dbHelper.readAllSecuritySymbols();
        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            sb.append(cursor.getString(0)).append("+");
        }
        DbHelper.closeCursor(cursor);
        String symbols = sb.toString();
        if (sb.length() > 0) {
            symbols = symbols.substring(0, symbols.length() - 1);
        }
        // Index symbols like "^SSMI" start with a "^" which is not allowed in URLs
        try {
            symbols = URLEncoder.encode(symbols, "UTF-8");
            result += symbols;
        } catch (UnsupportedEncodingException e) {
            Log.e(CLASS_NAME, exceptionMessage, e);
        }
        return result;
    } // getSymbolParameterValue()

    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    } // isConnected()

    private void sendLocalBroadcast(String message) {
        Intent intent = new Intent(QUOTE_REFRESHER_BROADCAST);
        intent.putExtra(QUOTE_REFRESHER_BROADCAST_NAME_EXTRA, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    } // sendLocalBroadcast()

    private void sendNotificationsForDueReminders(DbHelper dbHelper) {
        final String methodName = "sendNotificationsForDueReminders";
        Cursor cursor = dbHelper.readAllDueReminders();
        try {
            if (cursor.getCount() > 0) {
                Context context = getApplicationContext();
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                // Create and show notifications
                while (cursor.moveToNext()) {
                    NotificationCompat.Builder builder
                            = configureNotificationBuilder(context, cursor.getCount());
                    String reminderHeader = cursor.getString(0);
                    builder.setContentTitle("Reminder").setContentText(reminderHeader);
                    Log.v(CLASS_NAME,
                            String.format("%s(): Reminder = %s", methodName, reminderHeader));
                    long reminderId = cursor.getLong(1);
                    configureIntents(builder, ReminderEditActivity.class, context, reminderId);
                    addActionToNotification(
                            NOTIFICATION_ACTION_DEACTIVATE_REMINDER_BROADCAST, builder, reminderId);
                    addActionToNotification(
                            NOTIFICATION_ACTION_DELETE_REMINDER_BROADCAST, builder, reminderId);
                    // Using reminderId as notificationId avoids showing more than one notification
                    // for a reminder; also allows removing notification when user deletes reminder
                    // Casting _ID to an int may result in unexpected values as _IDs continue
                    // to grow
                    int notificationId = (int) reminderId;
                    notificationManager.notify(notificationId, builder.build());
                }
            }
            Log.d(CLASS_NAME,
                    String.format("%s(): created %d notifications", methodName, cursor.getCount()));
        } finally {
            DbHelper.closeCursor(cursor);
        }
    } // sendNotificationsForDueReminders()

    private void sendNotificationForTriggeredSignals(DbHelper dbHelper) {
        final String methodName = "sendNotificationForTriggeredSignals";
        Cursor cursor = dbHelper.readAllTriggeredSignals();
        if (cursor.getCount() > 0) {
            Context context = getApplicationContext();
            NotificationCompat.Builder builder
                    = configureNotificationBuilder(context, cursor.getCount());
            // reminderId only valid for reminder notifications
            long reminderId = -1;
            configureIntents(builder, WatchlistListActivity.class, context, reminderId);
            // Create notification
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                String s = buildNotificationLineFromCursor(cursor);
                builder.setContentTitle("Target hit").setContentText(s);
                Log.v(CLASS_NAME, String.format("%s(): Notification = %s", methodName, s));
            } else {
                builder.setContentTitle(cursor.getCount() + " Targets hit");
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                builder.setStyle(inboxStyle);
                while (cursor.moveToNext()) {
                    String s = buildNotificationLineFromCursor(cursor);
                    inboxStyle.addLine(s);
                    Log.v(CLASS_NAME, String.format("%s(): Notification = %s", methodName, s));
                }
            }
            // Show notification
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            // Update pending notification if existing
            final int notificationId = 1234;
            notificationManager.notify(notificationId, builder.build());
        }
        Log.d(CLASS_NAME,
                String.format("%s(): created %d notifications", methodName, cursor.getCount()));
        DbHelper.closeCursor(cursor);
    } // sendNotificationForTriggeredSignals()
} // class QuoteRefresherService
