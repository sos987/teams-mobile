package com.custom.teamsdesktop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.bridge.getWebView().post(new Runnable() {
            @Override
            public void run() {
                final WebView webView = bridge.getWebView();
                WebSettings settings = webView.getSettings();
                
                String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
                settings.setUserAgentString(desktopUA);
                
                // Включаем долгосрочные хранилища данных для сессий
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setSavePassword(true);
                
                // Глобальная авторизация кук
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);

                // Агрессивный интервальный скрипт для инжекции CSS (сработает даже после редиректов Microsoft)
                final String heavyCSSInjection = 
                    "(function() {" +
                    "  var id = 'custom-teams-margins';" +
                    "  if (document.getElementById(id)) return;" + // Если уже добавлено, не дублируем
                    "  var style = document.createElement('style');" +
                    "  style.id = id;" +
                    "  style.type = 'text/css';" +
                    "  style.innerHTML = '" +
                    "    /* Сдвигаем абсолютно позиционированный контейнер приложения целиком */" +
                    "    body, html, #app-container, .app-width, #main-layout-id {" +
                    "      padding-top: 45px !important;" +
                    "      box-sizing: border-box !important;" +
                    "    }" +
                    "    /* Корректируем верхнюю панель Teams (лупа, профиль) */" +
                    "    #tou-header-layout, .v2-header, [data-test-id=\"desktop-header\"], div[class*=\"header-container\"] {" +
                    "      top: 45px !important;" +
                    "    }" +
                    "    /* Сдвигаем боковую панель вниз, чтобы она соответствовала шапке */" +
                    "    .v2-left-rail, [data-test-id=\"left-rail\"], div[class*=\"leftRail\"] {" +
                    "      top: 95px !important;" +
                    "      width: 50px !important; min-width: 50px !important;" +
                    "    }" +
                    "    body { zoom: 0.85 !important; }" +
                    "    .download-app-banner, .mobile-promo { display: none !important; }" +
                    "  ';" +
                    "  document.head.appendChild(style);" +
                    "})();";

                // Запускаем цикл инжекции: каждые 1.5 секунды в течение первых 15 секунд
                final Handler handler = new Handler(Looper.getMainLooper());
                for (int i = 0; i < 10; i++) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            webView.evaluateJavascript(heavyCSSInjection, null);
                        }
                    }, i * 1500);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Насильно заставляем Android записать файлы кук на диск
        CookieManager.getInstance().flush();
    }
}
