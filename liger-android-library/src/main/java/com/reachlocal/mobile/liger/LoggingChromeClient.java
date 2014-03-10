package com.reachlocal.mobile.liger;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

public class LoggingChromeClient extends WebChromeClient {
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
