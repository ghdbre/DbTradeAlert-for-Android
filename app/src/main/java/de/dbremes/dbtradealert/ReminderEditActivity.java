package de.dbremes.dbtradealert;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;

import de.dbremes.dbtradealert.DbAccess.DbHelper;
import de.dbremes.dbtradealert.DbAccess.ReminderContract;

public class ReminderEditActivity extends AppCompatActivity {
    private final static String CLASS_NAME = "ReminderEditActivity";
    public final static int CREATE_REMINDER_REQUEST_CODE = 30;
    public final static int UPDATE_REMINDER_REQUEST_CODE = 31;
    public final static String REMINDER_ID_INTENT_EXTRA = "de.dbremes.dbtradealert.reminderId";
    private DbHelper dbHelper;
    private long reminderId = DbHelper.NewItemId;

    private void clearEditTextViews() {
        EditText editText = null;
        editText = (EditText) findViewById(R.id.dueDateEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.headingEditText);
        editText.setText("");
        editText = (EditText) findViewById(R.id.notesEditText);
        editText.setText("");
    } // clearEditTextViews()

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    } // onCancelButtonClick()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);
        this.dbHelper = new DbHelper(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.reminderId = extras.getLong(REMINDER_ID_INTENT_EXTRA);
            if (this.reminderId == DbHelper.NewItemId) {
                // Create mode
                clearEditTextViews();
                setTitle("Create Reminder");
            } else {
                // Update mode
                showReminderData(this.reminderId);
                setTitle("Edit Reminder");
            }
        }
    } // onCreate()

    public void onOkButtonClick(View view) {
        String errorMessage = "";
        Date dueDate = null;
        try {
            dueDate = Utils.getDateFromEditText(this, R.id.dueDateEditText);
        } catch (ParseException e) {
            errorMessage = e.getMessage();
        }
        if (dueDate == null && TextUtils.isEmpty(errorMessage)) {
            errorMessage = "Please enter a due date";
        }
        String heading = Utils.getStringFromEditText(this, R.id.headingEditText);
        if (TextUtils.isEmpty(heading)) {
            errorMessage = "Please enter a heading";
        }
        CheckBox isReminderActiveCheckBox
                = (CheckBox) findViewById(R.id.isReminderActiveCheckBox);
        boolean isReminderActive = isReminderActiveCheckBox.isChecked();
        String notes = Utils.getStringFromEditText(this, R.id.notesEditText);
        if (TextUtils.isEmpty(errorMessage)) {
            this.dbHelper.updateOrCreateReminder(
                    dueDate, heading, isReminderActive, notes, this.reminderId);
            setResult(RESULT_OK, getIntent());
            finish();
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    } // onOkButtonClick()

    private void setCheckedFromIntegerColumn(Cursor cursor, String columnName, Integer checkBoxId) {
        boolean isTrue = cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
        CheckBox checkBox = (CheckBox) findViewById(checkBoxId);
        checkBox.setChecked(isTrue);
    } // setCheckedFromIntegerColumn()

    private void showReminderData(long reminderId) {
        final String methodName = "showReminderData";
        Cursor reminderCursor = this.dbHelper.readReminder(reminderId);
        if (reminderCursor.getCount() == 1) {
            reminderCursor.moveToFirst();
            Utils.setTextFromDateColumn(this, reminderCursor,
                    ReminderContract.Reminder.DUE_DATE, R.id.dueDateEditText);
            Utils.setTextFromStringColumn(this, reminderCursor,
                    ReminderContract.Reminder.HEADING, R.id.headingEditText);
            setCheckedFromIntegerColumn(
                    reminderCursor, ReminderContract.Reminder.IS_ACTIVE,
                    R.id.isReminderActiveCheckBox);
            Utils.setTextFromStringColumn(this, reminderCursor,
                    ReminderContract.Reminder.NOTES, R.id.notesEditText);
        } else {
            PlayStoreHelper.logAsError(CLASS_NAME,
                    String.format(
                            "%s: readReminder() found %d reminders with id = %d; expected 1!",
                            methodName, reminderCursor.getCount(), reminderId));
        }
    } // showReminderData()

} // class ReminderEditActivity
