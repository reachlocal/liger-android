package com.reachlocal.mobile.liger.utils;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.reachlocal.mobile.liger.LIGER;

import java.io.IOException;
import java.io.InputStream;

public class CompatUtils {

    public static final String ASSET_BASE_URL = "file:///android_asset/";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static WebResourceResponse icsShouldInterceptRequest(Context context, WebView view, String url) {
        if(LIGER.LOGGING) {
            Log.d(LIGER.TAG, "WebViewClient.shouldInterceptRequest: " + url);
        }
        try {
            if (url.startsWith(ASSET_BASE_URL)) {
                int endPos = url.indexOf('?');
                if (endPos > 0) {
                    int startPos = ASSET_BASE_URL.length();
                    String assetPath = url.substring(startPos, endPos);
                    InputStream is = context.getResources().getAssets().open(assetPath);
                    return new WebResourceResponse(null, "UTF-8", is);
                }
            }
        } catch (IOException e) {
            Log.e(LIGER.TAG, "Failed to intercept asset request: " + url, e);
        }
        return null;
    }

}
