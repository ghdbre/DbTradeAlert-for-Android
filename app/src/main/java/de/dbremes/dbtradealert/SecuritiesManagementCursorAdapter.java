package de.dbremes.dbtradealert;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.QuoteContract;
import de.dbremes.dbtradealert.DbAccess.SecurityContract;
import de.dbremes.dbtradealert.DbAccess.WatchlistContract;

public class SecuritiesManagementCursorAdapter extends CursorAdapter {
    public static final String SECURITY_DELETED_BROADCAST = "SecurityDeletedBroadcast";
    DbHelper dbHelper;

    public SecuritiesManagementCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.dbHelper = new DbHelper(context);
    } // ctor()

    private View.OnClickListener deleteButtonClickListener = new View.OnClickListener() {

        public void onClick(final View v) {
            SecuritiesManagementDetailViewHolder holder
                    = (SecuritiesManagementDetailViewHolder) ((View) v.getParent()).getTag();
            String securityName = holder.nameTextView.getText().toString();
            String securitySymbol = holder.symbolTextView.getText().toString();
            new AlertDialog.Builder(holder.context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(
                            String.format(
                                    "Delete %s ('%s'), it's quotes and connections to watchlists?",
                                    securitySymbol, securityName))
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    SecuritiesManagementDetailViewHolder holder
                                            = (SecuritiesManagementDetailViewHolder) ((View) v
                                            .getParent()).getTag();
                                    long securityId = holder.securityId;
                                    dbHelper.deleteSecurity(securityId);
                                    // Inform SecuritiesManagementActivity so it can refresh
                                    // securitiesListView
                                    Intent intent = new Intent(SECURITY_DELETED_BROADCAST);
                                    LocalBroadcastManager.getInstance(holder.context)
                                            .sendBroadcast(intent);
                                }
                            })
                    .setTitle("Delete?")
                    .show();
        } // onClick()

    }; // deleteButtonClickListener

    private View.OnClickListener editButtonClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            SecuritiesManagementDetailViewHolder holder
                    = (SecuritiesManagementDetailViewHolder) ((View) v.getParent()).getTag();
            long securityId = holder.securityId;
            Intent intent = new Intent(holder.context, SecurityEditActivity.class);
            intent.putExtra(SecurityEditActivity.SECURITY_ID_INTENT_EXTRA, securityId);
            ((Activity) holder.context).startActivityForResult(intent,
                    SecurityEditActivity.UPDATE_SECURITY_REQUEST_CODE);
        }
    }; // editButtonClickListener

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SecuritiesManagementDetailViewHolder holder
                = (SecuritiesManagementDetailViewHolder) view.getTag();
        holder.nameTextView.setText(cursor.getString(cursor
                .getColumnIndex(QuoteContract.Quote.NAME)));
        holder.securityId = cursor.getLong(cursor
                .getColumnIndex(SecurityContract.Security.ID));
        holder.symbolTextView.setText(cursor.getString(cursor
                .getColumnIndex(SecurityContract.Security.SYMBOL)));
    } // bindView()

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.layout_securities_management_detail, null);
        // could replace SecuritiesManagementDetailViewHolder in ICS and above with
        // view.setTag(R.id.my_view, myView);
        SecuritiesManagementDetailViewHolder holder = new SecuritiesManagementDetailViewHolder();
        holder.context = context;
        holder.deleteButton = (Button) view.findViewById(R.id.deleteButton);
        holder.deleteButton.setOnClickListener(deleteButtonClickListener);
        holder.editButton = (Button) view.findViewById(R.id.editButton);
        holder.editButton.setOnClickListener(editButtonClickListener);
        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.securityId = cursor.getLong(cursor.getColumnIndex(SecurityContract.Security.ID));
        holder.symbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
        view.setTag(holder);
        return view;
    } // newView()

    private class SecuritiesManagementDetailViewHolder {
        public Context context;
        public Button deleteButton;
        public Button editButton;
        public TextView nameTextView;
        public long securityId;
        public TextView symbolTextView;
    } // class SecuritiesManagementDetailViewHolder
} // class SecuritiesManagementCursorAdapter
