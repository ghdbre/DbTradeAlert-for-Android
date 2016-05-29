package de.dbremes.dbtradealert;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class QuoteRefresherAsyncTask extends AsyncTask<Context, Void, Void> {
    private static final String CLASS_NAME = "QuoteRefresherAsyncTask";
    public static final String BROADCAST_ACTION_NAME = "QuoteRefresherAction";
    public static final String BROADCAST_EXTRA_ERROR = "Error: ";
    public static final String BROADCAST_EXTRA_NAME = "Message";
    public static final String BROADCAST_EXTRA_REFRESH_COMPLETED = "Refresh completed";
    private Context context;

    @Override
    protected Void doInBackground(Context... params) {
        this.context = params[0];
        String baseUrl = "http://download.finance.yahoo.com/d/quotes.csv";
        String url = baseUrl
                + "?f=" + DbHelper.QuoteDownloadFormatParameter
                + "&s=" + getSymbolParameterValue();
        String quoteCsv = "";
        try {
            if (isConnected()) {
                quoteCsv = downloadQuotes(url);
                DbHelper dbHelper = new DbHelper(this.context);
                dbHelper.updateOrCreateQuotes(quoteCsv);
            } else {
                sendLocalBroadcast(BROADCAST_EXTRA_ERROR + "no Internet!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    } // doInBackground()

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
        return result;
    } // downloadQuotes()

    private String getStringFromStream(InputStream inputStream) throws IOException {
        // This won't work as inputStream.available() always returns 0:
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
        DbHelper dbHelper = new DbHelper(this.context);
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
            e.printStackTrace();
        }
        return result;
    } // getSymbolParameterValue()

    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    } // isConnected()

    @Override
    protected void onPostExecute(Void result) {
        sendLocalBroadcast(BROADCAST_EXTRA_REFRESH_COMPLETED);
    } // onPostExecute()

    private void sendLocalBroadcast(String message) {
        Intent intent = new Intent(BROADCAST_ACTION_NAME);
        intent.putExtra(BROADCAST_EXTRA_NAME, message);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    } // sendLocalBroadcast()

} // class QuoteRefresherAsyncTask
