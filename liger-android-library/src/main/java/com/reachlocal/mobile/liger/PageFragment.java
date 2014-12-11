package com.reachlocal.mobile.liger;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.reachlocal.mobile.liger.utils.CordovaUtils;
import org.apache.cordova.CallbackContext;

public abstract class PageFragment extends DialogFragment {

    protected FragmentActivity mContext;

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

    public void doPageClosed() {};

    public void sendJavascriptWithArgs(String object, String function, String args) {};

    public void sendJavascript(String js) {};

    @Override
    public void onAttach(Activity activity) {
        mContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

}
