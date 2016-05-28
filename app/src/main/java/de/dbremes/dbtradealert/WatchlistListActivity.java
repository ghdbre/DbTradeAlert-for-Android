package de.dbremes.dbtradealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class WatchlistListActivity extends AppCompatActivity
        implements WatchlistFragment.OnListFragmentInteractionListener {
    private static final String APP_NAME = "DbTradeAlert";
    private static final String CLASS_NAME = "WatchlistListActivity";
    private DbHelper dbHelper = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private WatchlistListPagerAdapter mWatchlistListPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private BroadcastReceiver quoteRefresherMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(QuoteRefresherAsyncTask.BROADCAST_EXTRA_NAME);
            if (message.equals(QuoteRefresherAsyncTask.BROADCAST_EXTRA_REFRESH_COMPLETED)) {
                Log.d("BroadcastReceiver",
                        "quoteRefresherMessageReceiver triggered UI update");
                refreshAllWatchLists();
                setTitle(APP_NAME + " @ " + getTime());
            }
            else if (message.startsWith(QuoteRefresherAsyncTask.BROADCAST_EXTRA_ERROR)) {
                Toast.makeText(WatchlistListActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            Log.d("BroadcastReceiver",
                    "quoteRefresherMessageReceiver message = '" + message + "'");
        }
    };

    private String getTime() {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        // Seems not to be necessary:
        //sdf.setTimeZone(TimeZone.getDefault());
        result = sdf.format(new Date());
        return result;
    } // getTime()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist_list);

        this.dbHelper =  new DbHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each watchlist of the activity.
        mWatchlistListPagerAdapter
                = new WatchlistListPagerAdapter(getSupportFragmentManager(), dbHelper);

        // Set up the ViewPager with the watchlist adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mWatchlistListPagerAdapter);
    }

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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                setTitle(APP_NAME);
                Context context = getApplicationContext();
                new QuoteRefresherAsyncTask().execute(context);
                return true;
            }
            case R.id.action_settings: {
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(quoteRefresherMessageReceiver);
        Log.d(CLASS_NAME, "onPause(): quoteRefresherMessageReceiver unregistered");
        super.onPause();
    } // onPause()

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(quoteRefresherMessageReceiver,
                new IntentFilter(QuoteRefresherAsyncTask.BROADCAST_ACTION_NAME));
        Log.d(CLASS_NAME, "onResume(): quoteRefresherMessageReceiver registered");
    } // onResume()

    private void refreshAllWatchLists() {
        final String methodName = "refreshAllWatchLists";
        Cursor cursor = this.dbHelper.readAllWatchlists();
        final int watchListIdColumnIndex = cursor.getColumnIndex(WatchlistContract.Watchlist.ID);
        while (cursor.moveToNext()) {
            long watchListId = cursor.getLong(watchListIdColumnIndex);
            RecyclerView recyclerView = (RecyclerView) mViewPager.findViewWithTag(watchListId);
            if (recyclerView != null) {
                WatchlistRecyclerViewAdapter adapter
                        = (WatchlistRecyclerViewAdapter) recyclerView.getAdapter();
                Cursor quotesCursor = this.dbHelper.readAllQuotesForWatchlist(watchListId);
                adapter.swapCursor(quotesCursor);
                Log.d(CLASS_NAME, String.format(
                        "%s: changed cursor for recyclerView with tag = %d",
                        methodName, watchListId));
            } else {
                Log.d(CLASS_NAME, String.format(
                        "%s: cannot find recyclerView with tag = %d",
                        methodName, watchListId));
            }
        }
    } // refreshAllWatchLists()
}
