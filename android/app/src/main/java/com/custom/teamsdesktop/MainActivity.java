package com.custom.teamsdesktop;

import android.os.Bundle;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        // Use the native bridge post-render queue loop
        this.bridge.getWebView().post(new Runnable() {
            @Override
            public void run() {
                WebView webView = bridge.getWebView();
                WebSettings settings = webView.getSettings();
                
                // Hardcode specific User Agent to match the capacitor configuration string
                String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
                settings.setUserAgentString(desktopUA);
                
                // Retain data structures globally across app restarts
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setSavePassword(true);
                settings.setAllowContentAccess(true);
                settings.setAllowFileAccess(true);
                
                // Global cookie configuration rules
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
                
                // Inject responsive adjustments targeting Teams specific DOM elements directly
                String jsCSSInjection = 
                    "(function() {" +
                    "  var style = document.createElement('style');" +
                    "  style.type = 'text/css';" +
                    "  style.innerHTML = '" +
                    "    body, #main-layout-id, .app-margin { zoom: 0.85 !important; }" +
                    "    [data-test-id=\"left-rail\"], .v2-left-rail { width: 50px !important; min-width: 50px !important; }" +
                    "    .ts-message-list-item { padding-left: 4px !important; padding-right: 4px !important; }" +
                    "    .download-app-banner, .mobile-promo { display: none !important; }" +
                    "  ';" +
                    "  document.head.appendChild(style);" +
                    "})();";
                
                // Run the script directly through the active window frame
                webView.evaluateJavascript(jsCSSInjection, null);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Force sync active storage structures to the physical storage disk before closing background threads
        CookieManager.getInstance().flush();
    }
}