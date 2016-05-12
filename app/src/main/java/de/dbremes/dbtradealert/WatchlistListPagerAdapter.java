package de.dbremes.dbtradealert;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the watchlists.
 */
public class WatchlistListPagerAdapter extends FragmentPagerAdapter {

    public WatchlistListPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a QuoteFragment.
        return QuoteFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 2 total watchlists.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "CH Stocks";
            case 1:
                return "D Stocks";
        }
        return null;
    }
}
