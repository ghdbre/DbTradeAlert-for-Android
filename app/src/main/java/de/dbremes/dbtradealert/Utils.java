package de.dbremes.dbtradealert;

import android.database.Cursor;
import android.os.Build;

public class Utils {
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
