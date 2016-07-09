package de.dbremes.dbtradealert;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Utils {
    public static final String EXCEPTION_CAUGHT = "Exception caught";

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

    public static  boolean isAndroidBeforeMarshmallow() {
        return Build.VERSION.SDK_INT < 23;
    } // isAndroidBeforeMarshmallow()

    public static Float readFloatRespectingNull(String columnName, Cursor cursor) {
        Float result = Float.NaN;
        if (cursor.isNull(cursor.getColumnIndex(columnName)) == false) {
            result = cursor.getFloat(cursor.getColumnIndex(columnName));
        }
        return result;
    } // readFloatRespectingNull()
} // class Utils
