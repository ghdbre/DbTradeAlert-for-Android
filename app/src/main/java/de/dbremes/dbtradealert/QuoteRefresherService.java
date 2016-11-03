package de.dbremes.dbtradealert;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.ReminderContract;

public class QuoteRefresherService extends IntentService {
    private static final String CLASS_NAME = "QuoteRefresherService";
    public static final String QUOTE_REFRESHER_BROADCAST = "QuoteRefresherBroadcast";
    public static final String QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA = "Error: ";
    public static final String QUOTE_REFRESHER_BROADCAST_NAME_EXTRA = "Message";
    public static final String QUOTE_REFRESHER_BROADCAST_REFRESH_COMPLETED_EXTRA
            = "Refresh completed";
    public static final String QUOTE_REFRESHER_BROADCAST_IS_MANUAL_REFRESH_INTENT_EXTRA
            = "isManualRefresh";

    public QuoteRefresherService() {
        super("QuoteRefresherService");
    } // ctor()

    private void addOpenManageRemindersScreenAction(NotificationCompat.Builder builder) {
        Intent remindersManagementIntent = new Intent(this, RemindersManagementActivity.class);
        PendingIntent remindersManagementPendingIntent
                = PendingIntent.getActivity(this, 0, remindersManagementIntent, 0);
        int icon = R.drawable.ic_go_search_api_holo_light;
        String title = "Reminders";
        builder.addAction(icon, title, remindersManagementPendingIntent);
    } // addOpenManageRemindersScreenAction()

    private boolean areExchangesOpenNow() {
        final String methodName = "areExchangesOpenNow";
        boolean result = false;
        Calendar now = Calendar.getInstance();
        boolean isBusinessHour = isBusinessHour(now);
        if (isBusinessHour) {
            result = isBusinessDay(now);
        }
        Log.d(CLASS_NAME, String.format(
                "%s(): Exchanges %sopen", methodName, result ? "" : "not "));
        return result;
    }// areExchangesOpenNow()

    private String buildNotificationFromDueReminder(Cursor dueRemindersCursor) {
        String result = "";
        int headingColumnIndex
                = dueRemindersCursor.getColumnIndex(ReminderContract.Reminder.HEADING);
        result = dueRemindersCursor.getString(headingColumnIndex);
        return result;
    } // buildNotificationFromDueReminder()

    private String buildNotificationFromTriggeredSignal(Cursor cursor) {
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
    } // buildNotificationFromTriggeredSignal()

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
            int responseCode = -1;
            try {
                // Starts the query
                conn.connect();
                responseCode = conn.getResponseCode();
            } catch (SocketTimeoutException e) {
                sendLocalBroadcast(
                        QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "connection timed out!");
                Log.d(CLASS_NAME,
                        QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "connection timed out!");
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = conn.getInputStream();
                result = getStringFromStream(inputStream);
                Log.d(CLASS_NAME, "downloadQuotes(): got " + result.length() + " characters");
            } else {
                sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA
                        + "download failed (response code " + responseCode + ")!");
                PlayStoreHelper.logError(CLASS_NAME,
                        QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA
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
            PlayStoreHelper.logError(e);
        }
        return result;
    } // getSymbolParameterValue()

    private boolean isBusinessDay(Calendar now) {
        boolean result = false;
        int thisDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set businessDays = sharedPreferences.getStringSet(
                "business_days_preference", Collections.<String>emptySet());
        Utils.BusinessTimesPreferenceExtremes
                btpe = Utils.getBusinessTimesPreferenceExtremes(businessDays);
        result = (btpe.getFirstBusinessTime() <= thisDayOfWeek
                && btpe.getLastBusinessTime() >= thisDayOfWeek);
        // Log result details
        String s = String.valueOf(thisDayOfWeek) + (result ? " is" : " is not")
                + String.format(" in business days (%d - %d)",
                btpe.getFirstBusinessTime(),
                btpe.getLastBusinessTime()
        );
        Log.v(CLASS_NAME, s);
        return result;
    } // isBusinessDay()

    private boolean isBusinessHour(Calendar now) {
        boolean result = false;
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set businessHours = sharedPreferences.getStringSet(
                "business_hours_preference", Collections.<String>emptySet());
        Utils.BusinessTimesPreferenceExtremes
                btpe = Utils.getBusinessTimesPreferenceExtremes(businessHours);
        result = (btpe.getFirstBusinessTime() <= hourOfDay
                && btpe.getLastBusinessTime() >= hourOfDay);
        // Log result details
        String s = String.valueOf(hourOfDay) + (result ? " is" : " is not")
                + String.format(" in business hours (%02d - %02d)",
                btpe.getFirstBusinessTime(),
                btpe.getLastBusinessTime()
        );
        Log.v(CLASS_NAME, s);
        return result;
    } // isBusinessHour()

    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    } // isConnected()

    @Override
    protected void onHandleIntent(Intent intent) {
        String baseUrl = "http://" + BuildConfig.HOST + ":" + BuildConfig.PORT + "/d/quotes.csv";
        String url = baseUrl
                + "?f=" + DbHelper.QUOTE_DOWNLOAD_FORMAT_PARAMETER
                + "&s=" + getSymbolParameterValue();
        try {
            boolean isManualRefresh = intent.getBooleanExtra(
                    QUOTE_REFRESHER_BROADCAST_IS_MANUAL_REFRESH_INTENT_EXTRA, false);
            if (isManualRefresh || areExchangesOpenNow()) {
                if (isConnected()) {
                    String quoteCsv = downloadQuotes(url);
                    if (TextUtils.isEmpty(quoteCsv) == false) {
                        DbHelper dbHelper = new DbHelper(this);
                        dbHelper.updateOrCreateQuotes(quoteCsv);
                        // Notify user of triggered signals and reminders even if app is sleeping
                        dbHelper.updateSecurityMaxPrice();
                        sendNotification(dbHelper);
                        Log.d(CLASS_NAME,
                                "onHandleIntent(): quotes updated - initiating screen refresh");
                        sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_REFRESH_COMPLETED_EXTRA);
                    }
                    // Otherwise downloadQuotes() will report the error
                } else {
                    sendLocalBroadcast(QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "no Internet!");
                    PlayStoreHelper.logConnectionError(
                            CLASS_NAME, QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "no Internet!");
                }
            } else {
                Log.d(CLASS_NAME,
                        "onHandleIntent(): exchanges closed and not a manual refresh - skipping alarm");
            }
        } catch (IOException e) {
            // To reduce the amount of log spew that apps do in the non-error condition of the
            // network being unavailable Log.e() doesn't print UnknownHostException.
            if (e instanceof UnknownHostException) {
                // java.net.UnknownHostException:
                // Unable to resolve host "download.finance.yahoo.com":
                // No address associated with hostname
                sendLocalBroadcast(
                        QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "broken Internet connection!");
                PlayStoreHelper.logConnectionError(CLASS_NAME,
                        QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA + "broken Internet connection!");
            } else {
                PlayStoreHelper.logError(e);
            }
        } finally {
            QuoteRefreshAlarmReceiver.completeWakefulIntent(intent);
        }
    } // onHandleIntent()

    private void sendLocalBroadcast(String message) {
        Intent intent = new Intent(QUOTE_REFRESHER_BROADCAST);
        intent.putExtra(QUOTE_REFRESHER_BROADCAST_NAME_EXTRA, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    } // sendLocalBroadcast()

    private void sendNotification(DbHelper dbHelper) {
        final String methodName = "sendNotification";
        Cursor dueRemindersCursor = dbHelper.readAllDueReminders();
        Cursor triggeredSignalsCursor = dbHelper.readAllTriggeredSignals();
        try {
            int notificationCount
                    = triggeredSignalsCursor.getCount() + dueRemindersCursor.getCount();
            if (notificationCount > 0) {
                Context context = getApplicationContext();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setColor(Color.GREEN)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setNumber(notificationCount)
                        .setSmallIcon(R.drawable.emo_im_money_mouth);
                // Tapping notification should lead to app's main screen
                Intent watchlistListIntent = new Intent(this, WatchlistListActivity.class);
                PendingIntent watchlistListPendingIntent
                        = PendingIntent.getActivity(context, 0, watchlistListIntent, 0);
                builder.setContentIntent(watchlistListPendingIntent);
                // Build back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(WatchlistListActivity.class);
                stackBuilder.addNextIntent(watchlistListIntent);
                // Create notification
                if (notificationCount == 1) {
                    String contentText;
                    if (dueRemindersCursor.getCount() == 1) {
                        dueRemindersCursor.moveToFirst();
                        contentText = buildNotificationFromDueReminder(dueRemindersCursor);
                    } else {
                        triggeredSignalsCursor.moveToFirst();
                        contentText = buildNotificationFromTriggeredSignal(triggeredSignalsCursor);
                    }
                    builder.setContentTitle("Trade alert").setContentText(contentText);
                    Log.v(CLASS_NAME,
                            String.format("%s(): Trade alert = %s", methodName, contentText));
                } else {
                    // Wrap all notifications into one inboxStyle notification
                    builder.setContentTitle(notificationCount + " Trade alerts");
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    builder.setStyle(inboxStyle);
                    while (dueRemindersCursor.moveToNext()) {
                        String s = buildNotificationFromDueReminder(dueRemindersCursor);
                        inboxStyle.addLine(s);
                        Log.v(CLASS_NAME, String.format("%s(): trade alert = %s", methodName, s));
                    }
                    while (triggeredSignalsCursor.moveToNext()) {
                        String s = buildNotificationFromTriggeredSignal(triggeredSignalsCursor);
                        inboxStyle.addLine(s);
                        Log.v(CLASS_NAME, String.format("%s(): trade alert = %s", methodName, s));
                    }
                }
                if (dueRemindersCursor.getCount() > 0) {
                    addOpenManageRemindersScreenAction(builder);
                }
                // Show notification
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                // Update pending notification if existing
                final int notificationId = 1234;
                notificationManager.notify(notificationId, builder.build());
            }
            Log.d(CLASS_NAME, String.format(
                    "%s(): created trade alert for %d reminders + signals",
                    methodName, notificationCount));
        } finally {
            DbHelper.closeCursor(dueRemindersCursor);
            DbHelper.closeCursor(triggeredSignalsCursor);
        }
    } // sendNotification()
} // class QuoteRefresherService
