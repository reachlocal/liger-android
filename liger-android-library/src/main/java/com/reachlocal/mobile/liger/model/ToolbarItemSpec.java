package com.reachlocal.mobile.liger.model;

import android.util.Log;

import com.reachlocal.mobile.liger.LIGER;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ToolbarItemSpec {
    private String callback;
    private String iconGlyph;

    public static ToolbarItemSpec parseSpec(JSONObject object) {
        if (object == null) {
            return null;
        }
        ToolbarItemSpec spec = new ToolbarItemSpec();
        spec.callback = object.optString("callback");
        spec.iconGlyph = object.optString("icon");
        return spec;
    }

    public static List<ToolbarItemSpec> parseSpecArray(JSONArray array) {
        ArrayList<ToolbarItemSpec> list = new ArrayList<ToolbarItemSpec>();
        if (array != null) {
            int specCount = array.length();
            for (int i = 0; i < specCount; i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) {
                    ToolbarItemSpec spec = parseSpec(obj);
                    list.add(spec);
                }
            }
        }
        return list;
    }

    public static List<ToolbarItemSpec> parseSpecArray(String specAsJsonString) {
        if (specAsJsonString == null || specAsJsonString.length() == 0) {
            return null;
        }
        JSONArray array = null;
        try {
            array = new JSONArray(specAsJsonString);
        } catch (JSONException e) {
            Log.e(LIGER.TAG, "Failed to parse toolbar spec array: " + specAsJsonString, e);
        }
        return parseSpecArray(array);
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getIconGlyph() {
        return iconGlyph;
    }

    public void setIconGlyph(String iconGlyph) {
        this.iconGlyph = iconGlyph;
    }
}
