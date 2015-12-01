package com.andevcon.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class AnDevConMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        // Find reference to WebView
        WebView webView = (WebView) findViewById(R.id.webview);

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Add our JavaScript Bridge Interface as "AnDevConJavaBridge"
        webView.addJavascriptInterface(new JavaScriptBridge(webView), "AnDevConJavaBridge");

        // Load webpage to talk to JavaScript (For demo purposes included in assets)
        webView.loadUrl("file:///android_asset/AnDevCon.html");
    }

}
