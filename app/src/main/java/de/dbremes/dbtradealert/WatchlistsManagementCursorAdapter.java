package de.dbremes.dbtradealert;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class WatchlistsManagementCursorAdapter extends CursorAdapter {
    public static final String WATCHLIST_DELETED_BROADCAST = "WatchlistDeletedBroadcast";
    DbHelper dbHelper;

    private View.OnClickListener deleteButtonClickListener = new View.OnClickListener() {

        public void onClick(final View v) {
            WatchlistsManagementDetailViewHolder holder
                    = (WatchlistsManagementDetailViewHolder) ((View) v.getParent()).getTag();
            String watchListName = holder.nameTextView.getText().toString();
            new AlertDialog.Builder(holder.context)
                    .setTitle("Delete?")
                    .setMessage(
                            String.format(
                                    "Delete watchlist '%s' and it's connections to securities?",
                                    watchListName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    WatchlistsManagementDetailViewHolder holder
                                            = (WatchlistsManagementDetailViewHolder) ((View) v
                                            .getParent()).getTag();
                                    long watchListId = holder.watchlistId;
                                    dbHelper.deleteWatchlist(watchListId);
                                    Intent intent = new Intent(WATCHLIST_DELETED_BROADCAST);
                                    LocalBroadcastManager.getInstance(holder.context)
                                            .sendBroadcast(intent);
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        } // onClick()

    }; // deleteButtonClickListener

    private View.OnClickListener editButtonClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            WatchlistsManagementDetailViewHolder holder
                    = (WatchlistsManagementDetailViewHolder) ((View) v.getParent()).getTag();
            long watchlistId = holder.watchlistId;
            Intent intent = new Intent(holder.context, WatchlistEditActivity.class);
            intent.putExtra(WatchlistEditActivity.WATCHLIST_ID_INTENT_EXTRA, watchlistId);
            ((Activity) holder.context).startActivityForResult(intent,
                    WatchlistEditActivity.UPDATE_WATCHLIST_REQUEST_CODE);
        }
    }; // editButtonClickListener

    public WatchlistsManagementCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.dbHelper = new DbHelper(context);
    } // ctor()

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        WatchlistsManagementDetailViewHolder holder
                = (WatchlistsManagementDetailViewHolder) view.getTag();
        holder.nameTextView.setText(cursor.getString(cursor
                .getColumnIndex(WatchlistContract.Watchlist.NAME)));
        holder.watchlistId = cursor.getLong(cursor
                .getColumnIndex(WatchlistContract.Watchlist.ID));
    } // bindView()

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.layout_watchlists_management_detail, null);
        // could replace WatchlistsManagementDetailViewHolder in ICS and above with
        // view.setTag(R.id.my_view, myView);
        WatchlistsManagementDetailViewHolder holder = new WatchlistsManagementDetailViewHolder();
        holder.context = context;
        holder.deleteButton = (Button) view.findViewById(R.id.deleteButton);
        holder.deleteButton.setOnClickListener(deleteButtonClickListener);
        holder.editButton = (Button) view.findViewById(R.id.editButton);
        holder.editButton.setOnClickListener(editButtonClickListener);
        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.watchlistId = cursor.getLong(cursor.getColumnIndex(WatchlistContract.Watchlist.ID));
        view.setTag(holder);
        return view;
    } // newView()


    private class WatchlistsManagementDetailViewHolder {
        public Context context;
        public Button deleteButton;
        public Button editButton;
        public TextView nameTextView;
        public long watchlistId;
    } // class WatchlistsManagementDetailViewHolder
} // class WatchlistsManagementCursorAdapter
