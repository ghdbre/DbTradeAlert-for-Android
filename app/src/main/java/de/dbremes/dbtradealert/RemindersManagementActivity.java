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
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class RemindersManagementActivity extends AppCompatActivity {
    public final static String REMINDER_ID_INTENT_EXTRA = "de.dbremes.dbtradealert.reminderId";
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
                    long reminderId = extras.getLong(REMINDER_ID_INTENT_EXTRA);
                    // Need to pass reminderId as int because notificationManager.cancel()
                    // doesn't work with long
                    removeNotificationForDeletedReminder((int) reminderId);
                }
            }
        }
    }; // reminderDeletedBroadcastReceiver

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
} // class RemindersManagementActivity
