package de.dbremes.dbtradealert;

import android.database.Cursor;

public class Utils {
    public static Float readFloatRespectingNull(String columnName, Cursor cursor) {
        Float result = Float.NaN;
        if (cursor.isNull(cursor.getColumnIndex(columnName)) == false) {
            result = cursor.getFloat(cursor.getColumnIndex(columnName));
        }
        return result;
    }
}
