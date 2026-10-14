package com.custom.teamsdesktop;

import android.os.Bundle;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.bridge.getWebView().post(new Runnable() {
            @Override
            public void run() {
                WebView webView = bridge.getWebView();
                WebSettings settings = webView.getSettings();
                
                // Hardcode user agent definition directly into WebSettings configuration properties
                String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
                settings.setUserAgentString(desktopUA);
                
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setSavePassword(true);
                settings.setAllowContentAccess(true);
                settings.setAllowFileAccess(true);
                
                // Keep credentials persistently stored on local disk cache structures
                settings.setGeolocationEnabled(true);
                
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
                
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        String url = request.getUrl().toString();
                        // Force all redirections to inherit the Desktop User Agent structure
                        view.getSettings().setUserAgentString(desktopUA);
                        
                        if (url.contains("microsoft") || url.contains("live.com") || url.contains("teams")) {
                            view.loadUrl(url);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        CookieManager.getInstance().flush(); // Writes session credentials securely to persistent disk storage
                        
                        String customCSS = 
                            "body { zoom: 0.85 !important; padding-top: 36px !important; }" +
                            "[data-test-id='left-rail'], .v2-left-rail { width: 50px !important; min-width: 50px !important; }" +
                            ".ts-message-list-item { padding-left: 4px !important; padding-right: 4px !important; }" +
                            ".download-app-banner, .mobile-promo { display: none !important; }";
                        
                        String encodedCSS = Base64.encodeToString(customCSS.getBytes(), Base64.NO_WRAP);
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
        });
    }
}
