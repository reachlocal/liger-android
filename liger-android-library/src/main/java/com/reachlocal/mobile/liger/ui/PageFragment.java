package com.reachlocal.mobile.liger.ui;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.utils.CordovaUtils;
import org.apache.cordova.CallbackContext;
import org.json.JSONObject;

import java.util.Deque;
import java.util.LinkedList;

public abstract class PageFragment extends DialogFragment {

    protected PageFragment mContainer = null;

    public static final String DIALOG_FRAGMENT = "ligerDialogFragment";

    protected Deque<PageFragment> mFragDeck = new LinkedList<PageFragment>();

    public static PageFragment fromCallbackContext(CallbackContext cc) {
        return CordovaUtils.fromCallbackContext(cc, R.id.web_view_parent_frag);
    }

    protected boolean popTo(FragmentTransaction ft, PageFragment newTop) {
        if (mFragDeck.size() > 0 && mFragDeck.getLast().equals(newTop)) {
            return false;
        }
        boolean hasRemoved = false;
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        while (mFragDeck.size() > 0) {
            PageFragment top = mFragDeck.getLast();
            if (top == newTop) {
                break;
            } else {
                mFragDeck.removeLast();
                top.doPageClosed();
                ft.remove(top);
                hasRemoved = true;
            }
        }
        logStack("popTo");
        return hasRemoved;
    }

    public abstract void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions);

    public abstract void openDialog(String pageName, String title, JSONObject args, JSONObject options);

    public PageFragment getContainer(){ return mContainer; }

    public abstract String getPageName();

    public abstract String getPageTitle();

    public abstract void setToolbar(String toolbarSpec);

    public abstract void setChildArgs(String childUpdateArgs);

    public abstract String getPageArgs();

    public abstract String getParentUpdateArgs();

    public abstract void setParentUpdateArgs(String parentUpdateArgs);

    public abstract void setUserCanRefresh(boolean canRefresh);

    public void doPageAppear() {}

    public void addFragments(FragmentTransaction ft, int contentViewID){}

    public void doPageClosed() {}

    public void sendJavascriptWithArgs(String object, String function, String args) {}

    public void sendJavascript(String js) {}

    public abstract String closeLastPage(PageFragment closePage, String closeTo);

    protected void logStack(String crumb) {
        if (LIGER.LOGGING) {
            StringBuilder sb = new StringBuilder("PageFragment/").append(crumb).append("\n");
            for (PageFragment frag : mFragDeck) {
                sb.append("\n-->").append(frag.getPageName()).append("/").append(frag.getPageArgs());
            }
            Log.d(LIGER.TAG, sb.toString());
        }
    }

}
