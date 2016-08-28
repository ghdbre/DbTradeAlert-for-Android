package de.dbremes.dbtradealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.ReminderContract;
import de.dbremes.dbtradealert.DbAccess.SecurityContract;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class WatchlistListActivity extends AppCompatActivity
        implements WatchlistFragment.OnListFragmentInteractionListener {
    private static final String CLASS_NAME = "WatchlistListActivity";
    private static final String TITLE_INSTANCE_STATE = "Title";
    private static final int REMINDERS_MANAGEMENT_REQUEST = 1;
    private static final int SECURITIES_MANAGEMENT_REQUEST = 2;
    private static final int SECURITY_EDIT_REQUEST = 3;
    private static final int WATCHLISTS_MANAGEMENT_REQUEST = 4;
    private DbHelper dbHelper = null;
    // title is static to survive a user switching off the screen because
    // Android will only use savedInstanceState when the activity is about
    // to be recreated
    private static String title;
    private WatchlistListPagerAdapter watchlistListPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private BroadcastReceiver quotesRefreshedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(
                    QuoteRefresherService.QUOTE_REFRESHER_BROADCAST_NAME_EXTRA);
            if (message.equals(
                    QuoteRefresherService.QUOTE_REFRESHER_BROADCAST_REFRESH_COMPLETED_EXTRA)) {
                Log.v("BroadcastReceiver",
                        "quotesRefreshedBroadcastReceiver triggered UI update");
                refreshAllWatchlists();
                boolean addTimestamp = true;
                updateTitle(addTimestamp);
            } else if (message.startsWith(
                    QuoteRefresherService.QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA)) {
                Toast.makeText(WatchlistListActivity.this, message, Toast.LENGTH_SHORT).show();
                PlayStoreHelper.logAsError("BroadcastReceiver",
                        "quotesRefreshedBroadcastReceiver error = '" + message + "'");
            }
        }
    };


    private long copyFile(File sourceFile, File targetFile) {
        final String methodName = "copyFile";
        long bytesTransferred = 0;
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        try {
            try {
                sourceChannel = new FileInputStream(sourceFile).getChannel();
                Log.v(CLASS_NAME, String.format(
                        "%s(): sourcefile = %s", methodName, sourceFile));
                targetChannel = new FileOutputStream(targetFile).getChannel();
                Log.v(CLASS_NAME, String.format(
                        "%s(): targetFile = %s", methodName, targetFile));
                bytesTransferred
                        = targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                Log.v(CLASS_NAME, String.format(
                        "%s(): bytesTransferred = %d", methodName, bytesTransferred));
            } finally {
                if (sourceChannel != null) {
                    sourceChannel.close();
                }
                if (targetChannel != null) {
                    targetChannel.close();
                }
            }
        } catch (IOException e) {
            PlayStoreHelper.reportException(e);
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return bytesTransferred;
    } // copyFile()

    private void copyDatabase(boolean isExport) {
        File sourceDb;
        File targetDb;
        String message;
        File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir != null) {
            if (isExport) {
                sourceDb = getDatabasePath(DbHelper.DB_NAME);
                targetDb = new File(externalFilesDir, DbHelper.DB_NAME);
            } else {
                sourceDb = new File(externalFilesDir, DbHelper.DB_NAME);
                targetDb = getDatabasePath(DbHelper.DB_NAME);
            }
            long bytesCopied = copyFile(sourceDb, targetDb);
            if (isExport) {
                MediaScannerConnection.scanFile(WatchlistListActivity.this,
                        new String[]{targetDb.getAbsolutePath()}, null, null);
            }
            message = (isExport ? "Exported to " : "Imported from ")
                    + targetDb + " (" + bytesCopied + " bytes copied)";
        } else {
            message = "External files directory not available. SD card unmounted?";
        }
        Log.d(CLASS_NAME, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    } // copyDatabase()

    private void createQuoteRefreshSchedule() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, QuoteRefreshScheduler.class);
        intent.setAction("WatchlistListActivity.createQuoteRefreshSchedule()");
        sendBroadcast(intent);
    } // createQuoteRefreshSchedule()

    @SuppressWarnings("NewApi")
    private void ensureExemptionFromBatteryOptimizations() {
        if (Utils.isAndroidBeforeMarshmallow() == false) {
            String packageName = getPackageName();
            PowerManager powerManager = getSystemService(PowerManager.class);
            if (powerManager.isIgnoringBatteryOptimizations(packageName) == false) {
                String explanation = "DbTradeAlert needs to download quotes even when in background!";
                Toast.makeText(this, explanation, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    } // ensureExemptionFromBatteryOptimizations()

    private String getTime() {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        // Seems not to be necessary:
        //sdf.setTimeZone(TimeZone.getDefault());
        result = sdf.format(new Date());
        return result;
    } // getTime()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String methodName = "onActivityResult";
        Context context = getApplicationContext();
        long recordCount;
        switch (requestCode) {
            case REMINDERS_MANAGEMENT_REQUEST:
                recordCount = this.dbHelper.getRecordCount(ReminderContract.Reminder.TABLE);
                PlayStoreHelper.setLongUserProperty(
                        context, PlayStoreHelper.REMINDER_COUNT_USERPROPERTY, recordCount);
                // Nothing else to do
                break;
            case WATCHLISTS_MANAGEMENT_REQUEST:
                // Even if user tapped Cancel in Manage Watchlists screen he may have OK'd
                // changes in Edit Watchlist screen
                watchlistListPagerAdapter.notifyDataSetChanged();
                recordCount = this.dbHelper.getRecordCount(WatchlistContract.Watchlist.TABLE);
                PlayStoreHelper.setLongUserProperty(
                        context, PlayStoreHelper.WATCHLIST_COUNT_USERPROPERTY, recordCount);
                break;
            case SECURITIES_MANAGEMENT_REQUEST:
                recordCount = this.dbHelper.getRecordCount(SecurityContract.Security.TABLE);
                PlayStoreHelper.setLongUserProperty(
                        context, PlayStoreHelper.SECURITY_COUNT_USERPROPERTY, recordCount);
            case SECURITY_EDIT_REQUEST:
                if (resultCode == RESULT_OK) {
                    refreshAllWatchlists();
                }
                break;
            default:
                PlayStoreHelper.logAsError(
                        CLASS_NAME, String.format("%s(): unexpected requestCode = %d",
                        methodName, requestCode));
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    } // onActivityResult()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist_list);

        // Set app title as it may have an added timestamp indicating the last refresh
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(TITLE_INSTANCE_STATE);
        }
        if (TextUtils.isEmpty(title) == false) {
            setTitle(title);
        }

        // Without this the app's preferences will be empty until the user opens
        // its Settings screen for the 1st time
        boolean readAgain = false;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, readAgain);

        this.dbHelper = new DbHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set up the ViewPager with the watchlist adapter.
        watchlistListPagerAdapter
                = new WatchlistListPagerAdapter(getSupportFragmentManager(), dbHelper);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(watchlistListPagerAdapter);
        // Request user to whitelist app from Doze and App Standby
        ensureExemptionFromBatteryOptimizations();
        // Create initial quote refresh schedule (just overwrite existing ones)
        Log.d(CLASS_NAME, "onCreate(): creating quote refresh schedule");
        createQuoteRefreshSchedule();
        // Show banner ad in withAds flavor
        AdHelper.initialize(getApplicationContext());
        View adView = AdHelper.getAdView(getApplicationContext());
        if (adView != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.CENTER;
            adView.setLayoutParams(lp);
            LinearLayout ll = (LinearLayout) findViewById(R.id.main_linear_layout);
            ll.addView(adView);
        }
    } // onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_watchlist_list, menu);
        return true;
    }

    @Override
    public void onListFragmentInteraction(String symbol) {
        int actionId = 0;
        PlayStoreHelper.reportAction(getApplicationContext(), "Long tap security", actionId);
        Intent intent = new Intent(this, SecurityEditActivity.class);
        long securityId = dbHelper.getSecurityIdFromSymbol(symbol);
        intent.putExtra(SecurityEditActivity.SECURITY_ID_INTENT_EXTRA, securityId);
        startActivityForResult(intent, SECURITY_EDIT_REQUEST);
    } // onListFragmentInteraction()

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        int id = item.getItemId();
        PlayStoreHelper.reportAction(
                getApplicationContext(), item.getTitle().toString(), id);
        switch (id) {
            case R.id.action_export_database: {
                boolean isExport = true;
                copyDatabase(isExport);
                return true;
            }
            case R.id.action_help: {
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_import_database: {
                // Have user confirm overwrite of DB by import
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Overwrite existing database? No undo!")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        boolean isExport = false;
                                        copyDatabase(isExport);
                                    }
                                })
                        .setTitle("Overwrite DB?")
                        .show();
                return true;
            }
            case R.id.action_refresh: {
                boolean addTimestamp = false;
                updateTitle(addTimestamp);
                Context context = getApplicationContext();
                Intent service = new Intent(context, QuoteRefresherService.class);
                service.putExtra(
                        QuoteRefresherService.QUOTE_REFRESHER_BROADCAST_IS_MANUAL_REFRESH_INTENT_EXTRA, true);
                startService(service);
                return true;
            }
            case R.id.action_reminders_management:
                intent = new Intent(this, RemindersManagementActivity.class);
                startActivityForResult(intent, REMINDERS_MANAGEMENT_REQUEST);
                return true;
            case R.id.action_securities_management:
                intent = new Intent(this, SecuritiesManagementActivity.class);
                startActivityForResult(intent, SECURITIES_MANAGEMENT_REQUEST);
                return true;
            case R.id.action_watchlists_management: {
                intent = new Intent(this, WatchlistsManagementActivity.class);
                startActivityForResult(intent, WATCHLISTS_MANAGEMENT_REQUEST);
                return true;
            }
            case R.id.action_settings: {
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    } // onOptionsItemSelected()

    @Override
    protected void onPause() {
        // App goes to background - no need to update screen
        // And programmatically registered broadcast receivers don't receive broadcasts when the app
        // is paused anyway
        LocalBroadcastManager.getInstance(this).unregisterReceiver(quotesRefreshedBroadcastReceiver);
        Log.d(CLASS_NAME, "onPause(): quoteRefresherMessageReceiver unregistered");
        super.onPause();
    } // onPause()

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(quotesRefreshedBroadcastReceiver,
                new IntentFilter(QuoteRefresherService.QUOTE_REFRESHER_BROADCAST));
        Log.d(CLASS_NAME, "onResume(): quoteRefresherMessageReceiver registered");
        // Show possibly updated quotes when user returns to app
        refreshAllWatchlists();
    } // onResume()

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(TITLE_INSTANCE_STATE, title);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    } // onSaveInstanceState()

    private void refreshAllWatchlists() {
        final String methodName = "refreshAllWatchlists";
        Cursor watchlistsCursor = this.dbHelper.readAllWatchlists();
        try {
            final int watchListIdColumnIndex
                    = watchlistsCursor.getColumnIndex(WatchlistContract.Watchlist.ID);
            while (watchlistsCursor.moveToNext()) {
                long watchListId = watchlistsCursor.getLong(watchListIdColumnIndex);
                RecyclerView recyclerView = (RecyclerView) mViewPager.findViewWithTag(watchListId);
                if (recyclerView != null) {
                    WatchlistRecyclerViewAdapter adapter
                            = (WatchlistRecyclerViewAdapter) recyclerView.getAdapter();
                    Cursor quotesCursor = this.dbHelper.readAllQuotesForWatchlist(watchListId);
                    adapter.changeCursor(quotesCursor);
                    Log.v(CLASS_NAME, String.format(
                            "%s(): changed cursor for recyclerView with tag = %d",
                            methodName, watchListId));
                } else {
                    Log.v(CLASS_NAME, String.format(
                            "%s(): cannot find recyclerView with tag = %d",
                            methodName, watchListId));
                }
            }
        } finally {
            DbHelper.closeCursor(watchlistsCursor);
        }
    } // refreshAllWatchlists()

    private void updateTitle(boolean addTimestamp) {
        int resId = getApplicationContext().getApplicationInfo().labelRes;
        String appName = getApplicationContext().getString(resId);
        title = appName + (addTimestamp ? " @ " + getTime() : "");
        setTitle(title);
    } // updateTitle()
} // class WatchlistListActivity
