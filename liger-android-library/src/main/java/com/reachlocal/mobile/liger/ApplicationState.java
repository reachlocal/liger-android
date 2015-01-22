package com.reachlocal.mobile.liger;

/**
 * Created by Mark Wagner on 1/21/15.
 */
public enum ApplicationState {
    ACTIVE("android_active"),
    INACTIVE("android_inactive");

    private String stringValue;

    private ApplicationState(String toString) {
        stringValue = toString;

    }

    @Override
    public String toString() {
        return stringValue;
    }
}
