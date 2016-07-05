package de.dbremes.dbtradealert;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class WatchlistManagementCursorAdapter extends CursorAdapter {
    private Context context;
    DbHelper dbHelper;

    public WatchlistManagementCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.context = context;
        this.dbHelper = new DbHelper(this.context);
    } // ctor()

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        WatchListManagementDetailViewHolder holder
                = (WatchListManagementDetailViewHolder) view.getTag();
        holder.nameTextView.setText(cursor.getString(cursor
                .getColumnIndex(WatchlistContract.Watchlist.NAME)));
        holder.watchListId = cursor.getLong(cursor
                .getColumnIndex(WatchlistContract.Watchlist.ID));
    } // bindView()

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.layout_watchlists_management_detail, null);
        // could replace WatchListManagementDetailViewHolder in ICS and above with
        // view.setTag(R.id.my_view, myView);
        WatchListManagementDetailViewHolder holder = new WatchListManagementDetailViewHolder();
        holder.deleteButton = (Button) view.findViewById(R.id.deleteButton);
        //holder.deleteButton.setOnClickListener(deleteButtonClickListener);
        holder.editButton = (Button) view.findViewById(R.id.editButton);
        //holder.editButton.setOnClickListener(editButtonClickListener);
        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.watchListId = cursor.getLong(cursor.getColumnIndex(WatchlistContract.Watchlist.ID));
        view.setTag(holder);
        return view;
    } // newView()


    private class WatchListManagementDetailViewHolder {
        public Button deleteButton;
        public Button editButton;
        public TextView nameTextView;
        public long watchListId;
    } // class WatchListManagementDetailViewHolder
} // class WatchlistManagementCursorAdapter
