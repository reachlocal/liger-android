package com.reachlocal.mobile.liger.model;

import android.content.Context;
import com.reachlocal.mobile.liger.LIGER;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
            JSONArray menuParentArray = null;

            if(appConfig.appFormatVersion >= 6) {
                JSONObject rootPage = parentObj.getJSONObject("rootPage");
                appConfig.mRootPageArgs = rootPage.getJSONObject("args");
                appConfig.mRootPageName = rootPage.getString("page");
                appConfig.mAppConfigString = appConfig.mRootPageArgs.toString();
                appConfig.mNotifications = parentObj.optBoolean("notifications");
            } else {
                throw new RuntimeException("Not Supported appFormatVersion");
            }

            JSONArray majorMenu = null;
            JSONArray minorMenu = null;

            if(menuParentArray != null) {
                majorMenu = menuParentArray.optJSONArray(0);
                minorMenu = menuParentArray.optJSONArray(1);
            }

            if (majorMenu != null) {
                appConfig.setMajorMenuItems(createMenuItems(majorMenu, true));
            }

            if (minorMenu != null) {
                appConfig.setMinorMenuItems(createMenuItems(minorMenu, false));
            }

        } catch (JSONException e) {
            throw new RuntimeException("Invalid app.json!", e);
        }
        return appConfig;
    }

    public static List<MenuItemSpec> createMenuItems(JSONArray menuArray, boolean major) throws JSONException {
        List<MenuItemSpec> menuList = new ArrayList<MenuItemSpec>();
        int size = menuArray.length();
        for (int i = 0; i < size; i++) {
            menuList.add(MenuItemSpec.createFromJSON(menuArray.getJSONObject(i), major));
        }
        return menuList;
    }

    public List<String> getPageNames() {
        ArrayList<String> pageNames = new ArrayList<String>();
        addPageNames(pageNames, mMajorMenuItems);
        addPageNames(pageNames, mMinorMenuItems);
        return pageNames;
    }

    private void addPageNames(List<String> pageNames, List<MenuItemSpec> menuItems) {
        if (menuItems == null) {
            return;
        }
        for (MenuItemSpec item : menuItems) {
            if (!item.isDialog()) {
                pageNames.add(item.getPage());
            }
        }
    }
}
