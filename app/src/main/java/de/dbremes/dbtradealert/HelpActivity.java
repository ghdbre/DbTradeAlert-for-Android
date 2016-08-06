package de.dbremes.dbtradealert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/Help.html");
    } // onCreate()
} // class HelpActivity
