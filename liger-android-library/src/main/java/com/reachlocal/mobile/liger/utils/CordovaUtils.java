package com.reachlocal.mobile.liger.utils;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.reachlocal.mobile.liger.LIGER;

import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class CordovaUtils {

    @SuppressWarnings("unchecked")
    public static <T extends Fragment> T fromCallbackContext(CallbackContext cc, int tagId) {
        CordovaWebView webView = null;
        try {
            Field f = cc.getClass().getDeclaredField("webView"); //NoSuchFieldException
            f.setAccessible(true);
            webView = (CordovaWebView) f.get(cc);
        } catch (Exception e) {
            Log.e(LIGER.TAG, "Failed to get webview from callback context", e);
        }
        return (T) webView.getTag(tagId);
    }

    public static JSONObject stringToArgs(String argString) {
        JSONObject result = null;

        if (argString == null || argString.length() == 0 || StringUtils.equals(argString, "null")) {
            argString = "{}";
        }
        try {
            result = new JSONObject(argString);
        } catch (JSONException e) {
            Log.e(LIGER.TAG, "Failed to parse arguments: " + argString, e);
        }
        if (result == null) {
            result = new JSONObject();
        }
        return result;
    }

    public static boolean hasReset(String args) {
        boolean hasReset = false;
        if (args != null && args.length() > 0) {
            JSONObject obj;
            try {
                obj = new JSONObject(args);
                hasReset = obj.optBoolean("resetApp");
            } catch (JSONException e) {
                Log.e(LIGER.TAG, "Failed to parse arguments for hasReset: " + args, e);
            }
        }
        return hasReset;
    }

    public static String argsToString(CordovaArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 4; i++) {
            sb.append(args.optString(i));
            if (i < 3) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
