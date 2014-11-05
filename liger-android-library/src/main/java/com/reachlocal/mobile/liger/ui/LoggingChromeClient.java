package com.reachlocal.mobile.liger.ui;

import android.util.Log;
import android.webkit.ConsoleMessage;

import com.reachlocal.mobile.liger.LIGER;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;


public class LoggingChromeClient extends CordovaChromeClient {


    public LoggingChromeClient(CordovaInterface cordova) {
        super(cordova);
    }

    public LoggingChromeClient(CordovaInterface ctx, CordovaWebView app) {
        super(ctx, app);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, cm.message() + " -- From line "
                    + cm.lineNumber() + " of "
                    + cm.sourceId());
        }
        return true;
    }
}
