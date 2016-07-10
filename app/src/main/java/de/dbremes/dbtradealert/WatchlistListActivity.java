package de.dbremes.dbtradealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class WatchlistListActivity extends AppCompatActivity
        implements WatchlistFragment.OnListFragmentInteractionListener {
    private static final String APP_NAME = "DbTradeAlert";
    private static final String CLASS_NAME = "WatchlistListActivity";
    private static final int SECURITIES_MANAGEMENT_REQUEST = 1;
    private static final int WATCHLISTS_MANAGEMENT_REQUEST = 2;
    private DbHelper dbHelper = null;
    private WatchlistListPagerAdapter watchlistListPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private BroadcastReceiver quotesRefreshedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(QuoteRefresherService.BROADCAST_EXTRA_NAME);
            if (message.equals(QuoteRefresherService.BROADCAST_EXTRA_REFRESH_COMPLETED)) {
                Log.v("BroadcastReceiver",
                        "quotesRefreshedBroadcastReceiver triggered UI update");
                refreshAllWatchlists();
                setTitle(APP_NAME + " @ " + getTime());
            } else if (message.startsWith(QuoteRefresherService.BROADCAST_EXTRA_ERROR)) {
                Toast.makeText(WatchlistListActivity.this, message, Toast.LENGTH_SHORT).show();
                Log.e("BroadcastReceiver",
                        "quotesRefreshedBroadcastReceiver error = '" + message + "'");
            }
        }
    };

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
        switch (requestCode) {
            case WATCHLISTS_MANAGEMENT_REQUEST:
                // Even if user tapped Cancel in Manage Watchlists screen he may have OK'd
                // changes in Edit Watchlist screen
                watchlistListPagerAdapter.notifyDataSetChanged();
                break;
            case SECURITIES_MANAGEMENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    refreshAllWatchlists();
                }
                break;
            default:
                Log.e(CLASS_NAME, String.format("%s(): unexpected requestCode = %d",
                        methodName, requestCode));
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    } // onActivityResult()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist_list);

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
        startQuoteRefreshScheduleCreation();
    } // onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_watchlist_list, menu);
        return true;
    }

    @Override
    public void onListFragmentInteraction(String item) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                setTitle(APP_NAME);
                Context context = getApplicationContext();
                Intent service = new Intent(context, QuoteRefresherService.class);
                service.putExtra(QuoteRefresherService.INTENT_EXTRA_IS_MANUAL_REFRESH, true);
                startService(service);
                return true;
            }
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
                new IntentFilter(QuoteRefresherService.BROADCAST_ACTION_NAME));
        Log.d(CLASS_NAME, "onResume(): quoteRefresherMessageReceiver registered");
        // Show possibly updated quotes when user returns to app
        refreshAllWatchlists();
    } // onResume()

    private void refreshAllWatchlists() {
        final String methodName = "refreshAllWatchlists";
        Cursor watchlistsCursor = this.dbHelper.readAllWatchlists();
        final int watchListIdColumnIndex
                = watchlistsCursor.getColumnIndex(WatchlistContract.Watchlist.ID);
        while (watchlistsCursor.moveToNext()) {
            long watchListId = watchlistsCursor.getLong(watchListIdColumnIndex);
            RecyclerView recyclerView = (RecyclerView) mViewPager.findViewWithTag(watchListId);
            if (recyclerView != null) {
                WatchlistRecyclerViewAdapter adapter
                        = (WatchlistRecyclerViewAdapter) recyclerView.getAdapter();
                Cursor quotesCursor = this.dbHelper.readAllQuotesForWatchlist(watchListId);
                adapter.swapCursor(quotesCursor);
                Log.v(CLASS_NAME, String.format(
                        "%s(): changed cursor for recyclerView with tag = %d",
                        methodName, watchListId));
            } else {
                Log.v(CLASS_NAME, String.format(
                        "%s(): cannot find recyclerView with tag = %d",
                        methodName, watchListId));
            }
        }
        DbHelper.closeCursor(watchlistsCursor);
    } // refreshAllWatchlists()

    private void startQuoteRefreshScheduleCreation() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, QuoteRefreshScheduler.class);
        // LocalBroadcastManager won't work for statically registered receiver:
        //LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        sendBroadcast(intent);
    } // startQuoteRefreshScheduleCreation()
}
