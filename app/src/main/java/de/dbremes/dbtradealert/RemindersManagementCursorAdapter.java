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
import de.dbremes.dbtradealert.DbAccess.ReminderContract;

public class RemindersManagementCursorAdapter extends CursorAdapter {
    public static final String REMINDER_DELETED_BROADCAST = "ReminderDeletedBroadcast";
    DbHelper dbHelper;

    private View.OnClickListener deleteButtonClickListener = new View.OnClickListener() {

        public void onClick(final View v) {
            RemindersManagementDetailViewHolder holder
                    = (RemindersManagementDetailViewHolder) ((View) v.getParent()).getTag();
            String reminderHeading = holder.headingTextView.getText().toString();
            new AlertDialog.Builder(holder.context)
                    .setTitle("Delete?")
                    .setMessage(
                            String.format("Delete reminder '%s'?", reminderHeading))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    RemindersManagementDetailViewHolder holder
                                            = (RemindersManagementDetailViewHolder) ((View) v
                                            .getParent()).getTag();
                                    long reminderId = holder.reminderId;
                                    dbHelper.deleteReminder(reminderId);
                                    // Inform RemindersManagementActivity so it can refresh
                                    // remindersListView
                                    Intent intent = new Intent(REMINDER_DELETED_BROADCAST);
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
            RemindersManagementDetailViewHolder holder
                    = (RemindersManagementDetailViewHolder) ((View) v.getParent()).getTag();
            Intent intent = new Intent(holder.context, ReminderEditActivity.class);
            intent.putExtra(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA, holder.reminderId);
            ((Activity) holder.context).startActivityForResult(intent,
                    ReminderEditActivity.UPDATE_REMINDER_REQUEST_CODE);
        }
    }; // editButtonClickListener

    public RemindersManagementCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.dbHelper = new DbHelper(context);
    } // ctor()

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RemindersManagementDetailViewHolder holder
                = (RemindersManagementDetailViewHolder) view.getTag();
        holder.headingTextView.setText(cursor.getString(cursor
                .getColumnIndex(ReminderContract.Reminder.HEADING)));
        holder.reminderId = cursor.getLong(cursor
                .getColumnIndex(ReminderContract.Reminder.ID));
    } // bindView()

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.layout_reminders_management_detail, null);
        // could replace RemindersManagementDetailViewHolder in ICS and above with
        // view.setTag(R.id.my_view, myView);
        RemindersManagementDetailViewHolder holder = new RemindersManagementDetailViewHolder();
        holder.context = context;
        holder.deleteButton = (Button) view.findViewById(R.id.deleteButton);
        holder.deleteButton.setOnClickListener(deleteButtonClickListener);
        holder.editButton = (Button) view.findViewById(R.id.editButton);
        holder.editButton.setOnClickListener(editButtonClickListener);
        holder.headingTextView = (TextView) view.findViewById(R.id.headingTextView);
        holder.reminderId = cursor.getLong(cursor.getColumnIndex(ReminderContract.Reminder.ID));
        view.setTag(holder);
        return view;
    } // newView()


    private class RemindersManagementDetailViewHolder {
        public Context context;
        public Button deleteButton;
        public Button editButton;
        public TextView headingTextView;
        public long reminderId;
    } // class RemindersManagementDetailViewHolder
}
