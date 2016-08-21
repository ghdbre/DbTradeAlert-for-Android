package de.dbremes.dbtradealert;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.view.Gravity;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class AdHelper {
    private final static String SAMPLE_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    public static View getAdView(Context context) {
        // Create AdView
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(SAMPLE_AD_UNIT_ID);
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT);
        lp.setAnchorId(R.id.container);
        lp.anchorGravity = Gravity.BOTTOM;
        lp.gravity = Gravity.BOTTOM;
        adView.setLayoutParams(lp);
        // Load ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        return adView;
    } // getAdView()

    public static void initialize(Context context) {
        MobileAds.initialize(context, SAMPLE_AD_UNIT_ID);
    } // initialize()
} // class AdHelper
