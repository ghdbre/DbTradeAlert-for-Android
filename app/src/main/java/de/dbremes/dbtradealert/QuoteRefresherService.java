package de.dbremes.dbtradealert;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.Calendar;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class QuoteRefresherService extends IntentService {
    private static final String CLASS_NAME = "QuoteRefresherService";
    public static final String BROADCAST_ACTION_NAME = "QuoteRefresherAction";
    public static final String BROADCAST_EXTRA_ERROR = "Error: ";
    public static final String BROADCAST_EXTRA_NAME = "Message";
    public static final String BROADCAST_EXTRA_REFRESH_COMPLETED = "Refresh completed";
    public static final String INTENT_EXTRA_IS_MANUAL_REFRESH = "isManualRefresh";
    private static final String exceptionMessage = "Exception caught";

    public QuoteRefresherService() {
        super("QuoteRefresherService");
    } // ctor()

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

    @Override
    protected void onHandleIntent(Intent intent) {
        String baseUrl = "http://download.finance.yahoo.com/d/quotes.csv";
        String url = baseUrl
                + "?f=" + DbHelper.QuoteDownloadFormatParameter
                + "&s=" + getSymbolParameterValue();
        String quoteCsv = "";
        try {
            boolean isManualRefresh = intent.getBooleanExtra(INTENT_EXTRA_IS_MANUAL_REFRESH, false);
            if (isManualRefresh || areExchangesOpenNow()) {
                if (isConnected()) {
                        quoteCsv = downloadQuotes(url);
                        DbHelper dbHelper = new DbHelper(this);
                        dbHelper.updateOrCreateQuotes(quoteCsv);
                } else {
                    sendLocalBroadcast(BROADCAST_EXTRA_ERROR + "no Internet!");
                    Log.d(CLASS_NAME, BROADCAST_EXTRA_ERROR + "no Internet!");
                }
                Log.d(CLASS_NAME,
                        "onHandleIntent(): quotes updated - initiating screen refresh");
                sendLocalBroadcast(BROADCAST_EXTRA_REFRESH_COMPLETED);
                QuoteRefreshAlarmReceiver.completeWakefulIntent(intent);
            } else {
                Log.d(CLASS_NAME,
                    "onHandleIntent(): exchanges closed and not a manual reefresh - skipping alarm");
            }
        } catch (IOException e) {
            Log.e(CLASS_NAME, exceptionMessage, e);
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
            } else {
                sendLocalBroadcast(BROADCAST_EXTRA_ERROR
                        + "download failed (response code " + responseCode + ")!");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        Log.d(CLASS_NAME, "downloadQuotes(): got " + result.length() + " characters");
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
            sb.append(line + "\n");
        }
        return sb.toString();
    } // getStringFromStream()

    private String getSymbolParameterValue() {
        String result = "";
        DbHelper dbHelper = new DbHelper(this);
        Cursor cursor = dbHelper.readAllSecuritySymbols();
        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            sb.append(cursor.getString(0) + "+");
        }
        cursor.close();
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
        Intent intent = new Intent(BROADCAST_ACTION_NAME);
        intent.putExtra(BROADCAST_EXTRA_NAME, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    } // sendLocalBroadcast()
} // class QuoteRefresherService
