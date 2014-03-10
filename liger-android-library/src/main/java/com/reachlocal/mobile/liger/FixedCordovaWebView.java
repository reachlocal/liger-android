package com.reachlocal.mobile.liger;

import android.content.Context;
import android.util.AttributeSet;
import org.apache.cordova.CordovaWebView;

public class FixedCordovaWebView extends CordovaWebView {
    public FixedCordovaWebView(Context context) {
        super(context);
    }

    public FixedCordovaWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedCordovaWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FixedCordovaWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
    }

    public void applyAfterMoveFix() {
        onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY());
    }
}
