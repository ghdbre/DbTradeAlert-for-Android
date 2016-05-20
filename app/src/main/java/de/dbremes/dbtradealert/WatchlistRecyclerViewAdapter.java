package de.dbremes.dbtradealert;

import android.database.Cursor;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.dbremes.dbtradealert.DbAccess.QuoteContract;
import de.dbremes.dbtradealert.DbAccess.SecurityContract;
import de.dbremes.dbtradealert.WatchlistFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class WatchlistRecyclerViewAdapter
        extends RecyclerView.Adapter<WatchlistRecyclerViewAdapter.ViewHolder> {
    // Avoid warning "logging tag can be at most 23 characters ..."
    private static final String CLASS_NAME = "WatchlistRec.ViewAd.";
    private final Cursor cursor;
    private final OnListFragmentInteractionListener listener;

    public WatchlistRecyclerViewAdapter(Cursor cursor, OnListFragmentInteractionListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    } // ctor()

    private boolean isLastTradeOlderThanOneDay(Cursor cursor) {
        boolean result = false;
        int columnIndex = cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE_DATE_TIME);
        String s = cursor.getString(columnIndex);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            Date lastTradeDateTime = format.parse(s);
            Date oneDayAgo = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
            if (lastTradeDateTime.before(oneDayAgo)) {
                result = true;
            }
        } catch (ParseException x) {
            // Assume null for missing time stamp
            Log.e(CLASS_NAME, "Exception caught", x);
        }
        return result;
    } // isLastTradeOlderThanOneDay()

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_quote, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int quotePosition) {
        if (this.cursor.moveToPosition(quotePosition) == false) {
            throw new IllegalStateException(
                    String.format(
                            "%s.%s: cannot move to position = %d; cursor.getCount() = %d",
                            WatchlistRecyclerViewAdapter.CLASS_NAME, "onBindViewHolder",
                            quotePosition, cursor.getCount()));
        }
        boolean isLastTradeOlderThanOneDay = this.isLastTradeOlderThanOneDay(cursor);
        float lastTrade = cursor.getFloat(
                cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE));
        float maxPrice = cursor.getFloat(
                cursor.getColumnIndex(SecurityContract.Security.MAX_PRICE));
        // LastPriceDateTimeTextView
        viewHolder.LastPriceDateTimeTextView.setText(cursor.getString(
                cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE_DATE_TIME)));
        if (isLastTradeOlderThanOneDay) {
            viewHolder.LastPriceDateTimeTextView.setBackgroundResource(R.color.colorWarn);
        } else {
            viewHolder.LastPriceDateTimeTextView
                    .setBackgroundColor(android.R.attr.editTextBackground);
        }
        // LastPriceTextView
        String currency = cursor.getString(
                cursor.getColumnIndex(QuoteContract.Quote.CURRENCY));
        viewHolder.LastPriceTextView.setText(
                String.format("%01.2f %s", lastTrade, currency));
        // PercentChangeMaxPriceTextView
        float percentChangeFromMaxPrice = (lastTrade - maxPrice) / maxPrice * 100;
        this.setPercentageText(
                true, percentChangeFromMaxPrice, " MH", viewHolder.PercentChangeMaxPriceTextView);
        // PercentChangeTextView
        float percentChange = cursor.getFloat(
                cursor.getColumnIndex(QuoteContract.Quote.PERCENT_CHANGE));
        this.setPercentageText(false, percentChange, "", viewHolder.PercentChangeTextView);
        // region PercentDailyVolumeTextView
        int averageDailyVolumeColumnIndex
                = cursor.getColumnIndex(QuoteContract.Quote.AVERAGE_DAILY_VOLUME);
        long averageDailyVolume = cursor.getLong(averageDailyVolumeColumnIndex);
        // n/a for indices
        if (averageDailyVolume > 0) {
            long volume = cursor.getLong(
                    cursor.getColumnIndex(QuoteContract.Quote.VOLUME));
            Float percentDailyVolume = (float) (volume * 100 / averageDailyVolume);
            viewHolder.PercentDailyVolumeTextView.setText(
                    String.format("%01.1f%% V", percentDailyVolume));
            if (percentDailyVolume == 0) {
                viewHolder.PercentDailyVolumeTextView.setBackgroundResource(R.color.colorWarn);
            } else {
                viewHolder.PercentDailyVolumeTextView
                        .setBackgroundColor(android.R.attr.editTextBackground);
            }
        }
        // endregion PercentDailyVolumeTextView
        // SecurityNameTextView
        viewHolder.SecurityNameTextView.setText(
                cursor.getString(cursor.getColumnIndex(
                        QuoteContract.Quote.NAME)));
        // region SignalTextView
        TextView signalTextView = viewHolder.SignalTextView;
        // If a trailing target is active, show an underscore
        boolean isTrailingTargetActive = cursor.isNull(cursor.getColumnIndex(
                SecurityContract.Security.TRAILING_TARGET)) == false;
        if (isTrailingTargetActive) {
            signalTextView.setPaintFlags(signalTextView.getPaintFlags()
                    | Paint.UNDERLINE_TEXT_FLAG);
            signalTextView.setText(" ");
        } else {
            signalTextView.setPaintFlags(signalTextView.getPaintFlags()
                    & (~Paint.UNDERLINE_TEXT_FLAG));
        }
        float trailingTargetPercentage = cursor.getFloat(cursor.getColumnIndex(
                SecurityContract.Security.TRAILING_TARGET));
        boolean isTrailingTargetReached
                = isTrailingTargetActive
                && lastTrade <= maxPrice * (100 - trailingTargetPercentage) / 100;
        // Lower target
        boolean isLowerTargetActive = cursor.isNull(cursor.getColumnIndex(
                SecurityContract.Security.LOWER_TARGET)) == false;
        float lowerTarget = cursor.getFloat(
                cursor.getColumnIndex(SecurityContract.Security.LOWER_TARGET));
        boolean isLowerTargetReached = isLowerTargetActive && lowerTarget >= lastTrade;
        // Upper target
        boolean isUpperTargetActive = cursor.isNull(cursor.getColumnIndex(
                SecurityContract.Security.UPPER_TARGET)) == false;
        float upperTarget
                = cursor.getFloat(cursor.getColumnIndex(SecurityContract.Security.UPPER_TARGET));
        boolean isUpperTargetReached = isUpperTargetActive && upperTarget <= lastTrade;
        if (isLowerTargetReached
                || isTrailingTargetReached
                || isUpperTargetReached) {
            if (isLowerTargetReached) {
                signalTextView.setText("L");
            }
            if (isTrailingTargetReached) {
                signalTextView.setText("T");
            }
            if (isUpperTargetReached) {
                signalTextView.setText("U");
                int resId = isLastTradeOlderThanOneDay
                        ? R.color.colorWarn
                        : R.color.colorWin;
                signalTextView.setBackgroundResource(resId);
            } else {
                int resId = isLastTradeOlderThanOneDay
                        ? R.color.colorWarn
                        : R.color.colorLoss;
                signalTextView.setBackgroundResource(resId);
            }
            signalTextView.setVisibility(View.VISIBLE);
        } else {
            signalTextView.setBackgroundColor(android.R.attr.editTextBackground);
            if (isTrailingTargetActive == false) {
                signalTextView.setVisibility(View.GONE);
            }
        }
        // endregion SignalTextView
        // Symbol
        viewHolder.Symbol = cursor.getString(
                cursor.getColumnIndex(SecurityContract.Security.SYMBOL));
        // SymbolTextView
        viewHolder.SymbolTextView.setText(cursor.getString(
                cursor.getColumnIndex(SecurityContract.Security.SYMBOL)));
        // setOnClickListener()
        viewHolder.View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(viewHolder.Symbol);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        int result = 0;
        if (this.cursor != null) {
            result = cursor.getCount();
        }
        return result;
    }

    private void setPercentageText(
            boolean isSmallText, float percentValue, String textToAdd, TextView textView) {
        String formatString = isSmallText ? "%01.1f%%%s" : "%01.2f %%%s";
        textView.setText(String.format(formatString, percentValue, textToAdd));
    } // setPercentageText()

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView LastPriceDateTimeTextView;
        public final TextView LastPriceTextView;
        public final TextView PercentChangeMaxPriceTextView;
        public final TextView PercentChangeTextView;
        public final TextView PercentDailyVolumeTextView;
        public final TextView SecurityNameTextView;
        public final TextView SignalTextView;
        public String Symbol;
        public final TextView SymbolTextView;
        public final View View;

        public ViewHolder(View view) {
            super(view);
            this.View = view;
            this.LastPriceDateTimeTextView
                    = (TextView) view.findViewById(R.id.lastPriceDateTimeTextView);
            this.LastPriceTextView = (TextView) view.findViewById(R.id.lastPriceTextView);
            this.PercentChangeMaxPriceTextView
                    = (TextView) view.findViewById(R.id.percentChangeMaxPriceTextView);
            this.PercentChangeTextView = (TextView) view.findViewById(R.id.percentChangeTextView);
            this.PercentDailyVolumeTextView
                    = (TextView) view.findViewById(R.id.percentDailyVolumeTextView);
            this.SecurityNameTextView = (TextView) view.findViewById(R.id.securityNameTextView);
            this.SignalTextView = (TextView) view.findViewById(R.id.signalTextView);
            this.SymbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + SymbolTextView.getText() + "'";
        }
    }
}
