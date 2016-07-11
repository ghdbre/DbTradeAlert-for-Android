package de.dbremes.dbtradealert;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class Utils {
    private final static String CLASS_NAME = "Utils";
    public static final String EXCEPTION_CAUGHT = "Exception caught";

    public static Date getDateFromEditText(Activity activity, Integer editTextId)
            throws ParseException {
        Date result = null;
        EditText editText = (EditText) activity.findViewById(editTextId);
        if (editText.length() > 0) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
            // Without setLenient(false) DateFormat would accept weird date inputs. For example
            // "53.2.16" -> "Thu Mar 24 00:00:00 GMT+01:00 2016"
            // ("correctly" as February 2016 has 29 days :-))
            dateFormat.setLenient(false);
            String text = editText.getText().toString();
            result = dateFormat.parse(text);
        }
        return result;
    } // getDateFromEditText()

    private static String getDateTimeStringFromDbDateTime(
            Cursor cursor, int columnIndex, boolean includeTime) {
        // Return something so missing time stamps can be marked
        String result = "";
        String dateTimeString = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(dateTimeString) == false) {
            SimpleDateFormat databaseFormat;
            SimpleDateFormat localFormat;
            if (includeTime) {
                databaseFormat = new SimpleDateFormat(
                        DbHelper.DATE_TIME_FORMAT_STRING, Locale.getDefault());
                localFormat = (SimpleDateFormat) SimpleDateFormat
                        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            } else {
                databaseFormat = new SimpleDateFormat(
                        DbHelper.DATE_FORMAT_STRING, Locale.getDefault());
                localFormat = (SimpleDateFormat) SimpleDateFormat
                        .getDateInstance(DateFormat.SHORT);
            }
            try {
                Date dateTime = databaseFormat.parse(dateTimeString);
                result = localFormat.format(dateTime);
            } catch (ParseException e) {
                Log.e(CLASS_NAME, Utils.EXCEPTION_CAUGHT, e);
            }
        }
        return result;
    } // getDateTimeStringFromDbDateTime()

    public static long[] getSelectedListViewItemIds(Activity activity, Integer listViewId) {
        ListView listView = (ListView) activity.findViewById(listViewId);
        return listView.getCheckedItemIds();
    } // getSelectedListViewItemIds()

    public static String getStringFromEditText(Activity activity, Integer editTextId) {
        String result = "";
        EditText editText = (EditText) activity.findViewById(editTextId);
        if (editText.length() > 0) {
            result = editText.getText().toString();
        }
        return result;
    } // getStringFromEditText()

    public static boolean isAndroidBeforeMarshmallow() {
        return Build.VERSION.SDK_INT < 23;
    } // isAndroidBeforeMarshmallow()

    public static Float readFloatRespectingNull(String columnName, Cursor cursor) {
        Float result = Float.NaN;
        if (cursor.isNull(cursor.getColumnIndex(columnName)) == false) {
            result = cursor.getFloat(cursor.getColumnIndex(columnName));
        }
        return result;
    } // readFloatRespectingNull()

    public static void setTextFromDateColumn(
            Activity activity, Cursor cursor, String columnName, Integer editTextId) {
        EditText editText = (EditText) activity.findViewById(editTextId);
        int columnIndex = cursor.getColumnIndex(columnName);
        String s = getDateTimeStringFromDbDateTime(cursor, columnIndex, false);
        editText.setText(s);
    } // setTextFromDateColumn()

    public static void setTextFromStringColumn(
            Activity activity, Cursor cursor, String columnName, Integer editTextId) {
        EditText editText = (EditText) activity.findViewById(editTextId);
        String value = cursor.getString(cursor.getColumnIndex(columnName));
        editText.setText(value);
    } // setTextFromStringColumn()

} // class Utils
