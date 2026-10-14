package com.custom.teamsdesktop;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        this.bridge.getWebView().post(new Runnable() {
            @Override
            public void run() {
                WebView webView = bridge.getWebView();
                WebSettings settings = webView.getSettings();
                
                // Глобальный десктопный User-Agent на все запросы
                String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
                settings.setUserAgentString(desktopUA);
                
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setSavePassword(true);
                
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        CookieManager.getInstance().flush();
                        
                        // Сдвигаем элементы фронтенда вниз под статус-бар
                        String jsCSSInjection = 
                            "(function() {" +
                            "  var style = document.createElement('style');" +
                            "  style.type = 'text/css';" +
                            "  style.innerHTML = '" +
                            "    body { zoom: 0.85 !important; }" +
                            "    #tou-header-layout, .v2-header, [data-test-id=\"desktop-header\"], div[class*=\"header-container\"] {" +
                            "      padding-top: 45px !important; " +
                            "      height: auto !important;" +
                            "    }" +
                            "    [data-test-id=\"left-rail\"], .v2-left-rail {" +
                            "      width: 50px !important; min-width: 50px !important; " +
                            "    }" +
                            "    .download-app-banner, .mobile-promo { display: none !important; }" +
                            "  ';" +
                            "  document.head.appendChild(style);" +
                            "})();";
                        
                        webView.evaluateJavascript(jsCSSInjection, null);
                    }
                });
            }
        });
    }
}
