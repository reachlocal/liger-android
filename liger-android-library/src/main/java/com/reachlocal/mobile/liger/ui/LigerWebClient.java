package com.reachlocal.mobile.liger.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.reachlocal.mobile.liger.LIGER;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;

import java.io.IOException;
import java.io.InputStream;

public class LigerWebClient extends CordovaWebViewClient {

    protected CordovaPageFragment mPageFragment;
    protected DefaultMainActivity mActivity;
    ProgressDialog mProgressDialog;

    public LigerWebClient(CordovaPageFragment fragment, DefaultMainActivity activity, CordovaWebView webView) {
        super(activity, webView);
        mActivity = activity;
        mPageFragment = fragment;
    }

    @Override
    public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPageFragment.onPageStarted(view, url, favicon);
            }
        });
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(final WebView view, final String url) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPageFragment.onPageFinished(view, url);
            }
        });
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerWebClient.onLoadResource() " + url);
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, String.format("LigerWebClient.onReceivedError(), errorCode %d, description: %s, failuingUrl: %s ", errorCode, description, failingUrl));
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    public static class LigerICSWebClient extends LigerWebClient {
        public static final String ASSET_BASE_URL = "file:///android_asset/";

        public LigerICSWebClient(CordovaPageFragment fragment, DefaultMainActivity activity, CordovaWebView webView) {
            super(fragment, activity, webView);
        }

        public static WebResourceResponse getTrimmedAsset(Context context, String url) {
            if (LIGER.LOGGING) {
                Log.d(LIGER.TAG, "LigerWebClient.getTrimmedAsset: " + url);
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

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return getTrimmedAsset(mActivity, url);
        }
    }
}


