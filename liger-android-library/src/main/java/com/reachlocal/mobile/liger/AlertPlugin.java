package com.reachlocal.mobile.liger;

import android.util.Log;

import com.reachlocal.mobile.liger.ui.DefaultMainActivity;
import com.reachlocal.mobile.liger.widgets.AlertDialogFragment;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import static com.reachlocal.mobile.liger.utils.CordovaUtils.argsToString;

public class AlertPlugin extends CordovaPlugin{
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "AlertPlugin.execute() action:" + action + ", args:" + argsToString(args));
        }
        if (action.equalsIgnoreCase("openAlert")) {
            return openAlert(args.optString(0), args.optString(1), args.optString(2), callbackContext);
        }
        if(LIGER.LOGGING) {
            Log.w(LIGER.TAG, "Unknown AlertPlugin action: " + action);
        }
        return false;
    }

    private boolean openAlert(String title, String message, String buttonLabel, CallbackContext callbackContext) {
        DefaultMainActivity mainActivity = (DefaultMainActivity) cordova.getActivity();

        AlertDialogFragment alertFrag = AlertDialogFragment.build(title, message, buttonLabel);
        alertFrag.show(mainActivity.getSupportFragmentManager(), "alertDialog");
        callbackContext.success();
        return true;
    }
}
