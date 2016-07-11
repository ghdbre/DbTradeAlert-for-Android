package de.dbremes.dbtradealert;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.ReminderContract;

public class RemindersManagementActivity extends AppCompatActivity {
    private final static String CLASS_NAME = "RemindersManagementAct.";
    private Cursor cursor;
    private DbHelper dbHelper;
    private RemindersManagementCursorAdapter remindersManagementCursorAdapter;

    private BroadcastReceiver reminderDeletedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    RemindersManagementCursorAdapter.REMINDER_DELETED_BROADCAST)) {
                refreshRemindersListView();
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    long reminderId = extras.getLong(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA);
                    // Need to pass reminderId as int because notificationManager.cancel()
                    // doesn't work with long
                    removeNotificationForDeletedReminder((int) reminderId);
                }
            }
        }
    }; // reminderDeletedBroadcastReceiver

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshRemindersListView();
            if (requestCode == ReminderEditActivity.UPDATE_REMINDER_REQUEST_CODE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    long reminderId = extras.getLong(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA);
                    // Need to pass reminderId as int because notificationManager.cancel()
                    // doesn't work with long
                    removeNotificationForUpdatedReminder((int) reminderId);
                }
            }
        }
    } // onActivityResult()

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_management);
        setTitle("Manage Reminders");
        // Connect Cursor to CursorAdapter and ListView to CursorAdapter
        this.dbHelper = new DbHelper(this);
        this.cursor = dbHelper.readAllReminders();
        this.remindersManagementCursorAdapter
                = new RemindersManagementCursorAdapter(this, this.cursor, false);
        ListView remindersListView = (ListView) findViewById(R.id.remindersListView);
        TextView emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        remindersListView.setEmptyView(emptyTextView);
        remindersListView.setAdapter(remindersManagementCursorAdapter);
    } // onCreate()

    public void onOkButtonClick(View view) {
        setResult(RESULT_OK, getIntent());
        finish();
    } // onOkButtonClick()

    public void onNewButtonClick(View view) {
        Intent intent = new Intent(this, ReminderEditActivity.class);
        intent.putExtra(ReminderEditActivity.REMINDER_ID_INTENT_EXTRA, DbHelper.NEW_ITEM_ID);
        startActivityForResult(intent, ReminderEditActivity.CREATE_REMINDER_REQUEST_CODE);
    } // onNewButtonClick()

    @Override
    public void onPause() {
        super.onPause();
        // Unregister broadcast receiver for REMINDER_DELETED_BROADCAST
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(reminderDeletedBroadcastReceiver);
    } // onPause()

    @Override
    public void onResume() {
        super.onResume();
        // Register broadcast receiver for REMINDER_DELETED_BROADCAST
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RemindersManagementCursorAdapter.REMINDER_DELETED_BROADCAST);
        broadcastManager.registerReceiver(reminderDeletedBroadcastReceiver, intentFilter);
    } // onResume()

    private void refreshRemindersListView() {
        Cursor cursor = this.dbHelper.readAllReminders();
        this.remindersManagementCursorAdapter.changeCursor(cursor);
    } // refreshRemindersListView()

    private void removeNotificationForDeletedReminder(int reminderId) {
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(reminderId);
    } // removeNotificationForDeletedReminder()

    private void removeNotificationForUpdatedReminder(int reminderId) {
        Cursor cursor = this.dbHelper.readReminder(reminderId);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            // Check if still due
            boolean includeTime = false;
            String dueDateString = Utils.getDateTimeStringFromDbDateTime(cursor,
                    cursor.getColumnIndex(ReminderContract.Reminder.DUE_DATE), includeTime);
            SimpleDateFormat sdf
                    = (SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.SHORT);
            Date dueDate = null;
            try {
                dueDate = sdf.parse(dueDateString);
            } catch (ParseException e) {
                Log.e(CLASS_NAME, Utils.EXCEPTION_CAUGHT, e);
            }
            Date today = new Date();
            boolean isReminderDue = today.after(dueDate);
            // Check if still active
            boolean isReminderActive
                    = cursor.getInt(cursor.getColumnIndex(ReminderContract.Reminder.IS_ACTIVE)) == 1;
            if (isReminderDue == false || isReminderActive == false) {
                NotificationManager notificationManager
                        = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reminderId);
            }
        }
        // dbHelper will log an error if readReminder() found more or less reminders
    } // removeNotificationForUpdatedReminder()
} // class RemindersManagementActivity
