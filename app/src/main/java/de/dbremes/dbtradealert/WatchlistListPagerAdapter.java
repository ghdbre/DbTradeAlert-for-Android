package de.dbremes.dbtradealert;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the watchlists.
 */
public class WatchlistListPagerAdapter extends FragmentPagerAdapter {
    // Logging tag can be at most 23 characters
    private static final String CLASS_NAME = "WatchlistListPagerAd.";

    private final DbHelper dbHelper;

    public WatchlistListPagerAdapter(FragmentManager fm, DbHelper dbHelper) {
        super(fm);
        this.dbHelper = dbHelper;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a WatchlistFragment.
        return WatchlistFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        int result = -1;
        Cursor cursor = dbHelper.readAllWatchlists();
        result = cursor.getCount();
        cursor.close();
        return result;
    } // getCount()

    @Override
    public CharSequence getPageTitle(int position) {
        String result = "";
        final String methodName = "getPageTitle";
        Log.v(CLASS_NAME,
                String.format("%s: position = %d", methodName, position));
        Cursor cursor = dbHelper.readAllWatchlists();
        if (cursor.getCount() >= position) {
            cursor.moveToPosition(position);
            result = cursor.getString(cursor
                    .getColumnIndex(WatchlistContract.Watchlist.NAME));
        } else {
            Log.w(CLASS_NAME, String.format(
                    "%s: cannot move to position = %d; cursor.getCount() = %d",
                    methodName, position, cursor.getCount()));
        }
        cursor.close();
        return result;
    } // getPageTitle()
}
