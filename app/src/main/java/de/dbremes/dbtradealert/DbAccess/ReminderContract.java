package de.dbremes.dbtradealert.DbAccess;

import android.provider.BaseColumns;

public class ReminderContract {
    // Empty constructor prevents accidentally instantiating the contract class
    public ReminderContract() {}

    // Inner class defines column names
    public static abstract class Reminder {
        public final static String TABLE = "reminder";
        public final static String DUE_DATE = "due_date";
        public final static String HEADING = "heading";
        public final static String ID = BaseColumns._ID;
        public final static String IS_ACTIVE = "is_active";
        public final static String NOTES = "notes";
    }}
