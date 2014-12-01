package com.reachlocal.mobile.liger.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MenuItemSpec {
    private String mName;
    private String mIconText;
    private String mPage;
    private JSONObject mArgs;
    private JSONObject mOptions;
    private String mDetail;
    private String mAccessibilityLabel;
    private boolean mDialog;
    private boolean mMajor;
    private String mMenuIdString;

    public String getName() {
        return mName;
    }

    public String getMenuIdString() {
        return mMenuIdString;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getIconText() {
        return mIconText;
    }

    public void setIconText(String iconText) {
        mIconText = iconText;
    }

    public String getPage() {
        return mPage;
    }

    public void setPage(String page) {
        mPage = page;
    }

    public JSONObject getArgs() {
        return mArgs;
    }

    public JSONObject getOptions() {
        return mOptions;
    }

    public void setArgs(JSONObject args) {
        mArgs = args;
    }

    public void setOptions(JSONObject options) {
        mOptions = options;
    }

    public String getDetail() {
        return mDetail;
    }

    public void setDetail(String detail) {
        mDetail = detail;
    }

    public String getAccessibilityLabel() {
        return mAccessibilityLabel;
    }

    public void setAccessibilityLabel(String accessibilityLabel) {
        mAccessibilityLabel = accessibilityLabel;
    }

    public boolean isDialog() {
        return mDialog;
    }

    public void setDialog(boolean dialog) {
        this.mDialog = dialog;
    }

    public boolean isMajor() {
        return mMajor;
    }

    public void setMajor(boolean major) {
        mMajor = major;
    }

    public static MenuItemSpec createFromJSON(JSONObject jsonObject, boolean major, int menuId) {
        MenuItemSpec spec = new MenuItemSpec();
        try {
            spec.mName = jsonObject.getString("name");
            spec.mIconText = jsonObject.optString("iconText");
            spec.mPage = jsonObject.getString("page");
            spec.mArgs = jsonObject.optJSONObject("args");
            spec.mOptions = jsonObject.optJSONObject("options");
            String menuLevel = major ? "1" : "2";
            String reuseIdentifier;
            if(spec.mOptions != null){
                reuseIdentifier = spec.mOptions.optString("reuseIdentifier", "menu_" + menuLevel + "_" + Integer.toString(menuId) );
            } else {
                spec.mOptions = new JSONObject();
                reuseIdentifier = "menu_" + menuLevel + "_" + Integer.toString(menuId);
            }
            spec.mOptions.put("reuseIdentifier", reuseIdentifier );
            spec.mDetail = jsonObject.optString("detail");
            spec.mAccessibilityLabel = jsonObject.optString("accessibilityLabel");
            spec.mDialog = jsonObject.optBoolean("dialog", false);
            spec.mMajor = major;
            spec.mMenuIdString = jsonObject.getString("name") + (major == true ? "_major_" : "_minor_") + Integer.toString(menuId);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid menu item spec!", e);
        }
        return spec;
    }
}
