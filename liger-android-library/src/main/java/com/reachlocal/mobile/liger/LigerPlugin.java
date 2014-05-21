package com.reachlocal.mobile.liger;

import android.util.Log;
import com.reachlocal.mobile.liger.utils.CordovaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import static com.reachlocal.mobile.liger.utils.CordovaUtils.argsToString;

public class LigerPlugin extends CordovaPlugin {


    public LigerPlugin() {
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        DefaultMainActivity mainActivity = (DefaultMainActivity) cordova.getActivity();

        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerPlugin.execute() action:" + action + ", args:" + argsToString(args));
        }
        try {
            if (action.equalsIgnoreCase("openPage")) {
                return openPage(args.getString(0), args.optString(1), args.optJSONObject(2), callbackContext);
            }
            if (action.equalsIgnoreCase("closePage")) {
                return closePage(callbackContext, args.optString(0));
            }
            if (action.equalsIgnoreCase("updateParent")) {
                return updateParent(args.getJSONObject(1), callbackContext);
            }
            if (action.equalsIgnoreCase("openDialog")) {
                return openDialog(null, args.getString(0), args.optJSONObject(1), callbackContext);
            }
            if (action.equalsIgnoreCase("userCanRefresh")) {
                return userCanRefresh(args.getBoolean(0), callbackContext);
            }
            if (action.equalsIgnoreCase("openDialogWithTitle")) {
                return openDialog(args.getString(0), args.optString(1), args.optJSONObject(2), callbackContext);
            }
            if (action.equalsIgnoreCase("closeDialog")) {
                return closeDialog(args.getString(0), callbackContext);
            }
            if (action.equalsIgnoreCase("getPageArgs")) {
                return getPageArguments(callbackContext);
            }
            if (action.equalsIgnoreCase("mToolbarLayout")) {
                String toolbarSpec = args.isNull(0) ? null : args.getString(0);
                return toolbar(toolbarSpec, callbackContext);
            } else {
                if (LIGER.LOGGING) {
                    Log.w(LIGER.TAG, "LigerPlugin unknown action:" + action + ", args:" + argsToString(args));
                }
            }
        } catch (Exception e) {
            Log.e(LIGER.TAG, "Exception during plugin processing", e);
            callbackContext.error(e.getMessage());
        }
        return false;
    }

    private boolean userCanRefresh(final boolean canRefresh, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);
        if (webFragment != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webFragment.setUserCanRefresh(canRefresh);
                }
            });
        }
        callbackContext.success();
        return true;
    }

    /**
     * Opens a new page.
     *
     * @param title           The title of the page (title in UINavigationBar on iOS)
     * @param link            The 'name' of the page to be open. Should not include html.
     * @param args            json that will be sent to openLinkArguments
     * @param callbackContext the callbackContext
     */
    public boolean openPage(final String title, final String link, final JSONObject args, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.openPage(link, title, args);
            }
        });
        callbackContext.success();
        return true;
    }

    public boolean openDialog(final String title, final String pageName, final JSONObject args, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.openDialog(pageName, title, args);
            }
        });
        callbackContext.success();
        return true;
    }

    public boolean closePage(CallbackContext callbackContext, final String closeTo) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment closedPage = PageFragment.fromCallbackContext(callbackContext);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.closePage(closedPage, closeTo);
            }
        });
        callbackContext.success();
        return true;
    }

    public boolean updateParent(final JSONObject args, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webFragment.setParentUpdateArgs(args == null ? null : args.toString());
            }
        });
        callbackContext.success();
        return true;
    }

    public boolean getPageArguments(CallbackContext callbackContext) {
        String args = null;
        PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);
        if (webFragment != null) {
            args = webFragment.getPageArgs();
        }
        callbackContext.success(CordovaUtils.stringToArgs(args));
        return true;
    }

    public boolean toolbar(final String toolbarSpec, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);
        if (webFragment != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webFragment.setToolbar(toolbarSpec);
                }
            });
        }
        callbackContext.success();
        return true;
    }

    /**
     * Closes a page in an open dialog
     *
     * @param args            json argument to send back to page that opened the dialog by calling closeDialogArguments
     * @param callbackContext the callbackContext
     */
    public boolean closeDialog(final String args, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment dialog = PageFragment.fromCallbackContext(callbackContext);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject argObj = CordovaUtils.stringToArgs(args);
                String page = argObj.optString("page");
                boolean hasReset = argObj.optBoolean("resetApp") || StringUtils.isEmpty(page);

                if (dialog != null) {
                    dialog.dismiss();
                }
                if (hasReset) {
                    activity.resetApp();
                } else {
                    activity.openPage(page, null, argObj);
                    activity.sendJavascriptWithArgs("PAGE", "closeDialogArguments", args);
                }
            }
        });

        callbackContext.success();
        return true;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

}
