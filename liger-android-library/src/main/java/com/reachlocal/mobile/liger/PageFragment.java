package com.reachlocal.mobile.liger;

import android.support.v4.app.Fragment;
import com.reachlocal.mobile.liger.utils.CordovaUtils;
import org.apache.cordova.CallbackContext;

public abstract class PageFragment extends Fragment {
    public static PageFragment fromCallbackContext(CallbackContext cc) {
        return CordovaUtils.fromCallbackContext(cc, R.id.web_view_parent_frag);
    }

    public abstract String getPageName();

    public abstract void setToolbar(String toolbarSpec);

    public abstract void setChildArgs(String childUpdateArgs);

    public abstract String getPageArgs();

    public abstract String getParentUpdateArgs();

    public abstract void setParentUpdateArgs(String parentUpdateArgs);

    public abstract void setUserCanRefresh(boolean canRefresh);

    public void doPageAppear() {};

    public void sendJavascriptWithArgs(String object, String function, String args) {};

    public void sendJavascript(String js) {};

}
