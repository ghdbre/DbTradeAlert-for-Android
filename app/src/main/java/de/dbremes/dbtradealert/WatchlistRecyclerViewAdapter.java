package de.dbremes.dbtradealert;

import android.database.Cursor;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
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
    private final DbHelper.Extremes quoteExtremes;
    private final DbHelper.Extremes targetExtremes;
    private Cursor cursor;
    private final OnListFragmentInteractionListener listener;

    public WatchlistRecyclerViewAdapter(
            Cursor cursor, DbHelper.Extremes quoteExtremes, DbHelper.Extremes targetExtremes,
            OnListFragmentInteractionListener listener) {
        this.cursor = cursor;
        this.quoteExtremes = quoteExtremes;
        this.targetExtremes = targetExtremes;
        this.listener = listener;
    } // ctor()

    private boolean isLastTradeOlderThanOneDay(Cursor cursor) {
        boolean result = false;
        int columnIndex = cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE_DATE_TIME);
        String s = cursor.getString(columnIndex);
        SimpleDateFormat dbFormat = new SimpleDateFormat(DbHelper.DATE_TIME_FORMAT_STRING);
        try {
            Date lastTradeDateTime = dbFormat.parse(s);
            Date oneDayAgo = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
            if (lastTradeDateTime.before(oneDayAgo)) {
                result = true;
            }
        } catch (ParseException x) {
            // Assume null for missing time stamp
            PlayStoreHelper.logAsDebugMessage(x);
        }
        return result;
    } // isLastTradeOlderThanOneDay()

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int reportPosition) {
        if (this.cursor.moveToPosition(reportPosition) == false) {
            throw new IllegalStateException(
                    String.format(
                            "%s.%s: cannot move to position = %d; cursor.getCount() = %d",
                            WatchlistRecyclerViewAdapter.CLASS_NAME, "onBindViewHolder",
                            reportPosition, this.cursor.getCount()));
        }
        boolean isLastTradeOlderThanOneDay = this.isLastTradeOlderThanOneDay(this.cursor);
        // lastPrice is guaranteed to be not null because DbTradeAlert ignores data without
        // lastPrice specified -> lastPrice can use float variable (small f).
        // Other float values in the DB may be null. As cursor.getFloat() will return 0.0 for
        // those we need to use Float variables (capital F), initialize them with Float.NaN
        // and only copy the DB values if cursor.isNull() returns false for the respective field.
        // Drawbacks of Float are additional boxing and garbage collection.
        // Conditional formatting like background color needs to be reset when the condition
        // isn't met because of Android's view reuse.
        float lastPrice = this.cursor.getFloat(
                this.cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE));
        // LastPriceDateTimeTextView
        int columnIndex = cursor.getColumnIndex(QuoteContract.Quote.LAST_PRICE_DATE_TIME);
        String s = Utils.getDateTimeStringFromDbDateTime(cursor, columnIndex, true);
        viewHolder.LastPriceDateTimeTextView.setText(s);
        if (isLastTradeOlderThanOneDay) {
            viewHolder.LastPriceDateTimeTextView.setBackgroundResource(R.color.colorWarn);
        } else {
            resetTextViewBackground(viewHolder.LastPriceDateTimeTextView);
        }
        // LastPriceTextView
        String currency = this.cursor.getString(
                this.cursor.getColumnIndex(QuoteContract.Quote.CURRENCY));
        viewHolder.LastPriceTextView.setText(
                String.format("%01.2f %s", lastPrice, currency));
        // PercentChangeFromMaxPriceTextView
        Float maxPrice
                = Utils.readFloatRespectingNull(SecurityContract.Security.MAX_PRICE, this.cursor);
        if (maxPrice.isNaN() == false) {
            float percentChangeFromMaxPrice = (lastPrice - maxPrice) / maxPrice * 100;
            this.setPercentageText(true, percentChangeFromMaxPrice,
                    " MH", viewHolder.PercentChangeFromMaxPriceTextView);
        } else {
            viewHolder.PercentChangeFromMaxPriceTextView.setText("- MH");
        }
        // PercentChangeTextView
        Float percentChange
                = Utils.readFloatRespectingNull(QuoteContract.Quote.PERCENT_CHANGE, this.cursor);
        if (percentChange.isNaN() == false) {
            this.setPercentageText(false, percentChange, "", viewHolder.PercentChangeTextView);
        } else {
            viewHolder.PercentChangeTextView.setText("-");
        }
        // region PercentDailyVolumeTextView
        int averageDailyVolumeColumnIndex
                = this.cursor.getColumnIndex(QuoteContract.Quote.AVERAGE_DAILY_VOLUME);
        long averageDailyVolume = this.cursor.getLong(averageDailyVolumeColumnIndex);
        // n/a for indices
        if (averageDailyVolume > 0) {
            long volume = this.cursor.getLong(
                    this.cursor.getColumnIndex(QuoteContract.Quote.VOLUME));
            float percentDailyVolume = (float) (volume * 100 / averageDailyVolume);
            viewHolder.PercentDailyVolumeTextView.setText(
                    String.format("%01.1f%% V", percentDailyVolume));
            if (percentDailyVolume == 0) {
                viewHolder.PercentDailyVolumeTextView.setBackgroundResource(R.color.colorWarn);
            } else {
                resetTextViewBackground(viewHolder.PercentDailyVolumeTextView);
            }
        }
        // endregion PercentDailyVolumeTextView
        // region ReportChartView
        Float ask = Utils.readFloatRespectingNull(QuoteContract.Quote.ASK, this.cursor);
        Float basePrice
                = Utils.readFloatRespectingNull(SecurityContract.Security.BASE_PRICE, this.cursor);
        Float bid = Utils.readFloatRespectingNull(QuoteContract.Quote.BID, this.cursor);
        Float daysHigh = Utils.readFloatRespectingNull(QuoteContract.Quote.DAYS_HIGH, this.cursor);
        Float daysLow = Utils.readFloatRespectingNull(QuoteContract.Quote.DAYS_LOW, this.cursor);
        Float lowerTarget = Utils.readFloatRespectingNull(
                SecurityContract.Security.LOWER_TARGET, this.cursor);
        Float open = Utils.readFloatRespectingNull(QuoteContract.Quote.OPEN, this.cursor);
        Float previousClose = Utils.readFloatRespectingNull(
                QuoteContract.Quote.PREVIOUS_CLOSE, this.cursor);
        Float trailingTarget = Utils.readFloatRespectingNull(
                SecurityContract.Security.TRAILING_TARGET, this.cursor);
        Float upperTarget = Utils.readFloatRespectingNull(
                SecurityContract.Security.UPPER_TARGET, this.cursor);
        viewHolder.ReportChartView.setValues(this.quoteExtremes, this.targetExtremes,
                ask, basePrice, bid, daysHigh, daysLow, lastPrice, lowerTarget, maxPrice, open,
                previousClose, trailingTarget, upperTarget);
        // endregion ReportChartView
        // SecurityNameTextView
        viewHolder.SecurityNameTextView.setText(
                this.cursor.getString(this.cursor.getColumnIndex(
                        QuoteContract.Quote.NAME)));
        // region SignalTextView
        TextView signalTextView = viewHolder.SignalTextView;
        // If a trailing target is used, show an underscore
        Float trailingTargetPercentage = Utils.readFloatRespectingNull(
                SecurityContract.Security.TRAILING_TARGET, this.cursor);
        if (trailingTargetPercentage.isNaN() == false) {
            signalTextView.setPaintFlags(signalTextView.getPaintFlags()
                    | Paint.UNDERLINE_TEXT_FLAG);
            signalTextView.setText("  ");
        } else {
            signalTextView.setPaintFlags(signalTextView.getPaintFlags()
                    & ~Paint.UNDERLINE_TEXT_FLAG);
        }
        boolean isTrailingTargetReached
                = trailingTargetPercentage.isNaN() == false
                && lastPrice <= maxPrice * (100 - trailingTargetPercentage) / 100;
        // Lower target
        boolean isLowerTargetReached = lowerTarget.isNaN() == false && lowerTarget >= lastPrice;
        // Upper target
        boolean isUpperTargetReached = upperTarget.isNaN() == false && upperTarget <= lastPrice;
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
            resetTextViewBackground(signalTextView);
            if (trailingTargetPercentage.isNaN()) {
                signalTextView.setVisibility(View.GONE);
            }
        }
        // endregion SignalTextView
        // Symbol
        viewHolder.Symbol = this.cursor.getString(
                this.cursor.getColumnIndex(SecurityContract.Security.SYMBOL));
        // SymbolTextView
        viewHolder.SymbolTextView.setText(this.cursor.getString(
                this.cursor.getColumnIndex(SecurityContract.Security.SYMBOL)));
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

    @SuppressWarnings("deprecation")
    private void resetTextViewBackground(TextView textView) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackgroundDrawable(null);
        } else {
            textView.setBackground(null);
        }
    } // resetTextViewBackground()

    private void setPercentageText(
            boolean isSmallText, float percentValue, String textToAdd, TextView textView) {
        String formatString = isSmallText ? "%01.1f%%%s" : "%01.2f %%%s";
        textView.setText(String.format(formatString, percentValue, textToAdd));
    } // setPercentageText()

    public void changeCursor(Cursor newCursor) {
        if (newCursor != this.cursor) {
            DbHelper.closeCursor(this.cursor);
            this.cursor = newCursor;
            notifyDataSetChanged();
        }
    } // changeCursor()

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView LastPriceDateTimeTextView;
        public final TextView LastPriceTextView;
        public final TextView PercentChangeFromMaxPriceTextView;
        public final TextView PercentChangeTextView;
        public final TextView PercentDailyVolumeTextView;
        public final ReportChartView ReportChartView;
        public final TextView SecurityNameTextView;
        public final TextView SignalTextView;
        // Symbol needed for View's onClickListener
        public String Symbol;
        public final TextView SymbolTextView;
        public final View View;

        public ViewHolder(View view) {
            super(view);
            this.View = view;
            this.LastPriceDateTimeTextView
                    = (TextView) view.findViewById(R.id.lastPriceDateTimeTextView);
            this.LastPriceTextView = (TextView) view.findViewById(R.id.lastPriceTextView);
            this.PercentChangeFromMaxPriceTextView
                    = (TextView) view.findViewById(R.id.percentChangeFromMaxPriceTextView);
            this.PercentChangeTextView = (TextView) view.findViewById(R.id.percentChangeTextView);
            this.PercentDailyVolumeTextView
                    = (TextView) view.findViewById(R.id.percentDailyVolumeTextView);
            this.ReportChartView = (ReportChartView) view.findViewById(R.id.reportChartView);
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
