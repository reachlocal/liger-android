package com.reachlocal.mobile.liger.gcm;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class GcmRegistrationHelper {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "gcm_registration_id";
    private static final String PROPERTY_APP_VERSION = "gcm_app_version";

    private Activity mActivity;
    private Context mAppContext;
    private OnGcmRegisteredListener mListener;

    private GoogleCloudMessaging mGcm;

    public GcmRegistrationHelper(Activity activity, OnGcmRegisteredListener listener) {
        mActivity = activity;
        mAppContext = activity.getApplicationContext();
        mListener = listener;
    }

    public GcmRegistrationHelper(Activity activity) {
        mActivity = activity;
        mAppContext = activity.getApplicationContext();
    }

    public void registerGcm() {
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(mAppContext);
            String regId = getRegistrationId(mAppContext);

            if (StringUtils.isEmpty(regId)) {
                registerInBackground();
            } else {
                mListener.onGcmRegistered(regId, null);
            }
        } else {
            Log.i(LIGER.TAG, "GcmRegistrationHelper: No valid Google Play Services APK found.");
        }

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mAppContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LIGER.TAG, "GcmRegistrationHelper: This device does not support GCM.");
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (StringUtils.isEmpty(registrationId)) {
            Log.i(LIGER.TAG, "GcmRegistrationHelper: GCM Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LIGER.TAG, "GcmRegistrationHelper: App version changed. GCM will be re-registered");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("GcmRegistrationHelper: Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new RegistrationTask().execute();
    }

    private class RegistrationTask extends AsyncTask<Void, Void, String> {
        Exception exception;

        @Override
        protected String doInBackground(Void... params) {
            String regId = null;

            try {
                regId = mGcm.register(mAppContext.getString(R.string.liger_gcm_sender_id));
            } catch (IOException e) {
                Log.e(LIGER.TAG, "GcmRegistrationHelper: Failed to register for GCM");
            }
            return regId;
        }

        @Override
        protected void onPostExecute(String regId) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_REG_ID, regId);
            editor.putInt(PROPERTY_APP_VERSION, getAppVersion(mAppContext));
            editor.commit();

            mListener.onGcmRegistered(regId, exception == null ? null : exception.getMessage());
        }
    }

    public interface OnGcmRegisteredListener {
        public void onGcmRegistered(String registrationId, String errorMessage);
    }
}
