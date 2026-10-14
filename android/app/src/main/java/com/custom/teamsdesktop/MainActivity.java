package com.custom.teamsdesktop;

import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Wait for the Capacitor bridge WebView to initialize
        this.bridge.getWebView().setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                // Define your mobile-friendly desktop UI tweaks here
                String customCSS = "body { zoom: 0.85 !important; }" +
                                   "[data-test-id='left-rail'], .v2-left-rail { width: 50px !important; min-width: 50px !important; }" +
                                   ".ts-message-list-item { padding-left: 4px !important; padding-right: 4px !important; }" +
                                   ".download-app-banner, .mobile-promo { display: none !important; }";
                
                String encodedCSS = Base64.encodeToString(customCSS.getBytes(), Base64.NO_WRAP);
                
                // Programmatic injection
                view.evaluateJavascript(
                    "(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encodedCSS + "');" +
                    "parent.appendChild(style);" +
                    "})();", null
                );
            }
        });
    }
}
