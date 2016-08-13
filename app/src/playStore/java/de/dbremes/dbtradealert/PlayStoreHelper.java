package de.dbremes.dbtradealert;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class PlayStoreHelper {

    private PlayStoreHelper() {
        // Hide default construcor
    }

    public static void reportAction(Context context, String actionTitle, int actionId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(actionId));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, actionTitle);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    } // reportAction()
} // class PlayStoreHelper
