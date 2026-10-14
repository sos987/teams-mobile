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
                
                // Essential WebView properties for heavy SPAs like Microsoft Teams
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setSavePassword(true);
                settings.setAllowContentAccess(true);
                settings.setAllowFileAccess(true);
                
                // Allow cross-origin requests natively to fix broken styles
                settings.setAllowUniversalAccessFromFileURLs(true);
                settings.setAllowFileAccessFromFileURLs(true);
                
                // Secure cookie state tracking
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
                
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        String url = request.getUrl().toString();
                        // Prevent breaking out into external system browsers during auth loops
                        if (url.contains("microsoft") || url.contains("live.com") || url.contains("teams")) {
                            view.loadUrl(url);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        CookieManager.getInstance().flush();
                        
                        // Inject our specific mobile-friendly layouts
                        String customCSS = 
                            "body { zoom: 0.85 !important; }" +
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
