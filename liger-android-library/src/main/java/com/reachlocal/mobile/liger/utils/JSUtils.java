package com.reachlocal.mobile.liger.utils;

import org.apache.commons.lang3.StringUtils;

public class JSUtils {
    public static final String cleanJSString(String jsString) {
        if (StringUtils.equalsIgnoreCase(jsString, "undefined")) {
            return null;
        }
        return jsString;
    }

    public static final String stringListToArgString(String... args) {
        StringBuilder sb = new StringBuilder();
        int argCount = args.length;

        for (int i = 0; i < argCount; i++) {
            String arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg.startsWith("{") || arg.startsWith("[")) {
                sb.append(arg);
            } else {
                sb.append("\"");
                sb.append(arg);
                sb.append("\"");
            }
            if ((i + 1) < argCount) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
