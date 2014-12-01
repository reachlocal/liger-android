package com.reachlocal.mobile.liger.utils;


import android.util.Log;

import com.reachlocal.mobile.liger.LIGER;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static String getRightButtonName(String optionsString) {
        if (StringUtils.isEmpty(optionsString)) {
            return null;
        }
        String buttonName = null;
        try {
            buttonName = getRightButtonName(new JSONObject(optionsString));
        } catch (JSONException e) {
            Log.e(LIGER.TAG, "JsonUtils.getRightButtonName() failed to parse options string: " + optionsString, e);
        }
        return buttonName;
    }

    public static String getRightButtonName(JSONObject optionsObj) {
        JSONObject rightObj = optionsObj == null ? null : optionsObj.optJSONObject("right");
        return rightObj == null ? null : rightObj.optString("button", null);
    }
}
