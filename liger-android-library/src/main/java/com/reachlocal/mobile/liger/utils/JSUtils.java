package com.reachlocal.mobile.liger.utils;

import org.apache.commons.lang3.StringUtils;

public class JSUtils {
    public static final String cleanJSString(String jsString) {
        if (StringUtils.equalsIgnoreCase(jsString, "undefined")) {
            return null;
        }
        return jsString;
    }
}
