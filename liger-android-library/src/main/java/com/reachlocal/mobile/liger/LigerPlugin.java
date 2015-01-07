package com.reachlocal.mobile.liger;

import android.util.Log;

import com.reachlocal.mobile.liger.gcm.GcmRegistrationHelper;
import com.reachlocal.mobile.liger.ui.DefaultMainActivity;
import com.reachlocal.mobile.liger.ui.PageFragment;
import com.reachlocal.mobile.liger.utils.CordovaUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.reachlocal.mobile.liger.utils.CordovaUtils.argsToString;

public class LigerPlugin extends CordovaPlugin {


    public LigerPlugin() {
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerPlugin.execute() action:" + action + ", args:" + argsToString(args));
        }
        try {
            if (action.equalsIgnoreCase("openPage")) {
                return openPage(args.getString(0), args.optString(1), args.optJSONObject(2), args.optJSONObject(3), callbackContext);
            } else if (action.equalsIgnoreCase("displayNotification")) {
                return displayNotification(args.getString(0), args.optString(1), args.getString(2));
            } else if (action.equalsIgnoreCase("getPushToken")) {
                return getPushToken(callbackContext);
            } else if (action.equalsIgnoreCase("closePage")) {
                return closePage(callbackContext, args.optString(0));
            } else if (action.equalsIgnoreCase("updateParent")) {
                return updateParent(args.getJSONObject(1), callbackContext);
            } else if (action.equalsIgnoreCase("openDialog")) {
                return openDialog(null, args.getString(0), args.optJSONObject(1), args.optJSONObject(2), callbackContext);
            } else if (action.equalsIgnoreCase("openDialogWithTitle")) {
                return openDialog(args.getString(0), args.optString(1), args.optJSONObject(2), args.optJSONObject(3), callbackContext);
            } else if (action.equalsIgnoreCase("closeDialog")) {
                return closeDialog(args.getString(0), callbackContext);
            } else if (action.equalsIgnoreCase("getPageArgs")) {
                return getPageArguments(callbackContext);
            } else if (action.equalsIgnoreCase("mToolbarLayout")) {
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

    public boolean getPushToken(CallbackContext callbackContext){
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);

        GcmRegistrationHelper gcmHelper = new GcmRegistrationHelper(activity);

        String regID = gcmHelper.getRegistrationId(activity);

        webFragment.pushNotificationTokenUpdated(regID, "");

        return true;
    }
    public boolean displayNotification(String userUri, String message, String appName){
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();


        //TODO - This is a temporary testing method testing the sending and receiving of GCM messages
        // Remove this later


        GcmRegistrationHelper gcmHelper = new GcmRegistrationHelper(activity);


        final String regID = gcmHelper.getRegistrationId(activity);

        String msg = "";

        try{

            String apiKey = activity.getString(R.string.liger_gcm_sender_id);

            // 1. URL
            URL url = new URL("https://android.googleapis.com/gcm/send");

            // 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=AIzaSyBzR3Osnbv5uWEbCOl7BFjkGm_Bg7GGGWg");

            conn.setDoOutput(true);

            // 5. Add JSON data into POST request body

            //`5.1 Use Jackson object mapper to convert Contnet object into JSON
            JSONObject content = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(regID);
            JSONObject data = new JSONObject();
            data.put("message", message);
            data.put("userURI", userUri);

            content.put("registration_ids", regIds);
            content.put("data", data);
            // 5.2 Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            // 5.3 Copy Content "JSON" into
           // wr.writeBytes(URLEncoder.encode(content.toString(), "UTF-8"));
            wr.writeBytes(content.toString());
            // 5.4 Send the request
            wr.flush();

            // 5.5 close
            wr.close();

            // 6. Get the response
            int responseCode = conn.getResponseCode();
            Log.e(LIGER.TAG, "\nSending 'POST' request to URL : " + url);
            Log.e(LIGER.TAG, "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 7. Print result
            Log.e(LIGER.TAG, response.toString());
            msg = "Sent message";
        } catch (MalformedURLException ex) {
            msg = "MalformedURLException Error :" + ex.getMessage();
        } catch (IOException ex) {
            msg = "IOException Error :" + ex.getMessage();
            ex.printStackTrace();
        } catch (JSONException ex) {
            msg = "Error :" + ex.getMessage();
        }


        Log.e(LIGER.TAG, msg);

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
    public boolean openPage(final String title, final String link, final JSONObject args, final JSONObject options, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (webFragment.getContainer() == null) {
                    activity.openPage(link, title, args, options);
                } else {
                    webFragment.getContainer().openPage(link, title, args, options);
                }
            }
        });
        callbackContext.success();
        return true;
    }

    public boolean openDialog(final String title, final String pageName, final JSONObject args, final JSONObject options, CallbackContext callbackContext) {
        final DefaultMainActivity activity = (DefaultMainActivity) cordova.getActivity();
        final PageFragment webFragment = PageFragment.fromCallbackContext(callbackContext);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (webFragment.getContainer() == null) {
                    activity.openDialog(pageName, title, args, options);
                } else {
                    webFragment.getContainer().openDialog(pageName, title, args, options);
                }

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
                if (closedPage.getContainer() == null) {
                    activity.closePage(closedPage, closeTo);
                } else {
                    closedPage.getContainer().closeLastPage(closedPage, closeTo);
                }

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
                    PageFragment parent = dialog.getContainer();
                    if (parent != null) {
                        parent.dismiss();
                    } else {
                        dialog.dismiss();
                    }
                }
                if (hasReset) {
                    activity.resetApp();
                } else {
                    activity.openPage(page, null, argObj, null);
                    activity.getRootPageFragment().closeDialogArguments(args);
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
