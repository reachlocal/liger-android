package com.reachlocal.mobile.liger.utils;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mark Wagner on 1/16/15.
 */
public class LigerJSONObject extends JSONObject {

    public static Object wrap(Object o) {
        if (o == null) {
            return NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                JSONArray newArray = new JSONArray();
                Object array = o;
                final int length = Array.getLength(array);
                for (int i = 0; i < length; ++i) {
                    newArray.put(wrap(Array.get(array, i)));
                }

                return newArray;
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            if (o instanceof Bundle) {
                JSONObject newObject = new JSONObject();
                Set<String> keys = ((Bundle) o).keySet();
                for (String key : keys) {
                    try {
                        // json.put(key, bundle.get(key)); see edit below
                        newObject.put(key, wrap(((Bundle) o).get(key)));
                    } catch (JSONException e) {
                        //Handle exception here
                    }
                }
                return newObject;
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
