package com.reachlocal.mobile.liger.model;

import android.content.Context;
import com.reachlocal.mobile.liger.LIGER;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AppConfig {

    private long appFormatVersion;
    private List<MenuItemSpec> mMajorMenuItems;
    private List<MenuItemSpec> mMinorMenuItems;

    private String mAppConfigString;

    private String mRootPageName;
    private String mRootPageTitle = "";
    private JSONObject mRootPageArgs;
    private JSONObject mRootPageOptions;

    private boolean mNotifications;

    private static AppConfig sAppConfig;

    public long getAppFormatVersion() {
        return appFormatVersion;
    }

    public void setAppFormatVersion(long appFormatVersion) {
        this.appFormatVersion = appFormatVersion;
        if (appFormatVersion < LIGER.APP_FORMAT_MIN_VERSION || appFormatVersion > LIGER.APP_FORMAT_MAX_VERSION) {
            throw new RuntimeException("Invalid appFormatVersion in app.json! Expected " + LIGER.APP_FORMAT_MIN_VERSION + " to " + LIGER.APP_FORMAT_MAX_VERSION + ", but found " + appFormatVersion);
        }
    }

    public List<MenuItemSpec> getMajorMenuItems() {
        return mMajorMenuItems;
    }

    public void setMajorMenuItems(List<MenuItemSpec> majorMenuItems) {
        mMajorMenuItems = majorMenuItems;
    }

    public List<MenuItemSpec> getMinorMenuItems() {
        return mMinorMenuItems;
    }

    public void setMinorMenuItems(List<MenuItemSpec> minorMenuItems) {
        mMinorMenuItems = minorMenuItems;
    }

    public String getAppConfigString() {
        return mAppConfigString;
    }

    public String getRootPageName() {
        return mRootPageName;
    }

    public String getRootPageTitle() { return mRootPageTitle; }

    public JSONObject getRootPageArgs() {
        return mRootPageArgs;
    }

    public JSONObject getRootPageOptions() {
        return mRootPageOptions;
    }

    public boolean getNotificationsEnabled() {
        return mNotifications;
    }

    public static AppConfig getAppConfig(Context context) {
        if (sAppConfig != null) {
            return sAppConfig;
        }
        InputStream is;
        try {
            is = context.getAssets().open("app/app.json");
            sAppConfig = parseAppConfig(IOUtils.toString(is));
            is.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read app.json!", e);
        }
        return sAppConfig;
    }

    public static AppConfig parseAppConfig(String appConfigString) {
        AppConfig appConfig = new AppConfig();
        try {
            JSONObject parentObj = new JSONObject(appConfigString);
            appConfig.setAppFormatVersion(parentObj.getLong("appFormatVersion"));

            if(appConfig.appFormatVersion >= 6) {
                JSONObject rootPage = parentObj.getJSONObject("rootPage");
                appConfig.mRootPageArgs = rootPage.getJSONObject("args");
                appConfig.mRootPageName = rootPage.getString("page");
                appConfig.mAppConfigString = appConfig.mRootPageArgs.toString();
                appConfig.mNotifications = parentObj.optBoolean("notifications");
            } else {
                throw new RuntimeException("Not Supported appFormatVersion");
            }

        } catch (JSONException e) {
            throw new RuntimeException("Invalid app.json!", e);
        }
        return appConfig;
    }

}
