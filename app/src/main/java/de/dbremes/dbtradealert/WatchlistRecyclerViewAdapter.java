package de.dbremes.dbtradealert;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.dbremes.dbtradealert.WatchlistFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class WatchlistRecyclerViewAdapter
        extends RecyclerView.Adapter<WatchlistRecyclerViewAdapter.ViewHolder> {

    private final List<String> mSymbols;
    private final OnListFragmentInteractionListener mListener;

    public WatchlistRecyclerViewAdapter(List<String> symbols, OnListFragmentInteractionListener listener) {
        mSymbols = symbols;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_quote, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int quotePosition) {
        holder.mSymbol = mSymbols.get(quotePosition);
        holder.mLastTradeTextView.setText(Integer.toString(quotePosition));
        holder.mSymbolTextView.setText(mSymbols.get(quotePosition));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mSymbol);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSymbols.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mLastTradeTextView;
        public final TextView mSymbolTextView;
        public String mSymbol;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLastTradeTextView = (TextView) view.findViewById(R.id.lastTradeTextView);
            mSymbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSymbolTextView.getText() + "'";
        }
    }
}
